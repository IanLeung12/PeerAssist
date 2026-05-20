package org.example; /**
 * [PeerAssist.java]
 * Runs the PeerAssist Platform, a platform designed for students to publish and review assignments and other documents
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.io.IOException;
import java.io.InputStream;
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

        Properties config = loadConfig();
        String url = config.getProperty("supabase.url");
        String publishableKey = config.getProperty("supabase.publishableKey");
        if (url == null || url.isEmpty() || publishableKey == null || publishableKey.isEmpty()) {
            throw new RuntimeException("supabase.url and supabase.publishableKey must be set in supabase.properties");
        }
        Database db = new Database(url, publishableKey);

        // Login loop
        LoginDisplay login = new LoginDisplay(db);
        while (login.getUser() == null) {
            login.refresh();
            Thread.sleep(5);
        }
        User user = login.getUser();
        login.dispose();

        // Load data after authenticating (Row Level Security requires a session)
        ArrayList<User> users = db.loadUsers();
        ArrayList<Document> documents = db.loadDocs(users);

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
     * loadConfig
     * Loads supabase.properties (Supabase URL + publishable key) from the classpath.
     * The file is bundled inside the JAR, so downloaded copies need no setup.
     *
     * @return The loaded properties.
     */
    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = PeerAssist.class.getResourceAsStream("/supabase.properties")) {
            if (in == null) {
                throw new RuntimeException("supabase.properties not found on the classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read supabase.properties", e);
        }
        return props;
    }

}
