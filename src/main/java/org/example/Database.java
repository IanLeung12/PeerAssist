package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [Database.java]
 * Supabase REST client for PeerAssist. Talks to the Supabase Auth API and the
 * PostgREST data API using the public publishable key; Row Level Security
 * policies on the server bound what each signed-in user can do.
 * @author Ian Leung
 * @version 3.0
 */
public class Database {

    private static final Logger LOG = Logger.getLogger(Database.class.getName());

    private final String baseUrl;
    private final String publishableKey;
    private String accessToken;
    private String refreshToken;

    /**
     * Constructor for the Database class.
     *
     * @param url            The Supabase project URL (e.g. https://abc.supabase.co).
     * @param publishableKey The Supabase publishable key.
     */
    public Database(String url, String publishableKey) {
        this.baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.publishableKey = publishableKey;
    }

    /**
     * signUp
     * Registers a new account with Supabase Auth and creates its profile row.
     *
     * @return The new User.
     * @throws AuthException if the sign up is rejected (e.g. email exists, weak
     *                       password) or the server cannot be reached.
     */
    public User signUp(String name, int grade, String email, String password, ArrayList<String> subjects)
            throws AuthException {
        try {
            JsonObject creds = new JsonObject();
            creds.addProperty("email", email);
            creds.addProperty("password", password);
            JsonObject session = JsonParser.parseString(
                    send("POST", baseUrl + "/auth/v1/signup", creds.toString(), null, true))
                    .getAsJsonObject();

            storeSession(session);
            String userId = getString(session.getAsJsonObject("user"), "id", null);
            if (userId == null) {
                throw new AuthException(AuthException.Kind.UNKNOWN, 0,
                        AuthException.userMessageFor(AuthException.Kind.UNKNOWN), null);
            }

            JsonObject profile = new JsonObject();
            profile.addProperty("id", userId);
            profile.addProperty("name", name);
            profile.addProperty("grade", grade);
            profile.addProperty("subjects", String.join(" ", subjects));
            send("POST", baseUrl + "/rest/v1/profiles", profile.toString(), null, false);

            // Password intentionally discarded; never stored on the client User.
            return new User(userId, name, grade, email, subjects);
        } catch (HttpFailure f) {
            throw toAuthException(f, true);
        }
    }

    /**
     * logIn
     * Authenticates against Supabase Auth and loads the user's profile.
     *
     * @return The logged-in User.
     * @throws AuthException if the credentials are rejected or the server cannot
     *                       be reached.
     */
    public User logIn(String email, String password) throws AuthException {
        try {
            JsonObject creds = new JsonObject();
            creds.addProperty("email", email);
            creds.addProperty("password", password);
            JsonObject session = JsonParser.parseString(
                    send("POST", baseUrl + "/auth/v1/token?grant_type=password", creds.toString(), null, true))
                    .getAsJsonObject();

            storeSession(session);
            String userId = getString(session.getAsJsonObject("user"), "id", null);
            if (userId == null) {
                throw new AuthException(AuthException.Kind.UNKNOWN, 0,
                        AuthException.userMessageFor(AuthException.Kind.UNKNOWN), null);
            }

            JsonArray rows = JsonParser.parseString(
                    send("GET", baseUrl + "/rest/v1/profiles?select=*&id=eq." + encode(userId), null, null, false))
                    .getAsJsonArray();
            if (rows.size() == 0) {
                throw new AuthException(AuthException.Kind.UNKNOWN, 0,
                        AuthException.userMessageFor(AuthException.Kind.UNKNOWN), null);
            }
            JsonObject p = rows.get(0).getAsJsonObject();
            // Password intentionally discarded; never stored on the client User.
            return new User(userId, getString(p, "name", ""), getInt(p, "grade", 0),
                    email, splitSubjects(getString(p, "subjects", "")));
        } catch (HttpFailure f) {
            throw toAuthException(f, false);
        }
    }

    /**
     * loadUsers
     * Loads every user's profile. Malformed rows are skipped rather than
     * aborting the whole load.
     *
     * @return The list of users.
     */
    public ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        JsonArray rows;
        try {
            rows = JsonParser.parseString(
                    send("GET", baseUrl + "/rest/v1/profiles?select=*", null, null, false))
                    .getAsJsonArray();
        } catch (HttpFailure f) {
            throw asRuntime(f);
        }
        for (int i = 0; i < rows.size(); i++) {
            try {
                JsonObject p = rows.get(i).getAsJsonObject();
                String id = getString(p, "id", null);
                if (id == null) {
                    continue;
                }
                users.add(new User(id, getString(p, "name", ""), getInt(p, "grade", 0),
                        "", splitSubjects(getString(p, "subjects", ""))));
            } catch (Exception e) {
                LOG.log(Level.FINE, "Skipping malformed profile row", e);
            }
        }
        return users;
    }

    /**
     * saveDoc
     * Inserts a document and sets the generated id back on the Document object.
     *
     * @param doc The document to save.
     */
    public void saveDoc(org.example.Document doc) {
        StringBuilder subjectStr = new StringBuilder();
        for (int i = 0; i < DisplayConst.subjectArr.length; i++) {
            subjectStr.append(doc.getTopics().contains(DisplayConst.subjectArr[i]) ? "1" : "0");
        }

        JsonObject body = new JsonObject();
        body.addProperty("user_id", doc.getUser().getID());
        body.addProperty("name", doc.getName());
        body.addProperty("max_mark", doc.getMaxMark());
        body.addProperty("grade_level", doc.getGradeLevel());
        body.addProperty("subjects", subjectStr.toString());
        body.addProperty("content", Base64.getEncoder().encodeToString(doc.convertToBytes()));

        JsonArray rows;
        try {
            rows = JsonParser.parseString(
                    send("POST", baseUrl + "/rest/v1/documents", body.toString(), "return=representation", false))
                    .getAsJsonArray();
        } catch (HttpFailure f) {
            throw asRuntime(f);
        }
        doc.setID(rows.get(0).getAsJsonObject().get("doc_id").getAsInt());
    }

    /**
     * addReview
     * Inserts a review for the given document.
     *
     * @param document The document being reviewed.
     * @param review   The review to add.
     */
    public void addReview(org.example.Document document, Review review) {
        JsonObject body = new JsonObject();
        body.addProperty("doc_id", document.getID());
        body.addProperty("reviewer", review.getUser().getID());
        body.addProperty("mark", review.getMark());
        body.addProperty("comment", review.getComments());
        try {
            send("POST", baseUrl + "/rest/v1/reviews", body.toString(), null, false);
        } catch (HttpFailure f) {
            throw asRuntime(f);
        }
    }

    /**
     * deleteDoc
     * Deletes a document owned by the current user. Row Level Security ensures
     * only the owner can delete it.
     *
     * @param doc The document to delete.
     */
    public void deleteDoc(org.example.Document doc) {
        try {
            send("DELETE", baseUrl + "/rest/v1/documents?doc_id=eq." + encode(String.valueOf(doc.getID())),
                    null, null, false);
        } catch (HttpFailure f) {
            throw asRuntime(f);
        }
    }

    /**
     * deleteReview
     * Deletes the given review (a review the current user authored) from the
     * given document. Row Level Security ensures only the reviewer can delete it.
     *
     * @param doc    The document the review belongs to.
     * @param review The review to delete.
     */
    public void deleteReview(org.example.Document doc, Review review) {
        try {
            send("DELETE", baseUrl + "/rest/v1/reviews?doc_id=eq." + encode(String.valueOf(doc.getID()))
                    + "&reviewer=eq." + encode(review.getUser().getID()), null, null, false);
        } catch (HttpFailure f) {
            throw asRuntime(f);
        }
    }

    /**
     * loadDocs
     * Loads all documents along with their reviews. Malformed document or review
     * rows are skipped rather than aborting the whole load.
     *
     * @param users The list of users used to resolve owner and reviewer ids.
     * @return The list of documents.
     */
    public ArrayList<org.example.Document> loadDocs(ArrayList<User> users) {
        Map<String, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getID(), user);
        }

        ArrayList<org.example.Document> documents = new ArrayList<>();
        Map<Integer, org.example.Document> docMap = new HashMap<>();

        JsonArray docRows;
        try {
            docRows = JsonParser.parseString(
                    send("GET", baseUrl + "/rest/v1/documents?select=*&order=doc_id", null, null, false))
                    .getAsJsonArray();
        } catch (HttpFailure f) {
            throw asRuntime(f);
        }
        for (int i = 0; i < docRows.size(); i++) {
            try {
                JsonObject d = docRows.get(i).getAsJsonObject();

                ArrayList<String> topics = new ArrayList<>();
                String subjects = getString(d, "subjects", "");
                for (int j = 0; j < subjects.length() && j < DisplayConst.subjectArr.length; j++) {
                    if (subjects.charAt(j) == '1') {
                        topics.add(DisplayConst.subjectArr[j]);
                    }
                }
                if (!d.has("doc_id") || d.get("doc_id").isJsonNull()) {
                    continue;
                }
                int docId = d.get("doc_id").getAsInt();
                String content = getString(d, "content", null);
                if (content == null) {
                    LOG.log(Level.FINE, "Skipping document {0} with no content", docId);
                    continue;
                }
                org.example.Document document = new org.example.Document(
                        docId,
                        getString(d, "name", ""),
                        userMap.get(getString(d, "user_id", "")),
                        getDouble(d, "max_mark", 0),
                        getInt(d, "grade_level", 0),
                        topics,
                        Base64.getDecoder().decode(content));
                documents.add(document);
                docMap.put(docId, document);
            } catch (Exception e) {
                LOG.log(Level.FINE, "Skipping malformed document row", e);
            }
        }

        JsonArray reviewRows;
        try {
            reviewRows = JsonParser.parseString(
                    send("GET", baseUrl + "/rest/v1/reviews?select=*&order=review_id", null, null, false))
                    .getAsJsonArray();
        } catch (HttpFailure f) {
            throw asRuntime(f);
        }
        for (int i = 0; i < reviewRows.size(); i++) {
            try {
                JsonObject r = reviewRows.get(i).getAsJsonObject();
                if (!r.has("doc_id") || r.get("doc_id").isJsonNull()) {
                    continue;
                }
                org.example.Document document = docMap.get(r.get("doc_id").getAsInt());
                if (document != null) {
                    document.addReview(new Review(
                            userMap.get(getString(r, "reviewer", "")),
                            getDouble(r, "mark", 0),
                            getString(r, "comment", "")));
                }
            } catch (Exception e) {
                LOG.log(Level.FINE, "Skipping malformed review row", e);
            }
        }
        return documents;
    }

    /**
     * logout
     * Clears the local session and makes a best-effort attempt to invalidate the
     * session server-side. Any failure of the server call is ignored.
     */
    public void logout() {
        String token = accessToken;
        this.accessToken = null;
        this.refreshToken = null;
        if (token == null) {
            return;
        }
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(baseUrl + "/auth/v1/logout").openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("apikey", publishableKey);
            c.setRequestProperty("Authorization", "Bearer " + token);
            c.setRequestProperty("Content-Type", "application/json");
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            c.getResponseCode();
            c.disconnect();
        } catch (Exception e) {
            LOG.log(Level.FINE, "Best-effort logout request failed", e);
        }
    }

    /**
     * close
     * No-op. The REST API is stateless; kept so existing callers compile.
     */
    public void close() {
    }

    /**
     * storeSession
     * Saves the access and refresh tokens from an Auth response.
     */
    private void storeSession(JsonObject session) {
        this.accessToken = getString(session, "access_token", null);
        this.refreshToken = getString(session, "refresh_token", null);
    }

    /**
     * refreshSession
     * Exchanges the refresh token for a fresh access token.
     */
    private void refreshSession() throws HttpFailure {
        JsonObject body = new JsonObject();
        body.addProperty("refresh_token", refreshToken);
        JsonObject session = JsonParser.parseString(
                send("POST", baseUrl + "/auth/v1/token?grant_type=refresh_token", body.toString(), null, true))
                .getAsJsonObject();
        storeSession(session);
    }

    /**
     * splitSubjects
     * Splits a whitespace-joined subjects string into a list. Returns an empty
     * list for null or blank input and skips empty tokens.
     */
    private ArrayList<String> splitSubjects(String str) {
        ArrayList<String> subjects = new ArrayList<>();
        if (str == null || str.trim().isEmpty()) {
            return subjects;
        }
        for (String s : str.trim().split("\\s+")) {
            if (!s.isEmpty()) {
                subjects.add(s);
            }
        }
        return subjects;
    }

    /**
     * send
     * Performs an HTTP request against Supabase. Retries once after refreshing
     * the session if the access token has expired (HTTP 401).
     *
     * @param method   HTTP method.
     * @param url      Full request URL.
     * @param body     JSON request body, or null.
     * @param prefer   Value for the Prefer header, or null.
     * @param isAuth   True for Auth-API calls (do not send the user token).
     * @return The response body.
     * @throws HttpFailure on any non-2xx response or transport error.
     */
    private String send(String method, String url, String body, String prefer, boolean isAuth) throws HttpFailure {
        // Data (non-auth) calls require a real session token. Never fall back to
        // the publishable key as a Bearer token for these calls.
        if (!isAuth && accessToken == null && refreshToken == null) {
            throw new RuntimeException("Session expired");
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setRequestMethod(method);
                c.setRequestProperty("apikey", publishableKey);
                c.setRequestProperty("Content-Type", "application/json");
                if (!isAuth && accessToken != null) {
                    c.setRequestProperty("Authorization", "Bearer " + accessToken);
                }
                if (prefer != null) {
                    c.setRequestProperty("Prefer", prefer);
                }
                if (body != null) {
                    c.setDoOutput(true);
                    try (OutputStream os = c.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }

                int code = c.getResponseCode();
                if (code == 401 && !isAuth && refreshToken != null && attempt == 0) {
                    refreshSession();
                    continue;
                }
                InputStream is = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream();
                String response = is == null ? "" : readAll(is);
                if (code < 200 || code >= 300) {
                    throw new HttpFailure(code, response, url, null);
                }
                return response;
            } catch (IOException e) {
                throw new HttpFailure(0, null, url, e);
            }
        }
        throw new HttpFailure(0, null, url, null);
    }

    /**
     * readAll
     * Reads an input stream fully into a UTF-8 string.
     */
    private String readAll(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        is.close();
        return out.toString("UTF-8");
    }

    /**
     * encode
     * URL-encodes a user-derived query value for safe inclusion in a URL.
     */
    private String encode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported.
            return value;
        }
    }

    /**
     * asRuntime
     * Converts an HttpFailure raised by a data call into a RuntimeException with
     * a safe, non-sensitive message. Server bodies are logged at FINE only.
     */
    private RuntimeException asRuntime(HttpFailure f) {
        if (f.cause instanceof IOException) {
            LOG.log(Level.FINE, "Network error for " + f.url, f.cause);
            return new RuntimeException("Network error reaching the server", f.cause);
        }
        LOG.log(Level.FINE, "HTTP {0} for {1}", new Object[]{f.httpStatus, f.url});
        return new RuntimeException("Request failed (HTTP " + f.httpStatus + ")");
    }

    /**
     * toAuthException
     * Maps an internal HttpFailure from an auth flow into a typed AuthException.
     * Auth server bodies are inspected for known error codes but never logged or
     * surfaced verbatim.
     *
     * @param f        The internal failure.
     * @param isSignUp True if the failure happened during sign up.
     */
    private AuthException toAuthException(HttpFailure f, boolean isSignUp) {
        if (f.cause instanceof IOException) {
            LOG.log(Level.FINE, "Auth network error for " + safeUrl(f.url), f.cause);
            return new AuthException(AuthException.Kind.NETWORK, 0,
                    AuthException.userMessageFor(AuthException.Kind.NETWORK), f.cause);
        }

        int status = f.httpStatus;
        String code = "";
        String combined = "";
        try {
            if (f.body != null && !f.body.isEmpty()) {
                JsonElement parsed = JsonParser.parseString(f.body);
                if (parsed.isJsonObject()) {
                    JsonObject o = parsed.getAsJsonObject();
                    String errorCode = getString(o, "error_code", "");
                    String msg = getString(o, "msg", "");
                    String error = getString(o, "error", "");
                    String errorDescription = getString(o, "error_description", "");
                    code = errorCode;
                    combined = (errorCode + " " + msg + " " + error + " " + errorDescription)
                            .toLowerCase(Locale.ROOT);
                }
            }
        } catch (Exception e) {
            // Body was not valid JSON; fall back to status-based mapping.
            LOG.log(Level.FINE, "Auth error body was not parseable JSON", e);
        }

        AuthException.Kind kind = classify(status, code, combined, isSignUp);
        LOG.log(Level.FINE, "Auth failure mapped to {0} (status {1})", new Object[]{kind, status});
        return new AuthException(kind, status, AuthException.userMessageFor(kind), null);
    }

    /**
     * classify
     * Decides the AuthException kind from the HTTP status and (already
     * lower-cased) auth error code/body text.
     */
    private AuthException.Kind classify(int status, String code, String combined, boolean isSignUp) {
        if (status >= 500) {
            return AuthException.Kind.SERVER;
        }
        if (combined.contains("email_not_confirmed") || combined.contains("email not confirmed")) {
            return AuthException.Kind.EMAIL_NOT_CONFIRMED;
        }
        if (combined.contains("user_already_exists") || combined.contains("email_exists")
                || combined.contains("already registered") || combined.contains("already been registered")) {
            return AuthException.Kind.EMAIL_EXISTS;
        }
        if (combined.contains("weak_password") || combined.contains("password should be")
                || combined.contains("password is too")) {
            return AuthException.Kind.WEAK_PASSWORD;
        }
        if (combined.contains("invalid_grant") || (status == 400 && !isSignUp && combined.isEmpty())) {
            return AuthException.Kind.INVALID_CREDENTIALS;
        }
        if (combined.contains("invalid_credentials") || combined.contains("invalid login")
                || combined.contains("invalid email or password")) {
            return AuthException.Kind.INVALID_CREDENTIALS;
        }
        if (combined.contains("invalid_email") || combined.contains("invalid email")
                || combined.contains("unable to validate email")) {
            return AuthException.Kind.INVALID_EMAIL;
        }
        if (combined.contains("validation") || code.contains("validation")) {
            return AuthException.Kind.VALIDATION;
        }
        if (status == 422) {
            return AuthException.Kind.VALIDATION;
        }
        if (status == 400) {
            return isSignUp ? AuthException.Kind.VALIDATION : AuthException.Kind.INVALID_CREDENTIALS;
        }
        return AuthException.Kind.UNKNOWN;
    }

    /**
     * safeUrl
     * Strips query strings so URLs can be logged without leaking parameters.
     */
    private String safeUrl(String url) {
        if (url == null) {
            return "";
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    /**
     * getString
     * Safely reads a string property, returning a default when absent or null.
     */
    private static String getString(JsonObject o, String key, String def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) {
            return def;
        }
        try {
            return o.get(key).getAsString();
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * getInt
     * Safely reads an int property, returning a default when absent or invalid.
     */
    private static int getInt(JsonObject o, String key, int def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) {
            return def;
        }
        try {
            return o.get(key).getAsInt();
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * getDouble
     * Safely reads a double property, returning a default when absent or invalid.
     */
    private static double getDouble(JsonObject o, String key, double def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) {
            return def;
        }
        try {
            return o.get(key).getAsDouble();
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * HttpFailure
     * Internal unchecked failure carrying the HTTP status and (possibly null)
     * response body so auth flows can map kinds and data flows can wrap it. A
     * non-null {@code cause} that is an IOException signals a transport/network
     * error rather than a server response.
     */
    private static class HttpFailure extends RuntimeException {
        final int httpStatus;
        final String body;
        final String url;
        final Throwable cause;

        HttpFailure(int httpStatus, String body, String url, Throwable cause) {
            super("HTTP failure (" + httpStatus + ") for " + url, cause);
            this.httpStatus = httpStatus;
            this.body = body;
            this.url = url;
            this.cause = cause;
        }
    }
}
