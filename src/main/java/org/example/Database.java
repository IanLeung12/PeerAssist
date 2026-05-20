package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * [Database.java]
 * Supabase REST client for PeerAssist. Talks to the Supabase Auth API and the
 * PostgREST data API using the public publishable key; Row Level Security
 * policies on the server bound what each signed-in user can do.
 * @author Ian Leung
 * @version 3.0
 */
public class Database {

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
     * @return The new User, or null if sign up failed.
     */
    public User signUp(String name, int grade, String email, String password, ArrayList<String> subjects) {
        try {
            JsonObject creds = new JsonObject();
            creds.addProperty("email", email);
            creds.addProperty("password", password);
            JsonObject session = JsonParser.parseString(
                    send("POST", baseUrl + "/auth/v1/signup", creds.toString(), null, true))
                    .getAsJsonObject();

            storeSession(session);
            String userId = session.getAsJsonObject("user").get("id").getAsString();

            JsonObject profile = new JsonObject();
            profile.addProperty("id", userId);
            profile.addProperty("name", name);
            profile.addProperty("grade", grade);
            profile.addProperty("subjects", String.join(" ", subjects));
            send("POST", baseUrl + "/rest/v1/profiles", profile.toString(), null, false);

            return new User(userId, name, grade, email, password, subjects);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * logIn
     * Authenticates against Supabase Auth and loads the user's profile.
     *
     * @return The logged-in User, or null if the credentials were rejected.
     */
    public User logIn(String email, String password) {
        try {
            JsonObject creds = new JsonObject();
            creds.addProperty("email", email);
            creds.addProperty("password", password);
            JsonObject session = JsonParser.parseString(
                    send("POST", baseUrl + "/auth/v1/token?grant_type=password", creds.toString(), null, true))
                    .getAsJsonObject();

            storeSession(session);
            String userId = session.getAsJsonObject("user").get("id").getAsString();

            JsonArray rows = JsonParser.parseString(
                    send("GET", baseUrl + "/rest/v1/profiles?select=*&id=eq." + userId, null, null, false))
                    .getAsJsonArray();
            if (rows.size() == 0) {
                return null;
            }
            JsonObject p = rows.get(0).getAsJsonObject();
            return new User(userId, p.get("name").getAsString(), p.get("grade").getAsInt(),
                    email, password, splitSubjects(p.get("subjects").getAsString()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * loadUsers
     * Loads every user's profile.
     *
     * @return The list of users.
     */
    public ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        JsonArray rows = JsonParser.parseString(
                send("GET", baseUrl + "/rest/v1/profiles?select=*", null, null, false))
                .getAsJsonArray();
        for (int i = 0; i < rows.size(); i++) {
            JsonObject p = rows.get(i).getAsJsonObject();
            users.add(new User(p.get("id").getAsString(), p.get("name").getAsString(),
                    p.get("grade").getAsInt(), "", "",
                    splitSubjects(p.get("subjects").getAsString())));
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

        JsonArray rows = JsonParser.parseString(
                send("POST", baseUrl + "/rest/v1/documents", body.toString(), "return=representation", false))
                .getAsJsonArray();
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
        send("POST", baseUrl + "/rest/v1/reviews", body.toString(), null, false);
    }

    /**
     * loadDocs
     * Loads all documents along with their reviews.
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

        JsonArray docRows = JsonParser.parseString(
                send("GET", baseUrl + "/rest/v1/documents?select=*&order=doc_id", null, null, false))
                .getAsJsonArray();
        for (int i = 0; i < docRows.size(); i++) {
            JsonObject d = docRows.get(i).getAsJsonObject();

            ArrayList<String> topics = new ArrayList<>();
            String subjects = d.get("subjects").getAsString();
            for (int j = 0; j < subjects.length(); j++) {
                if (subjects.charAt(j) == '1') {
                    topics.add(DisplayConst.subjectArr[j]);
                }
            }
            int docId = d.get("doc_id").getAsInt();
            org.example.Document document = new org.example.Document(
                    docId,
                    d.get("name").getAsString(),
                    userMap.get(d.get("user_id").getAsString()),
                    d.get("max_mark").getAsDouble(),
                    d.get("grade_level").getAsInt(),
                    topics,
                    Base64.getDecoder().decode(d.get("content").getAsString()));
            documents.add(document);
            docMap.put(docId, document);
        }

        JsonArray reviewRows = JsonParser.parseString(
                send("GET", baseUrl + "/rest/v1/reviews?select=*&order=review_id", null, null, false))
                .getAsJsonArray();
        for (int i = 0; i < reviewRows.size(); i++) {
            JsonObject r = reviewRows.get(i).getAsJsonObject();
            org.example.Document document = docMap.get(r.get("doc_id").getAsInt());
            if (document != null) {
                document.addReview(new Review(
                        userMap.get(r.get("reviewer").getAsString()),
                        r.get("mark").getAsDouble(),
                        r.get("comment").getAsString()));
            }
        }
        return documents;
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
        this.accessToken = session.get("access_token").getAsString();
        this.refreshToken = session.get("refresh_token").getAsString();
    }

    /**
     * refreshSession
     * Exchanges the refresh token for a fresh access token.
     */
    private void refreshSession() {
        JsonObject body = new JsonObject();
        body.addProperty("refresh_token", refreshToken);
        JsonObject session = JsonParser.parseString(
                send("POST", baseUrl + "/auth/v1/token?grant_type=refresh_token", body.toString(), null, true))
                .getAsJsonObject();
        storeSession(session);
    }

    /**
     * splitSubjects
     * Splits a space-joined subjects string into a list.
     */
    private ArrayList<String> splitSubjects(String str) {
        ArrayList<String> subjects = new ArrayList<>();
        for (String s : str.split(" ")) {
            subjects.add(s);
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
     */
    private String send(String method, String url, String body, String prefer, boolean isAuth) {
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setRequestMethod(method);
                c.setRequestProperty("apikey", publishableKey);
                c.setRequestProperty("Content-Type", "application/json");
                if (!isAuth) {
                    c.setRequestProperty("Authorization",
                            "Bearer " + (accessToken != null ? accessToken : publishableKey));
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
                    throw new RuntimeException("HTTP " + code + " for " + url + ": " + response);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException("Request failed: " + url, e);
            }
        }
        throw new RuntimeException("Request failed after session refresh: " + url);
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
}
