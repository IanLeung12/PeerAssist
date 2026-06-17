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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;


public class PeerAssist {

    private static final Logger LOG = Logger.getLogger(PeerAssist.class.getName());

    public static void main(String[] args) {
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
        final Database db = new Database(url, publishableKey);

        // Ensure a best-effort logout when the JVM shuts down.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    db.logout();
                } catch (Exception e) {
                    LOG.log(Level.FINE, "Shutdown logout failed", e);
                }
            }
        }));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showLogin(db);
            }
        });
    }

    /**
     * showLogin
     * Shows the login screen and wires the success callback so that, on a
     * successful authentication, the app loads its data off the EDT and then
     * builds the main display. Logging out returns here.
     *
     * @param db The Supabase database client.
     */
    private static void showLogin(final Database db) {
        final LoginDisplay login = new LoginDisplay(db);
        login.setOnLoginSuccess(new java.util.function.Consumer<User>() {
            @Override
            public void accept(final User user) {
                login.dispose();
                loadAndShowMain(db, user);
            }
        });
    }

    /**
     * loadAndShowMain
     * Loads users and documents on a background worker, then builds the main
     * display on the EDT. On failure, shows an error dialog and returns to login.
     *
     * @param db   The Supabase database client.
     * @param user The authenticated user.
     */
    private static void loadAndShowMain(final Database db, final User user) {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                ArrayList<User> users = db.loadUsers();
                ArrayList<Document> documents = db.loadDocs(users);
                return new Object[]{users, documents};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] result = get();
                    ArrayList<User> users = (ArrayList<User>) result[0];
                    ArrayList<Document> documents = (ArrayList<Document>) result[1];
                    Runnable onLogout = new Runnable() {
                        @Override
                        public void run() {
                            showLogin(db);
                        }
                    };
                    new MainDisplay(user, documents, users, db, onLogout);
                } catch (Exception e) {
                    LOG.log(Level.FINE, "Failed to load data after login", e);
                    JOptionPane.showMessageDialog(null,
                            "Could not load data from the server. Please try again.",
                            "PeerAssist", JOptionPane.ERROR_MESSAGE);
                    db.logout();
                    showLogin(db);
                }
            }
        }.execute();
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
