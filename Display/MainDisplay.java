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
    private PDDocument pdf;
    private PDFRenderer pdfRenderer;
    private User user;
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    public MainDisplay(User user, ArrayList<Document> documents) {
        this.user = user;
        this.documents = documents;

        this.frame = new JFrame("Main GUI");
        lframe = new JLayeredPane();
        lframe.setBounds(0, 0, size.width, size.height);

        try {
            pdf = PDDocument.load(new File("Pictures/Mario Essay.pdf"));
            pdfRenderer = new PDFRenderer(pdf);
        } catch (Exception e) {
            e.printStackTrace();
        }


        HomePanel homePanel = new HomePanel(documents);
        homePanel.setBackground(new Color(150, 217, 136));
        homePanel.setBounds(0, 0, size.width, size.height);
        homePanel.setOpaque(true);


        lframe.add(homePanel, 0, 0);


        frame.add(lframe);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size);
        frame.setVisible(true);
    }

    public void refresh() {
        SwingUtilities.invokeLater(() ->frame.repaint());
    }



    class GridAreaPanel extends JPanel {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            setDoubleBuffered(true);




            if (pdfRenderer != null) {
                BufferedImage image = null;
                try {
                    image = pdfRenderer.renderImage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                g2d.drawImage(image, 900, 100, null);
            }


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
