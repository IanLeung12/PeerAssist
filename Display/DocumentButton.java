import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DocumentButton extends JButton {

    private static final int ARC_WIDTH = 15;
    private static final int ARC_HEIGHT = 15;

    public DocumentButton(Document document) {
        super("");
        setContentAreaFilled(false); // Make the button transparent
        setFocusPainted(false); // Remove the focus rectangle around the text
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 50, 50);

        g2.setStroke(new BasicStroke(5f));
        g2.setColor(getBackground());
        g2.fill(roundedRectangle);

        g2.setColor(getForeground());
        g2.draw(roundedRectangle);

        g2.dispose();

        super.paintComponent(g);
    }
}