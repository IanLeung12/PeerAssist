import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainDisplay {

    private JFrame frame;
    private JLayeredPane lframe;
    private JPanel gPanel = new JPanel();
    private JPanel bPanel = new JPanel();
    ArrayList<Document> documents;
    private User user;

    public MainDisplay(User user, ArrayList<Document> documents) {
        this.user = user;
        this.documents = documents;

        this.frame = new JFrame("PeerAssist");
        lframe = new JLayeredPane();


        HomePanel homePanel = new HomePanel(documents);
        DocumentPanel reviewPanel = new DocumentPanel(user, documents.get(3));

        lframe.add(reviewPanel, 1);

        DrawPanel drawPanel = new DrawPanel();
        drawPanel.setSize(DisplayConst.size);
        drawPanel.setBackground(new Color(0,0,0,0));
        drawPanel.setVisible(true);

        lframe.add(drawPanel, 0);


        frame.add(lframe);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(DisplayConst.size);
        frame.setVisible(true);
    }

    public void refresh() {
        SwingUtilities.invokeLater(() ->frame.repaint());
    }



    private class DrawPanel extends JPanel {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            setDoubleBuffered(true);


            g2d.drawImage(DisplayConst.profile, DisplayConst.size.width - 100, 20, 80, 80, null);


        }
    }

    public static void main(String[] args) {
        ArrayList<Document> documents = new ArrayList<>();
        documents.add(new Document("Pictures/Proposal.pdf", 100, 12));
        documents.add(new Document("Pictures/Mario Essay.pdf", 100, 7));
        documents.add(new Document("Pictures/DECA PSE Presentation.pdf", 100, 11));
        documents.add(new Document("Pictures/R Presentation.pdf", 100, 12));
        MainDisplay md = new MainDisplay(null, documents);
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
