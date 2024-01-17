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

public class Display {

    private JFrame frame;
    private JLayeredPane lframe;
    private JPanel gPanel = new JPanel();
    private JPanel bPanel = new JPanel();
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    public Display() {
        gPanel = new GridAreaPanel();
        gPanel.setBackground(new Color(25, 100, 23));
        gPanel.setBounds(0, 0, size.width, size.height);
        gPanel.setOpaque(true);
        bPanel = addButtons();
        bPanel.setBounds(0, 0, size.width, size.height);
        bPanel.setOpaque(false);

        lframe.add(gPanel, 0, 0);
        lframe.add(bPanel, 1, 0);

        frame.add(lframe);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size);
        frame.setVisible(true);
    }

    private JPanel addButtons() {
        JPanel buttonPanel = new JPanel();
        JButton button = new JButton("Example");

        button.setBounds(0, 0, 100, 50);
        button.setForeground(Color.white);
        button.setBackground(Color.black);
        button.setFocusPainted(false);
        button.addActionListener(e -> {
            //Action
        });

        buttonPanel.add(button);
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


            setDoubleBuffered(true);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setBackground(Color.white);
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
            // Restore the original state
            g2d.setTransform(originalTransform);
            g2d.setStroke(originalStroke);
            g2d.setColor(originalColor);
        }
    }
}
