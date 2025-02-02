package org.example; /**
 * [DocumentButton.java]
 * Button that previews and lets users select documents
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import javax.swing.*;
import java.awt.*;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class DocumentButton extends JButton {
    private Document document;

    /**
     * DocumentButton constructor
     * Creates a DocumentButton with the specified document.
     * @param document The document associated with the button
     */
    public DocumentButton(Document document) {
        super("");
        this.document = document;
        BufferedImage profile = DisplayConst.profile;
        setContentAreaFilled(false); // Make the button transparent
        setFocusPainted(false); // Remove the focus rectangle around the tex
        setBorderPainted(false);
        setBackground(new Color(71, 173, 39, 207));
        setForeground(new Color(240, 240, 248));
        addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(57, 134, 32, 207));
                setForeground(new Color(197, 197, 203));
            }

            /**
             * {@inheritDoc}
             *
             * @param e
             */
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

        int width = getWidth();
        int height = getHeight();

        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 50, 50);

        g2.setStroke(new BasicStroke(5f));
        g2.setColor(getBackground());
        g2.fill(roundedRectangle);

        g2.setColor(getForeground());
        g2.draw(roundedRectangle);
        g2.setFont(new Font("Helvetica", Font.BOLD, 36));

        g2.drawString(document.getName(), 20, 50);

        // Publisher
        g2.drawImage(DisplayConst.profile, 20, 65, 30, 30, null);
        g2.setFont(new Font("Helvetica", Font.PLAIN, 24));
        g2.drawString(document.getUser().getName(), 60 , 90);
        g2.drawString("Grade: " + document.getGradeLevel(), 20, 130);
        g2.drawString(document.getReviews().size() + " Reviews", 20, this.getHeight() - 20);

        // Mark
        double average = document.getAvgMark()/document.getMaxMark();
        g2.setColor(new Color((int) (255 - average*255), 15, (int) (average * 255)));
        g2.setFont(new Font("Helvetica", Font.BOLD, 52));
        g2.drawString(Math.round(average*1000.0)/10.0 + "%", this.getWidth() - 165, this.getHeight() - 20);

        int topics = 0;

        // Topics
        g2.setFont(new Font("Helvetica", Font.PLAIN, 18));
        for (String topic: document.getTopics()) {
            g2.setColor(new Color(47, 114, 27, 207));
            g2.fillRoundRect(155 + 205 * topics - 410 * (topics/2), 60 + 40 * (topics/2),
                    190, 30, 10, 15);
            g2.setColor(Color.white);
            if (topics < 3) {
                g2.drawString(topic, 160 + 205 * topics - 410 * (topics/2),  80 + 40 * (topics/2));
            } else {
                g2.drawString("More...", 160 + 205 * topics - 410 * (topics/2),  80 + 40 * (topics/2));
                break;
            }
            topics ++;


        }

        g2.dispose();

        super.paintComponent(g);
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
