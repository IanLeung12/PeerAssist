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
    JScrollPane scrollPane;
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



        gPanel = new GridAreaPanel();
        gPanel.setBackground(new Color(150, 217, 136));
        gPanel.setBounds(0, 0, size.width, size.height);
        gPanel.setOpaque(true);
        bPanel = DocumentPanel();
        scrollPane = new JScrollPane(bPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(50, 100, 750, 800);

        lframe.add(gPanel, 0, 0);
        lframe.add(scrollPane, 1, 0);

        frame.add(lframe);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size);
        frame.setVisible(true);
    }

    private JPanel DocumentPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(0,0, size.width, size.height);
        buttonPanel.setBackground(new Color(150, 217, 136));
        buttonPanel.setOpaque(true);
        ArrayList<DocumentButton> documentList = new ArrayList<>();
        for (int i = 0; i < documents.size(); i ++) {
            documentList.add(new DocumentButton(documents.get(i)));
            documentList.get(i).setBounds(100, 50 + 250 * i, 600, 200);
            buttonPanel.add(documentList.get(i));
        }
        JButton button = new JButton("Example");

        button.setBounds(0, 0, 100, 50);
        button.setForeground(Color.white);
        button.setBackground(Color.black);
        button.setFocusPainted(false);
        button.addActionListener(e -> {
            //Action
        });

        buttonPanel.add(button);
        buttonPanel.setPreferredSize(new Dimension(600, 300 + 250 * documents.size()));
        buttonPanel.setLayout(null);
        return buttonPanel;
    }
    /**
     * Image
     * Loads an image
     * @param path image location
     * @return the image
     */
    public BufferedImage image(String path){
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void drawBorderedString(String str, int x, int y, float size, float stroke, Graphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        try {
            g2d.translate(x, y);

            g2d.setColor(Color.black);
            FontRenderContext frc = g2d.getFontRenderContext();
            TextLayout tl = new TextLayout(str, g2d.getFont().deriveFont(size), frc);
            Shape shape = tl.getOutline(null);

            g2d.setStroke(new BasicStroke(stroke));
            g2d.draw(shape);

            g2d.setColor(Color.white);
            g2d.fill(shape);
        } finally {
            // Restore the ginal state
            g2d.setTransform(originalTransform);
            g2d.setStroke(originalStroke);
            g2d.setColor(originalColor);
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
