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
    private JPanel gPanel;
    private JPanel iPanel = new JPanel();
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    public Display() {
        this.frame = new JFrame("Display");
        lframe = new JLayeredPane();
        lframe.setBounds(0, 0, size.width, size.height);

        gPanel = new DrawPanel();
        gPanel.setBackground(new Color(25, 100, 23));
        gPanel.setBounds(0, 0, size.width, size.height);
        gPanel.setOpaque(true);
        iPanel = addInterface();
        iPanel.setBounds(0, 0, size.width, size.height);
        iPanel.setOpaque(false);

        lframe.add(gPanel, 0, 0);
        lframe.add(iPanel, 1, 0);

        frame.add(lframe);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size);
        frame.setVisible(true);
    }

    private JPanel addInterface() {
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
        frame.repaint();

    }

    class DrawPanel extends JPanel {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            setDoubleBuffered(true);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setBackground(Color.white);
        }
    }

}
