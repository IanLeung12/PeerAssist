package org.example; /**
 * [PeerAssist.java]
 * Runs the PeerAssist Platform, a platform designed for students to publish and review assignments and other documents
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.UIManager;


public class PeerAssist {
    public static void main(String[] args) throws InterruptedException {
        // Modern look-and-feel. Must run before any Swing component is created.
        FlatLaf.registerCustomDefaultsSource("themes"); // themes/FlatLaf.properties: refined green accent
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 14);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);

        ArrayList<User> users = new ArrayList<>();
        ArrayList<Document> documents = new ArrayList<>();


        String dbUrl = loadDbUrl();
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new RuntimeException("Database URL not set. Add db.url to db.properties " +
                    "in the project root, or set the PEERASSIST_DB_URL environment variable.");
        }
        Database db = new Database(dbUrl);
        users = db.loadUsers();

        // Login loop
        LoginDisplay login = new LoginDisplay(users, db);
        while (login.getUser() == null) {
            login.refresh();
            Thread.sleep(5);
        }
        User user = login.getUser();
        if (login.isNewUser()) {
            users.add(user);
        }
        login.dispose();
        documents = db.loadDocs(users);


        // Main loop
        MainDisplay md = new MainDisplay(user, documents, users, db);
        while (true) {
            md.refresh();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * loadDbUrl
     * Reads the database URL from db.properties (project root), falling back to
     * the PEERASSIST_DB_URL environment variable if the file is missing.
     *
     * @return The database URL, or null if not configured.
     */
    private static String loadDbUrl() {
        File file = new File("db.properties");
        if (file.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                String url = props.getProperty("db.url");
                if (url != null && !url.isEmpty()) {
                    return url;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return System.getenv("PEERASSIST_DB_URL");
    }

}
