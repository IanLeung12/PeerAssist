package org.example; /**
 * [DocumentButton.java]
 * Button that previews and lets users select documents. Renders relative to its
 * actual size so it works at any width assigned by the surrounding layout.
 * @author Ian Leung
 * @version 3.0
 */

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

public class DocumentButton extends JButton {

    private static final Font TITLE_FONT = new Font("Helvetica", Font.BOLD, 30);
    private static final Font BODY_FONT = new Font("Helvetica", Font.PLAIN, 20);
    private static final Font CHIP_FONT = new Font("Helvetica", Font.PLAIN, 16);
    private static final Font MARK_FONT = new Font("Helvetica", Font.BOLD, 44);

    private final Document document;

    /**
     * DocumentButton constructor
     * Creates a DocumentButton for the given document.
     * @param document The document associated with the button
     */
    public DocumentButton(Document document) {
        super("");
        this.document = document;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setBackground(new Color(71, 173, 39, 207));
        setForeground(new Color(240, 240, 248));
        setPreferredSize(new Dimension(640, 200));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(57, 134, 32, 207));
                setForeground(new Color(197, 197, 203));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(new Color(71, 173, 39, 207));
                setForeground(new Color(240, 240, 248));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        RoundRectangle2D card = new RoundRectangle2D.Float(2, 2, width - 5, height - 5, 40, 40);
        g2.setColor(getBackground());
        g2.fill(card);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(getForeground());
        g2.draw(card);

        String name = document != null && document.getName() != null ? document.getName() : "Untitled";
        String author = document != null && document.getUser() != null && document.getUser().getName() != null
                ? document.getUser().getName() : "Unknown";
        int grade = document != null ? document.getGradeLevel() : 0;
        ArrayList<Review> reviews = document != null && document.getReviews() != null
                ? document.getReviews() : new ArrayList<Review>();
        ArrayList<String> topics = document != null && document.getTopics() != null
                ? document.getTopics() : new ArrayList<String>();

        g2.setColor(getForeground());
        g2.setFont(TITLE_FONT);
        g2.drawString(name, 24, 46);

        g2.drawImage(DisplayConst.profile, 24, 62, 28, 28, null);
        g2.setFont(BODY_FONT);
        g2.drawString(author, 60, 84);
        g2.drawString("Grade: " + grade, 24, 116);
        g2.drawString(reviews.size() + " Reviews", 24, height - 20);

        // Average mark (colour shifts blue->red with score), bottom-right.
        double average = document != null ? document.avgPercent() : 0;
        int red = clamp((int) (255 - average * 255));
        int blue = clamp((int) (average * 255));
        String markText = (Math.round(average * 1000.0) / 10.0) + "%";
        g2.setFont(MARK_FONT);
        g2.setColor(new Color(red, 15, blue));
        int markWidth = g2.getFontMetrics().stringWidth(markText);
        g2.drawString(markText, width - markWidth - 28, height - 18);

        // Topic chips along the top-right; truncate with "More…" when they run out of room.
        g2.setFont(CHIP_FONT);
        int chipW = 132;
        int gap = 8;
        int startX = Math.max(width / 2, 320);
        int chipY = 60;
        for (int i = 0; i < topics.size(); i++) {
            int x = startX + i * (chipW + gap);
            if (x + chipW > width - 24) {
                g2.setColor(new Color(47, 114, 27, 207));
                g2.fillRoundRect(x, chipY, chipW, 28, 12, 12);
                g2.setColor(Color.WHITE);
                g2.drawString("More…", x + 12, chipY + 19);
                break;
            }
            g2.setColor(new Color(47, 114, 27, 207));
            g2.fillRoundRect(x, chipY, chipW, 28, 12, 12);
            g2.setColor(Color.WHITE);
            g2.drawString(trim(topics.get(i), 14), x + 10, chipY + 19);
        }

        g2.dispose();
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static String trim(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    /**
     * getDocument
     * Returns the document associated with the button.
     * @return Document The document associated with the button
     */
    public Document getDocument() {
        return document;
    }
}
