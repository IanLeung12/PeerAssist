package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * [Database.java]
 * PostgreSQL persistence layer for PeerAssist. Stores users, documents and
 * reviews in a free cloud Postgres database (Supabase / Neon free tier).
 * @author Ian Leung
 * @version 2.0
 */
public class Database {

    private final String jdbcUrl;
    private Connection connection;

    /**
     * Constructor for the Database class.
     * Opens a JDBC connection and creates the tables if they do not exist.
     *
     * @param jdbcUrl The Postgres JDBC connection URL.
     */
    public Database(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        conn();
        createTables();
    }

    /**
     * conn
     * Returns a live database connection, reopening it if it has been closed
     * or dropped (e.g. network blip or idle timeout on the cloud database).
     *
     * @return A valid Connection.
     */
    private Connection conn() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(3)) {
                connection = DriverManager.getConnection(jdbcUrl);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
        return connection;
    }

    /**
     * createTables
     * Creates the users, documents and reviews tables if they do not exist.
     */
    private void createTables() {
        try (Statement stmt = conn().createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id SERIAL PRIMARY KEY," +
                    "name TEXT," +
                    "grade INTEGER," +
                    "email TEXT," +
                    "password TEXT," +
                    "subjects TEXT)");
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS documents (" +
                    "doc_id SERIAL PRIMARY KEY," +
                    "user_id INTEGER," +
                    "name TEXT," +
                    "max_mark DOUBLE PRECISION," +
                    "grade_level INTEGER," +
                    "subjects TEXT," +
                    "content BYTEA)");
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS reviews (" +
                    "review_id SERIAL PRIMARY KEY," +
                    "doc_id INTEGER," +
                    "reviewer INTEGER," +
                    "mark DOUBLE PRECISION," +
                    "comment TEXT)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables", e);
        }
    }

    /**
     * saveUser
     * Inserts a user and sets the generated id back on the User object.
     *
     * @param user The user to save.
     */
    public void saveUser(User user) {
        String sql = "INSERT INTO users (name, grade, email, password, subjects) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING user_id";
        try (PreparedStatement stmt = conn().prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getGradeLevel());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, String.join(" ", user.getSubjects()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setID(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    /**
     * saveDoc
     * Inserts a document and sets the generated id back on the Document object.
     *
     * @param doc The document to save.
     */
    public void saveDoc(org.example.Document doc) {
        String subjectStr = "";
        for (int i = 0; i < DisplayConst.subjectArr.length; i++) {
            if (doc.getTopics().contains(DisplayConst.subjectArr[i])) {
                subjectStr = subjectStr + "1";
            } else {
                subjectStr = subjectStr + "0";
            }
        }

        String sql = "INSERT INTO documents (user_id, name, max_mark, grade_level, subjects, content) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING doc_id";
        try (PreparedStatement stmt = conn().prepareStatement(sql)) {
            stmt.setInt(1, doc.getUser().getID());
            stmt.setString(2, doc.getName());
            stmt.setDouble(3, doc.getMaxMark());
            stmt.setInt(4, doc.getGradeLevel());
            stmt.setString(5, subjectStr);
            stmt.setBytes(6, doc.convertToBytes());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    doc.setID(rs.getInt("doc_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save document", e);
        }
    }

    /**
     * addReview
     * Inserts a review for the given document.
     *
     * @param document The document being reviewed.
     * @param review   The review to add.
     */
    public void addReview(org.example.Document document, Review review) {
        String sql = "INSERT INTO reviews (doc_id, reviewer, mark, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn().prepareStatement(sql)) {
            stmt.setInt(1, document.getID());
            stmt.setInt(2, review.getUser().getID());
            stmt.setDouble(3, review.getMark());
            stmt.setString(4, review.getComments());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add review", e);
        }
    }

    /**
     * loadUsers
     * Loads all users ordered by user id.
     *
     * @return The list of users.
     */
    public ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ArrayList<String> subjects = new ArrayList<>();
                String str = rs.getString("subjects");
                String[] strSubjects = str.split(" ");
                for (String subject : strSubjects) {
                    subjects.add(subject);
                }
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getInt("grade"),
                        rs.getString("email"),
                        rs.getString("password"),
                        subjects));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users", e);
        }
        return users;
    }

    /**
     * loadDocs
     * Loads all documents ordered by doc id, along with their reviews.
     *
     * @param users The list of users used to resolve owner and reviewer ids.
     * @return The list of documents.
     */
    public ArrayList<org.example.Document> loadDocs(ArrayList<User> users) {
        ArrayList<org.example.Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY doc_id";
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ArrayList<String> topics = new ArrayList<>();
                String str = rs.getString("subjects");
                for (int i = 0; i < str.length(); i++) {
                    if (str.charAt(i) == '1') {
                        topics.add(DisplayConst.subjectArr[i]);
                    }
                }
                int docId = rs.getInt("doc_id");
                int userId = rs.getInt("user_id");
                org.example.Document document = new org.example.Document(
                        docId,
                        rs.getString("name"),
                        users.get(userId - 1),
                        rs.getDouble("max_mark"),
                        rs.getInt("grade_level"),
                        topics,
                        rs.getBytes("content"));
                loadReviews(document, users);
                documents.add(document);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load documents", e);
        }
        return documents;
    }

    /**
     * loadReviews
     * Loads and attaches all reviews for the given document.
     *
     * @param document The document to load reviews for.
     * @param users    The list of users used to resolve reviewer ids.
     */
    private void loadReviews(org.example.Document document, ArrayList<User> users) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE doc_id = ? ORDER BY review_id";
        try (PreparedStatement stmt = conn().prepareStatement(sql)) {
            stmt.setInt(1, document.getID());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    document.addReview(new Review(
                            users.get(rs.getInt("reviewer") - 1),
                            rs.getDouble("mark"),
                            rs.getString("comment")));
                }
            }
        }
    }

    /**
     * close
     * Closes the database connection.
     */
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
