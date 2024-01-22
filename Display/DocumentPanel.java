import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.Graphics;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DocumentPanel extends JLayeredPane {

    private Document document;
    private PDDocument pdf;
    private User user;
    int pageNum;
    int pages;

    DocumentPanel(User user, Document document) {
        this.document = document;
        this.user = user;
        this.pageNum = 0;
        pages = document.getDocument().getNumberOfPages();

        pdf = document.getDocument();


        GraphicsPanel graphicsPanel = new GraphicsPanel();

        PdfPanel documentPanel = new PdfPanel(pdf);
        JScrollPane documentScrollPane = new JScrollPane(documentPanel);
        documentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        documentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        documentScrollPane.getVerticalScrollBar().setUnitIncrement(22);
        documentScrollPane.setBounds(50, 100, 1020, 900);


        ReviewPanel reviewPanel = new ReviewPanel();
        JScrollPane reviewScrollPane = new JScrollPane(reviewPanel);
        reviewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        reviewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        reviewScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        reviewScrollPane.setBounds(1100, 380 + 40 * (document.getTopics().size()/3), 600, 500);

        add(graphicsPanel, 1);
        add(documentScrollPane, 0);
        add(reviewScrollPane, 0);
        setBackground(new Color(150, 217, 136));
        setSize(DisplayConst.size);
        setVisible(true);
    }

    private class GraphicsPanel extends JPanel {
        GraphicsPanel() {
            setBackground(new Color(150, 217, 136));
            setSize(DisplayConst.size);
            setVisible(true);

        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            setDoubleBuffered(true);

            g2d.drawImage(DisplayConst.logo, 920, 0, null);

            g.setColor(Color.black);
            g.setFont(new Font("Helvetica", Font.BOLD, 52));
            g.drawString(document.getName(), 1100, 150);

            g2d.drawImage(DisplayConst.profile, 1100, 165, 50, 50, null);
            g.setFont(new Font("Helvetica", Font.PLAIN, 36));
            g.drawString("Bobby", 1170, 205);
            g.setFont(new Font("Helvetica", Font.PLAIN, 28));
            g.drawString("Grade: " + document.getGradeLevel(), 1100, 245);

            g2d.setFont(new Font("Helvetica", Font.PLAIN, 18));
            for (int i = 0; i < document.getTopics().size(); i ++) {
                g2d.setColor(new Color(47, 114, 27, 207));
                g2d.fillRoundRect(1100 + 205 * i - 615 * (i/3), 260 + 40 * (i/3),
                        190, 30, 10, 15);
                g2d.setColor(Color.white);
                g2d.drawString(document.getTopics().get(i), 1110 + 205 * i - 615 * (i/3),  280 + 40 * (i/3));
            }

            g.setFont(new Font("Helvetica", Font.BOLD, 42));
            double average = document.getAvgMark()/document.getMaxMark();
            g.setColor(new Color((int) (255 - average*255), 15, (int) (average * 255)));
            g.drawString("Average Mark:                  "  + Math.round(average*1000.0)/10.0 + "%",
                    1100, 340 + 40 * (document.getTopics().size()/3));

        }
    }


    private class PdfPanel extends JPanel{

        BufferedImage[] pdfPages;
        private PDFRenderer pdfRenderer;


        PdfPanel(PDDocument pdf) {
            setBackground(new Color(85, 121, 97));

            pdfRenderer = new PDFRenderer(pdf);
            pdfPages = new BufferedImage[pages];

            try {
                int totalHeight = 0;
                for (int i = 0; i < pages; i ++) {
                    pdfPages[i] = pdfRenderer.renderImage(i);
                    totalHeight += 50 + pdfPages[i].getHeight();
                }
                setPreferredSize(new Dimension(1000, totalHeight + 50));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            setDoubleBuffered(true);

            for (int i = 0; i < pages; i ++) {

                g2d.drawImage(pdfPages[i], 500 - pdfPages[i].getWidth()/2, 40 + i * (50 + pdfPages[i].getHeight()),null);
            }


        }


    }

    private class ReviewPanel extends JPanel {
        ReviewPanel() {
            setBackground(new Color(150, 217, 136));
            ArrayList<commentBox> commentBoxes = new ArrayList<>();

            JButton reviewButton = new JButton("Add a review");
            reviewButton.setBounds(150, 20, 300, 75);
            reviewButton.setBackground(new Color(24, 134, 14, 178));
            reviewButton.setBorderPainted(false);

            JTextField commentField = new CustomTextField("Add a comment");
            commentField.setBounds(20, 50, 560, 30);
            commentField.setFont(new Font("Helvetica", Font.PLAIN, 18));
            commentField.setBackground(new Color(165, 236, 150));
            commentField.setVisible(false);

            JTextField markField = createNumericTextField("Mark (Out of " + document.getMaxMark() + ")");
            markField.setBounds(20, 100, 250, 30);
            markField.setFont(new Font("Helvetica", Font.PLAIN, 18));
            markField.setBackground(new Color(165, 236, 150));
            markField.setVisible(false);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBounds(300, 100, 100, 30);
            cancelButton.setBackground(new Color(236, 36, 36, 178));
            cancelButton.setBorderPainted(false);
            cancelButton.setVisible(false);

            JButton postButton = new JButton("Post Review");
            postButton.setBounds(430, 100, 150, 30);
            postButton.setBackground(new Color(74, 159, 21, 178));
            postButton.setBorderPainted(false);
            postButton.setVisible(false);

            reviewButton.addActionListener(e -> {
                commentField.setVisible(true);
                markField.setVisible(true);
                cancelButton.setVisible(true);
                postButton.setVisible(true);
                reviewButton.setVisible(false);
            });

            cancelButton.addActionListener(e -> {
                commentField.setText("");
                commentField.setVisible(false);
                markField.setText("");
                markField.setVisible(false);
                cancelButton.setVisible(false);
                postButton.setVisible(false);
                reviewButton.setVisible(true);
            });

            final int[] currentY = {0};
            postButton.addActionListener(e -> {
                String comment = commentField.getText();
                commentField.setText("");
                commentField.setVisible(false);

                double mark = 0;
                try {
                    mark = Double.parseDouble(markField.getText());
                    if (mark > document.getMaxMark()) {
                        mark = document.getMaxMark();
                    } else if (mark < 0) {
                        mark = 0;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                markField.setText("");
                reviewButton.setVisible(true);
                markField.setVisible(false);
                cancelButton.setVisible(false);
                postButton.setVisible(false);

                Review newReview = new Review(user, mark, comment);
                document.addReview(newReview);
                commentBox commentBox = new commentBox(newReview);
                commentBox.setBounds(20, 150 + currentY[0], 565, commentBox.getHeight() + 5);
                currentY[0] += commentBox.getHeight() + 20;
                commentBoxes.add(commentBox);
                add(commentBox);
                setPreferredSize(new Dimension(600, 200 + currentY[0]));
            });


            for (Review review: document.getReviews()) {
                commentBox commentBox = new commentBox(review);
                commentBox.setBounds(20, 150 + currentY[0], 565, commentBox.getHeight() + 5);
                currentY[0] += commentBox.getHeight() + 20;
                commentBoxes.add(commentBox);
                add(commentBox);
                setPreferredSize(new Dimension(600, 200 + currentY[0]));
            }

            add(reviewButton);
            add(commentField);
            add(markField);
            add(cancelButton);
            add(postButton);
            setLayout(null);
        }

        private JTextField createNumericTextField(String text) {
            JTextField textField = new CustomTextField(text);
            AbstractDocument document = (AbstractDocument) textField.getDocument();
            document.setDocumentFilter(new NumericFilter());
            return textField;
        }

        private class NumericFilter extends DocumentFilter {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (isNumeric(string)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (isNumeric(text)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean isNumeric(String text) {
                return text.matches("\\d*");
            }
        }

        private class commentBox extends JPanel {

            Review review;
            ArrayList<String> lines;

            commentBox(Review review) {
                this.review = review;

                lines = new ArrayList<>();
                setBackground(new Color(0,0,0,0));
                String[] words = review.getComments().split("\\s+");
                StringBuilder currentLine = new StringBuilder();

                for (String word : words) {
                    if (currentLine.length() + word.length() <= 50) {
                        currentLine.append(word).append(" ");
                    } else {
                        lines.add(currentLine.toString().trim());
                        currentLine = new StringBuilder(word + " ");
                    }
                }

                lines.add(currentLine.toString().trim());

                setSize(560, 50 + lines.size() * 25);
                setVisible(true);

            }

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, 560, this.getHeight() - 2, 50, 50);


                g2d.setColor(new Color(111, 183, 94, 203));
                g2d.fill(roundedRectangle);

                g2d.setColor(Color.black);
                g2d.draw(roundedRectangle);

                g2d.drawImage(DisplayConst.profile, 10, 10, 30, 30, null);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 24));
                g2d.drawString("Bobby", 50 , 35);

                g2d.setFont(new Font("Helvetica", Font.PLAIN, 18));
                for (int i = 0; i < lines.size(); i ++) {
                    g2d.drawString(lines.get(i), 20, 65 + 25 * i);
                }

                double percent = review.getMark()/document.getMaxMark();
                g2d.setColor(new Color((int) (255 - percent*255), 15, (int) (percent * 255)));
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 24));
                g2d.drawString(Math.round(percent*1000.0)/10.0 + "%", 470, 30);

            }

        }

    }
}
