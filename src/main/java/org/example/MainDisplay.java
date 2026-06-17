package org.example; /**
 * [MainDisplay.java]
 * Display frame class that is in charge of the main Graphical User Interface.
 * Hosts the home panel and the per-document review panel, swapping between them
 * with a CardLayout, and provides the global header (back, profile, logout).
 * @author Ian Leung
 * @version 3.0
 */

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MainDisplay {

    private static final Color BACKGROUND = new Color(150, 217, 136);
    private static final Color HEADER = new Color(120, 199, 104);
    private static final String HOME_CARD = "home";
    private static final String DOCUMENT_CARD = "document";

    private final JFrame frame;
    private final JPanel content;
    private final CardLayout cardLayout;
    private final HomePanel homePanel;
    private DocumentPanel documentPanel;
    private final JButton backButton;

    private final ArrayList<Document> documents;
    private final ArrayList<User> users;
    private final User user;
    private final Database db;
    private final Runnable onLogout;

    /**
     * Constructor for MainDisplay.
     *
     * @param user      The current user using the PeerAssist Platform.
     * @param documents The list of documents available on the platform.
     * @param users     The list of users registered on the platform.
     * @param db        The Supabase database client.
     * @param onLogout  Callback run after the user logs out (returns to login).
     */
    public MainDisplay(User user, ArrayList<Document> documents, ArrayList<User> users, Database db,
                       Runnable onLogout) {
        this.user = user;
        this.documents = documents;
        this.users = users;
        this.db = db;
        this.onLogout = onLogout;

        this.frame = new JFrame("PeerAssist");

        // Header: WEST back button, EAST profile + name + logout.
        this.backButton = new JButton("< Back");
        backButton.setBackground(new Color(59, 138, 51));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Helvetica", Font.BOLD, 20));
        backButton.setFocusPainted(false);
        backButton.setVisible(false);
        backButton.addActionListener(e -> backToHome());

        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);
        headerLeft.add(backButton, BorderLayout.WEST);

        JLabel profileLabel = new JLabel(scaledProfileIcon(44));
        profileLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JLabel nameLabel = new JLabel(user != null && user.getName() != null ? user.getName() : "User");
        nameLabel.setFont(new Font("Helvetica", Font.PLAIN, 22));
        nameLabel.setForeground(Color.BLACK);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBackground(new Color(199, 70, 70));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> confirmLogout());

        JPanel headerRight = new JPanel();
        headerRight.setOpaque(false);
        headerRight.add(profileLabel);
        headerRight.add(nameLabel);
        headerRight.add(logoutButton);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Content: home panel and document panel swapped via CardLayout.
        this.cardLayout = new CardLayout();
        this.content = new JPanel(cardLayout);
        this.content.setBackground(BACKGROUND);

        this.homePanel = new HomePanel(documents, this, user, db);
        content.add(homePanel, HOME_CARD);

        frame.setLayout(new BorderLayout());
        frame.add(header, BorderLayout.NORTH);
        frame.add(content, BorderLayout.CENTER);

        // Best-effort logout on window close, then exit.
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    db.logout();
                } finally {
                    db.close();
                    frame.dispose();
                    System.exit(0);
                }
            }
        });

        DisplayConst.enableFullScreen(frame);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(DisplayConst.size);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * setToDocumentPanel
     * Switches the content area to the document review panel for the given
     * document, showing the back button.
     *
     * @param document The document to be displayed.
     */
    public void setToDocumentPanel(Document document) {
        if (documentPanel != null) {
            content.remove(documentPanel);
        }
        documentPanel = new DocumentPanel(user, document, db);
        content.add(documentPanel, DOCUMENT_CARD);
        cardLayout.show(content, DOCUMENT_CARD);
        backButton.setVisible(true);
        content.revalidate();
        content.repaint();
    }

    /**
     * backToHome
     * Returns the content area to the home panel and hides the back button.
     */
    public void backToHome() {
        cardLayout.show(content, HOME_CARD);
        if (documentPanel != null) {
            content.remove(documentPanel);
            documentPanel = null;
        }
        backButton.setVisible(false);
        content.revalidate();
        content.repaint();
    }

    /**
     * confirmLogout
     * Confirms with the user, then logs out, disposes this frame and runs the
     * logout callback (which returns to the login screen).
     */
    private void confirmLogout() {
        int choice = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to log out?", "Log Out",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        db.logout();
        frame.dispose();
        if (onLogout != null) {
            onLogout.run();
        }
    }

    /**
     * scaledProfileIcon
     * Builds a square ImageIcon from the shared profile image, scaled to the
     * requested size. Safe even when the underlying image is the 1x1 fallback.
     *
     * @param size The width and height, in pixels.
     * @return The scaled icon.
     */
    private ImageIcon scaledProfileIcon(int size) {
        BufferedImage src = DisplayConst.profile;
        Image scaled = src.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
