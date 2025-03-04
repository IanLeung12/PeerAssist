package org.example; /**
 * [MainDisplay.java]
 * Display frame class that is in charge of the main Graphical User Interface
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.PrintWriter;

public class MainDisplay {

    private JFrame frame;
    private JLayeredPane lframe;
    private HomePanel homePanel;
    private DocumentPanel documentPanel;
    private JButton backButton;
    ArrayList<Document> documents;
    ArrayList<User> users;
    private User user;
    private MongoDB db;

    /**
     * Constructor for MainDisplay.
     *
     * @param user      The current user using the PeerAssist Platform.
     * @param documents The list of documents available on the platform.
     * @param users     The list of users registered on the platform.
     */
    public MainDisplay(User user, ArrayList<Document> documents, ArrayList<User> users, MongoDB db) {
        this.user = user;
        this.documents = documents;
        this.users = users;
        this.db = db;
        this.frame = new JFrame("PeerAssist");
        lframe = new JLayeredPane();


        homePanel = new HomePanel(documents, this,  user, db);

        lframe.add(homePanel, 1);

        DrawPanel drawPanel = new DrawPanel();
        drawPanel.setSize(DisplayConst.size);
        drawPanel.setBackground(new Color(0,0,0,0));
        drawPanel.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                db.close();
                super.windowClosing(e);
            }
        });

        lframe.add(drawPanel, 0);
        lframe.setLayout(null);

        frame.add(lframe);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(DisplayConst.size);
        frame.setVisible(true);
    }

    public void refresh()    {
        SwingUtilities.invokeLater(() ->frame.repaint());
    }

    /**
     * Goes to the document display/ reviewing panel.
     *
     * @param document The document to be displayed.
     */
    public void setToDocumentPanel(Document document) {
        homePanel.setVisible(false);
        documentPanel = new DocumentPanel(user, document, db);
        backButton.setVisible(true);
        lframe.add(documentPanel, 1);
    }

    /**
     * Returns to the home panel.
     */
    public void backToHome() {
        homePanel.setVisible(true);
        lframe.remove(documentPanel);
        backButton.setVisible(false);
        homePanel.getDocumentsCopy().addAll(documents);
    }

    private class DrawPanel extends JPanel {

        DrawPanel() {
            backButton = new JButton("< Back");
            backButton.setBackground(new Color(59, 138, 51, 216));
            backButton.setBorderPainted(false);
            backButton.setBounds(50, 20, 250, 60);
            backButton.setForeground(Color.WHITE);
            backButton.setFont(new Font("Helvetica", Font.BOLD, 48));
            backButton.addActionListener(e -> {
                backToHome();
            });
            backButton.setVisible(false);
            add(backButton);
            setLayout(null);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            setDoubleBuffered(true);

            // Things drawn here are always visible (on main panel)
            g2d.drawImage(DisplayConst.profile, DisplayConst.size.width - 100, 20, 80, 80, null);
            g.setColor(Color.black);
            g.setFont(new Font("Helvetica", Font.PLAIN, 36));
            drawRightAlignedString(g, user.getName(), DisplayConst.size.width - 120,  70);

        }

        private void drawRightAlignedString(Graphics g, String text, int x, int y) {
            FontMetrics fontMetrics = g.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(text);
            int xCoordinate = x - textWidth;
            g.drawString(text, xCoordinate, y);
        }
    }
}
