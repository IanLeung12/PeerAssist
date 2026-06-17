package org.example; /**
 * [DocumentPanel.java]
 * Panel that displays the review page for a document. The PDF is shown in a
 * scrollable centre area; the reviews (and the form for adding a new one) are
 * shown in a scrollable side area. Everything uses real layout managers so the
 * page reflows with the window size.
 * @author Ian Leung
 * @version 3.0
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DocumentPanel extends JPanel {

    private static final Color BACKGROUND = new Color(150, 217, 136);
    private static final Color PDF_BACKGROUND = new Color(85, 121, 97);
    private static final int MAX_COMMENT_LENGTH = 500;

    private final Document document;
    private final PDDocument pdf;
    private final User user;
    private final Database db;
    private final int pages;

    private ReviewPanel reviewPanel;

    /**
     * DocumentPanel
     * Constructs a DocumentPanel for the given user and document, building a
     * NORTH header, a CENTER PDF view and an EAST review view.
     * @param user The user viewing the document
     * @param document The document to be displayed
     * @param db The Supabase database client
     */
    DocumentPanel(User user, Document document, Database db) {
        this.db = db;
        this.document = document;
        this.user = user;
        this.pdf = document.getDocument();
        this.pages = pdf.getNumberOfPages();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);

        PdfPanel pdfPanel = new PdfPanel();
        JScrollPane pdfScroll = new JScrollPane(pdfPanel);
        pdfScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pdfScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pdfScroll.getVerticalScrollBar().setUnitIncrement(22);
        pdfScroll.setBorder(BorderFactory.createEmptyBorder());

        reviewPanel = new ReviewPanel();
        JScrollPane reviewScroll = new JScrollPane(reviewPanel);
        reviewScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        reviewScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        reviewScroll.getVerticalScrollBar().setUnitIncrement(14);
        reviewScroll.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pdfScroll, reviewScroll);
        split.setResizeWeight(0.62);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(BACKGROUND);
        // EAST review area ~38% of the width.
        split.setDividerLocation(0.62);
        add(split, BorderLayout.CENTER);
    }

    /**
     * buildHeader
     * Builds the NORTH header from real JLabels: title, author (profile + name),
     * grade, topic badges and the average mark. No absolute drawString.
     * @return the header panel
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(BACKGROUND);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(16, 24, 12, 24));

        JLabel title = new JLabel(safe(document.getName(), "Untitled"));
        title.setFont(new Font("Helvetica", Font.BOLD, 40));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(8));

        // Author row: profile icon + name.
        JPanel authorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        authorRow.setOpaque(false);
        authorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorRow.add(new JLabel(scaledIcon(DisplayConst.profile, 40)));
        String authorName = (document.getUser() != null && document.getUser().getName() != null)
                ? document.getUser().getName() : "Unknown";
        JLabel author = new JLabel(authorName);
        author.setFont(new Font("Helvetica", Font.PLAIN, 28));
        authorRow.add(author);
        header.add(authorRow);

        JLabel grade = new JLabel("Grade: " + document.getGradeLevel());
        grade.setFont(new Font("Helvetica", Font.PLAIN, 22));
        grade.setForeground(Color.BLACK);
        grade.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(grade);
        header.add(Box.createVerticalStrut(8));

        // Topic badges in a wrapping row.
        ArrayList<String> topics = document.getTopics();
        if (topics != null && !topics.isEmpty()) {
            JPanel topicsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
            topicsPanel.setOpaque(false);
            topicsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String topic : topics) {
                topicsPanel.add(new Badge(topic));
            }
            header.add(topicsPanel);
        }

        header.add(Box.createVerticalStrut(8));

        double percent = document.avgPercent();
        JLabel average = new JLabel("Average Mark: " + Math.round(percent * 1000.0) / 10.0 + "%");
        average.setFont(new Font("Helvetica", Font.BOLD, 30));
        average.setForeground(new Color(clamp((int) (255 - percent * 255)), 15, clamp((int) (percent * 255))));
        average.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(average);

        return header;
    }

    /**
     * Badge
     * Small rounded label used to display a topic tag.
     */
    private static class Badge extends JLabel {
        Badge(String text) {
            super(text);
            setForeground(Color.WHITE);
            setFont(new Font("Helvetica", Font.PLAIN, 16));
            setOpaque(false);
            setBorder(new EmptyBorder(5, 12, 5, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(47, 114, 27, 207));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * scaledIcon
     * Builds a square ImageIcon scaled from the given image. Safe even when the
     * source is the 1x1 fallback image.
     * @param src the source image
     * @param size the width and height in pixels
     * @return the scaled icon
     */
    private static ImageIcon scaledIcon(BufferedImage src, int size) {
        Image scaled = src.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * clamp
     * Clamps a colour component into the legal 0..255 range.
     */
    private static int clamp(int v) {
        if (v < 0) {
            return 0;
        }
        return Math.min(v, 255);
    }

    /**
     * safe
     * Returns the given string, or a default when it is null or blank.
     */
    private static String safe(String s, String def) {
        return (s == null || s.trim().isEmpty()) ? def : s;
    }

    /**
     * PdfPanel
     * Renders the pages of the PDF, centring each page horizontally within the
     * available width. Rendering failures surface a user-facing message rather
     * than a silent stack trace.
     */
    private class PdfPanel extends JPanel {

        private final BufferedImage[] pdfPages;
        private int maxPageWidth = 1;
        private int totalHeight = 50;
        private boolean renderFailed = false;

        PdfPanel() {
            setBackground(PDF_BACKGROUND);
            pdfPages = new BufferedImage[pages];

            try {
                PDFRenderer renderer = new PDFRenderer(pdf);
                int height = 0;
                for (int i = 0; i < pages; i++) {
                    pdfPages[i] = renderer.renderImage(i);
                    height += 50 + pdfPages[i].getHeight();
                    maxPageWidth = Math.max(maxPageWidth, pdfPages[i].getWidth());
                }
                totalHeight = height + 50;
            } catch (Exception e) {
                renderFailed = true;
                totalHeight = 200;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            // Track the viewport width so pages stay centred; ensure the page
            // itself always fits.
            int viewportWidth = (getParent() != null) ? getParent().getWidth() : 0;
            int width = Math.max(maxPageWidth + 40, viewportWidth);
            return new Dimension(width, totalHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            if (renderFailed) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 20));
                g2d.drawString("This document could not be displayed.", 30, 60);
                return;
            }

            int y = 40;
            for (int i = 0; i < pages; i++) {
                BufferedImage page = pdfPages[i];
                if (page == null) {
                    continue;
                }
                int x = (getWidth() - page.getWidth()) / 2;
                if (x < 0) {
                    x = 0;
                }
                g2d.drawImage(page, x, y, null);
                y += 50 + page.getHeight();
            }
        }
    }

    /**
     * ReviewPanel
     * Vertical list of reviews with a form, at the top, for adding a new review.
     */
    private class ReviewPanel extends JPanel {

        private final JButton reviewButton;
        private final JTextArea commentField;
        private final JScrollPane commentScroll;
        private final JTextField markField;
        private final JButton cancelButton;
        private final JButton postButton;
        private final JPanel formPanel;
        private final JPanel listPanel;

        ReviewPanel() {
            setBackground(BACKGROUND);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(20, 16, 20, 16));

            reviewButton = new JButton("Add a review");
            reviewButton.setBackground(new Color(24, 134, 14));
            reviewButton.setForeground(Color.WHITE);
            reviewButton.setFont(new Font("Helvetica", Font.BOLD, 20));
            reviewButton.setFocusPainted(false);
            reviewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            reviewButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            commentField = new JTextArea(4, 20);
            commentField.setLineWrap(true);
            commentField.setWrapStyleWord(true);
            commentField.setFont(new Font("Helvetica", Font.PLAIN, 18));
            commentField.setBackground(new Color(165, 236, 150));
            ((AbstractDocument) commentField.getDocument()).setDocumentFilter(new LengthFilter(MAX_COMMENT_LENGTH));
            commentScroll = new JScrollPane(commentField);
            commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            commentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            markField = createNumericTextField("Mark (Out of " + trimMark(document.getMaxMark()) + ")");
            markField.setFont(new Font("Helvetica", Font.PLAIN, 18));
            markField.setBackground(new Color(165, 236, 150));
            markField.setAlignmentX(Component.LEFT_ALIGNMENT);
            markField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

            cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(199, 70, 70));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFocusPainted(false);

            postButton = new JButton("Post Review");
            postButton.setBackground(new Color(74, 159, 21));
            postButton.setForeground(Color.WHITE);
            postButton.setFocusPainted(false);

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            buttonRow.setOpaque(false);
            buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            buttonRow.add(cancelButton);
            buttonRow.add(postButton);

            formPanel = new JPanel();
            formPanel.setOpaque(false);
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            formPanel.add(commentScroll);
            formPanel.add(Box.createVerticalStrut(8));
            formPanel.add(markField);
            formPanel.add(Box.createVerticalStrut(8));
            formPanel.add(buttonRow);
            formPanel.setVisible(false);

            reviewButton.addActionListener(e -> showForm(true));
            cancelButton.addActionListener(e -> {
                commentField.setText("");
                markField.setText("");
                showForm(false);
            });
            postButton.addActionListener(e -> postReview());

            listPanel = new JPanel();
            listPanel.setOpaque(false);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (Review review : document.getReviews()) {
                addCommentBox(review);
            }

            add(reviewButton);
            add(Box.createVerticalStrut(10));
            add(formPanel);
            add(Box.createVerticalStrut(14));
            add(listPanel);
            add(Box.createVerticalGlue());
        }

        /**
         * showForm
         * Toggles between the "add a review" button and the review form.
         * @param visible true to show the form, false to show the button
         */
        private void showForm(boolean visible) {
            formPanel.setVisible(visible);
            reviewButton.setVisible(!visible);
            revalidate();
            repaint();
        }

        /**
         * postReview
         * Validates the form, then posts the review on a background worker so the
         * UI never blocks. The Post button is disabled while the request runs.
         */
        private void postReview() {
            final String comment = commentField.getText() == null ? "" : commentField.getText().trim();

            String markText = markField.getText() == null ? "" : markField.getText().trim();
            double parsed;
            if (markText.isEmpty()) {
                parsed = 0;
            } else {
                try {
                    parsed = Double.parseDouble(markText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(DocumentPanel.this,
                            "Please enter a valid numeric mark.", "Invalid mark",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (parsed < 0) {
                    parsed = 0;
                } else if (parsed > document.getMaxMark()) {
                    parsed = document.getMaxMark();
                }
            }
            final double mark = parsed;
            final Review newReview = new Review(user, mark, comment);

            postButton.setEnabled(false);
            cancelButton.setEnabled(false);
            final String original = postButton.getText();
            postButton.setText("Posting...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    db.addReview(document, newReview);
                    return null;
                }

                @Override
                protected void done() {
                    postButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    postButton.setText(original);
                    try {
                        get();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(DocumentPanel.this,
                                "Could not post your review. Please try again.",
                                "PeerAssist", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    document.addReview(newReview);
                    commentField.setText("");
                    markField.setText("");
                    showForm(false);
                    addCommentBox(newReview);
                    listPanel.revalidate();
                    listPanel.repaint();
                }
            }.execute();
        }

        /**
         * addCommentBox
         * Appends a comment box for the given review to the list.
         * @param review the review to display
         */
        private void addCommentBox(Review review) {
            CommentBox box = new CommentBox(review);
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(box);
            listPanel.add(Box.createVerticalStrut(12));
        }

        /**
         * removeCommentBox
         * Removes the given comment box (and its trailing strut) from the list,
         * used after a successful delete of the user's own review.
         * @param box the box to remove
         */
        private void removeCommentBox(CommentBox box) {
            int index = -1;
            Component[] children = listPanel.getComponents();
            for (int i = 0; i < children.length; i++) {
                if (children[i] == box) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                return;
            }
            listPanel.remove(box);
            // Remove the trailing strut that followed the box, if present.
            if (index < listPanel.getComponentCount()) {
                listPanel.remove(index);
            }
            listPanel.revalidate();
            listPanel.repaint();
        }

        /**
         * createNumericTextField
         * Creates a text field that only accepts a non-negative decimal number.
         * @param text placeholder text
         * @return the numeric text field
         */
        private JTextField createNumericTextField(String text) {
            JTextField field = new CustomTextField(text);
            AbstractDocument doc = (AbstractDocument) field.getDocument();
            doc.setDocumentFilter(new NumericFilter());
            return field;
        }

        /**
         * CommentBox
         * Displays a single review: author (profile + name), the comment as a
         * wrapping JTextArea, the mark as a percentage, and — for the user's own
         * review — a delete control.
         */
        private class CommentBox extends JPanel {

            private final Review review;

            CommentBox(Review review) {
                this.review = review;
                setOpaque(false);
                setLayout(new BorderLayout(8, 6));
                setBorder(new EmptyBorder(10, 14, 10, 14));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

                // Top row: profile + name on the left, mark + (optional) delete on the right.
                JPanel top = new JPanel(new BorderLayout());
                top.setOpaque(false);

                JPanel authorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                authorRow.setOpaque(false);
                authorRow.add(new JLabel(scaledIcon(DisplayConst.profile, 28)));
                String name = (review.getUser() != null && review.getUser().getName() != null)
                        ? review.getUser().getName() : "Unknown";
                JLabel nameLabel = new JLabel(name);
                nameLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
                authorRow.add(nameLabel);
                top.add(authorRow, BorderLayout.WEST);

                JPanel rightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                rightRow.setOpaque(false);

                double percent = document.getMaxMark() <= 0 ? 0 : review.getMark() / document.getMaxMark();
                JLabel markLabel = new JLabel(Math.round(percent * 1000.0) / 10.0 + "%");
                markLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
                markLabel.setForeground(new Color(clamp((int) (255 - percent * 255)), 15,
                        clamp((int) (percent * 255))));
                rightRow.add(markLabel);

                if (review.getUser() != null && user != null
                        && review.getUser().getID() != null
                        && review.getUser().getID().equals(user.getID())) {
                    JButton deleteButton = new JButton("Delete");
                    deleteButton.setBackground(new Color(199, 70, 70));
                    deleteButton.setForeground(Color.WHITE);
                    deleteButton.setFont(new Font("Helvetica", Font.PLAIN, 13));
                    deleteButton.setFocusPainted(false);
                    deleteButton.addActionListener(e -> deleteOwnReview(deleteButton));
                    rightRow.add(deleteButton);
                }

                top.add(rightRow, BorderLayout.EAST);
                add(top, BorderLayout.NORTH);

                String commentText = review.getComments() == null ? "" : review.getComments();
                if (!commentText.trim().isEmpty()) {
                    JTextArea commentArea = new JTextArea(commentText);
                    commentArea.setLineWrap(true);
                    commentArea.setWrapStyleWord(true);
                    commentArea.setEditable(false);
                    commentArea.setOpaque(false);
                    commentArea.setFont(new Font("Helvetica", Font.PLAIN, 16));
                    commentArea.setBorder(new EmptyBorder(4, 0, 0, 0));
                    add(commentArea, BorderLayout.CENTER);
                }
            }

            /**
             * deleteOwnReview
             * Confirms with the user, then deletes the user's own review on a
             * background worker and refreshes the list on success.
             * @param trigger the delete button, disabled while the request runs
             */
            private void deleteOwnReview(final JButton trigger) {
                int choice = JOptionPane.showConfirmDialog(DocumentPanel.this,
                        "Delete your review?", "Delete review",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
                trigger.setEnabled(false);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        db.deleteReview(document, review);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                        } catch (Exception ex) {
                            trigger.setEnabled(true);
                            JOptionPane.showMessageDialog(DocumentPanel.this,
                                    "Could not delete your review. Please try again.",
                                    "PeerAssist", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        document.getReviews().remove(review);
                        removeCommentBox(CommentBox.this);
                    }
                }.execute();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(111, 183, 94, 203));
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2d.dispose();
                super.paintComponent(g);
            }
        }
    }

    /**
     * trimMark
     * Formats a max mark for display, dropping a redundant ".0".
     */
    private static String trimMark(double mark) {
        if (mark == Math.floor(mark) && !Double.isInfinite(mark)) {
            return String.valueOf((long) mark);
        }
        return String.valueOf(mark);
    }

    /**
     * NumericFilter
     * Restricts a field to a non-negative decimal number, allowing at most one
     * decimal point.
     */
    private static class NumericFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String proposed = current.substring(0, offset) + string + current.substring(offset);
            if (isValid(proposed)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String proposed = current.substring(0, offset) + text + current.substring(offset + length);
            if (isValid(proposed)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValid(String text) {
            // Allow empty, an integer, or a decimal with a single point.
            return text.matches("\\d*\\.?\\d*");
        }
    }

    /**
     * LengthFilter
     * Caps the total length of a document at a maximum number of characters.
     */
    private static class LengthFilter extends DocumentFilter {
        private final int max;

        LengthFilter(int max) {
            this.max = max;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }
            if (fb.getDocument().getLength() + string.length() <= max) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) {
                text = "";
            }
            if (fb.getDocument().getLength() - length + text.length() <= max) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
