import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainDisplay {

    private JFrame frame;
    private JLayeredPane lframe;
    private HomePanel homePanel;
    private DocumentPanel documentPanel;
    private JButton backButton;
    ArrayList<Document> documents;
    private User user;

    public MainDisplay(User user, ArrayList<Document> documents) {
        this.user = user;
        this.documents = documents;

        this.frame = new JFrame("PeerAssist");
        lframe = new JLayeredPane();


        homePanel = new HomePanel(documents, this);

        lframe.add(homePanel, 1);

        DrawPanel drawPanel = new DrawPanel();
        drawPanel.setSize(DisplayConst.size);
        drawPanel.setBackground(new Color(0,0,0,0));
        drawPanel.setVisible(true);

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

    public void setToDocumentPanel(Document document) {
        homePanel.setVisible(false);
        documentPanel = new DocumentPanel(user, document);
        backButton.setVisible(true);
        lframe.add(documentPanel, 1);
    }

    public void backToHome() {
        homePanel.setVisible(true);
        lframe.remove(documentPanel);
        backButton.setVisible(false);
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

    public static void main(String[] args) {
        ArrayList<Document> documents = new ArrayList<>();
        documents.add(new Document("Pictures/Proposal.pdf", 100, 12));
        documents.add(new Document("Pictures/Mario Essay.pdf", 100, 7));
        documents.add(new Document("Pictures/DECA PSE Presentation.pdf", 100, 11));
        documents.add(new Document("Pictures/R Presentation.pdf", 100, 12));
        MainDisplay md = new MainDisplay(new User(1, "Ian Leung", 11, "ian@gmail.com", "abcdef123", null), documents);
        while (true) {
            md.refresh();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
