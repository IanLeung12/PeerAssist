/**
 * [HomePanel.java]
 * Panel that displays all documents and lets users upload their own. Uses real
 * layout managers (BorderLayout + BoxLayout + GridBagLayout) so it fits any
 * screen, uploads on a background worker with inline feedback, and lets owners
 * delete their own documents.
 * @author Ian Leung
 * @version 3.0
 */

package org.example;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class HomePanel extends JPanel {

    private static final Color BACKGROUND = new Color(150, 217, 136);
    private static final Color PANEL = new Color(185, 239, 173);
    private static final Color ERROR_RED = new Color(176, 0, 32);
    private static final Color OK_GREEN = new Color(26, 110, 21);

    private final ArrayList<Document> documents;
    private ArrayList<Document> documentsCopy;
    private final MainDisplay mainDisplay;
    private final User user;
    private final Database db;

    private final JPanel listPanel;
    private CustomTextField searchBar;
    private JComboBox<String> sortSelector;

    // Upload form state.
    private String selectedPath;
    private boolean fileValid;

    /**
     * Constructor for HomePanel.
     *
     * @param documents   The list of documents to be displayed.
     * @param mainDisplay The main display object.
     * @param user        The current user.
     * @param db          The Supabase database client.
     */
    HomePanel(ArrayList<Document> documents, MainDisplay mainDisplay, User user, Database db) {
        this.documents = documents;
        this.mainDisplay = mainDisplay;
        this.user = user;
        this.db = db;

        documents.sort(new Document.NameComparator());
        this.documentsCopy = new ArrayList<>(documents);

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        listPanel = new JPanel();
        listPanel.setBackground(BACKGROUND);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane listScroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.setBorder(null);
        listScroll.getVerticalScrollBar().setUnitIncrement(18);

        add(buildToolbar(), BorderLayout.NORTH);
        add(listScroll, BorderLayout.CENTER);
        add(buildUploadPanel(), BorderLayout.EAST);

        refreshList();
    }

    /**
     * buildToolbar
     * Builds the search + sort + reverse toolbar.
     */
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        toolbar.setBackground(BACKGROUND);

        searchBar = new CustomTextField("Search documents…");
        searchBar.setFont(new Font("Helvetica", Font.PLAIN, 18));
        searchBar.setPreferredSize(new Dimension(360, 40));
        searchBar.addActionListener(e -> applyFilterAndSort());

        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
        sortLabel.setForeground(new Color(26, 72, 21));

        sortSelector = new JComboBox<>(new String[]{"Alphabetical", "Average Mark", "Grade Level", "Review Amount"});
        sortSelector.setFont(new Font("Helvetica", Font.PLAIN, 16));
        sortSelector.setPreferredSize(new Dimension(180, 40));
        sortSelector.addActionListener(e -> applyFilterAndSort());

        JButton reverseButton = new JButton("Reverse");
        reverseButton.setFont(new Font("Helvetica", Font.PLAIN, 15));
        reverseButton.setFocusPainted(false);
        reverseButton.addActionListener(e -> {
            Collections.reverse(documentsCopy);
            refreshList();
        });

        toolbar.add(searchBar);
        toolbar.add(sortLabel);
        toolbar.add(sortSelector);
        toolbar.add(reverseButton);
        return toolbar;
    }

    /**
     * buildUploadPanel
     * Builds the right-hand upload form using a GridBagLayout inside a scroll
     * pane (so it stays usable on small screens).
     */
    private JScrollPane buildUploadPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(8, 0, 8, 0);

        JLabel title = new JLabel("Upload a Document");
        title.setFont(new Font("Helvetica", Font.BOLD, 28));
        title.setForeground(new Color(26, 72, 21));

        final JLabel fileLabel = new JLabel("No file selected.");
        fileLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));

        JButton chooseButton = new JButton("Select PDF…");
        chooseButton.setFont(new Font("Helvetica", Font.PLAIN, 16));
        chooseButton.addActionListener(e -> chooseFile(fileLabel));

        Integer[] grades = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        final JComboBox<Integer> gradeChooser = new JComboBox<>(grades);
        gradeChooser.setSelectedIndex(0);

        final JTextField markField = createDecimalField("e.g. 100");
        markField.setFont(new Font("Helvetica", Font.PLAIN, 16));

        final TopicSelector topicSelector = new TopicSelector(DisplayConst.subjectArr);

        final JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Helvetica", Font.PLAIN, 15));
        final Timer statusTimer = new Timer(7000, e -> statusLabel.setText(" "));
        statusTimer.setRepeats(false);

        final JButton publishButton = new JButton("Publish Document");
        publishButton.setFont(new Font("Helvetica", Font.BOLD, 20));
        publishButton.setBackground(new Color(59, 138, 51));
        publishButton.setForeground(Color.WHITE);
        publishButton.setFocusPainted(false);
        publishButton.addActionListener(e -> publish(gradeChooser, markField, topicSelector,
                fileLabel, statusLabel, statusTimer, publishButton));

        int y = 0;
        c.gridy = y++; form.add(title, c);
        c.gridy = y++; form.add(chooseButton, c);
        c.gridy = y++; form.add(fileLabel, c);
        c.gridy = y++; form.add(labeled("Grade level:", gradeChooser), c);
        c.gridy = y++; form.add(labeled("Maximum mark:", markField), c);
        c.gridy = y++; form.add(labeled("Topics:", topicSelector), c);
        c.gridy = y++; form.add(statusLabel, c);
        c.gridy = y++; form.add(publishButton, c);

        JScrollPane scroll = new JScrollPane(form,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setPreferredSize(new Dimension(480, 100));
        return scroll;
    }

    /**
     * chooseFile
     * Opens a PDF-filtered chooser and validates the selection immediately,
     * giving the user instant feedback.
     */
    private void chooseFile(JLabel fileLabel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        selectedPath = file.getAbsolutePath();
        boolean extOk = file.getName().toLowerCase().endsWith(".pdf");
        fileValid = extOk && isPdf(file);
        if (fileValid) {
            fileLabel.setText(file.getName());
            fileLabel.setForeground(OK_GREEN);
        } else {
            fileLabel.setText("Not a valid PDF file.");
            fileLabel.setForeground(ERROR_RED);
        }
    }

    /**
     * isPdf
     * Returns true if the file begins with the %PDF- magic bytes.
     */
    private boolean isPdf(File file) {
        try (InputStream in = new FileInputStream(file)) {
            byte[] header = new byte[5];
            int read = in.read(header);
            return read == 5 && header[0] == '%' && header[1] == 'P'
                    && header[2] == 'D' && header[3] == 'F' && header[4] == '-';
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * publish
     * Validates the form, then saves the document on a background worker, showing
     * progress and inline success/error feedback.
     */
    private void publish(final JComboBox<Integer> gradeChooser, final JTextField markField,
                         final TopicSelector topicSelector, final JLabel fileLabel,
                         final JLabel statusLabel, final Timer statusTimer, final JButton publishButton) {
        if (selectedPath == null || !fileValid) {
            showStatus(statusLabel, statusTimer, "Please select a valid PDF file.", false);
            return;
        }
        final double maxMark;
        try {
            maxMark = Double.parseDouble(markField.getText().trim());
        } catch (NumberFormatException ex) {
            showStatus(statusLabel, statusTimer, "Maximum mark must be a number.", false);
            return;
        }
        if (maxMark <= 0) {
            showStatus(statusLabel, statusTimer, "Maximum mark must be greater than 0.", false);
            return;
        }

        final ArrayList<String> topics = topicSelector.getSelectedTopics();
        final int grade = (Integer) gradeChooser.getSelectedItem();
        final String path = selectedPath;

        publishButton.setEnabled(false);
        publishButton.setText("Uploading…");
        new SwingWorker<Document, Void>() {
            @Override
            protected Document doInBackground() throws Exception {
                // doc_id is assigned by the server in saveDoc(); use -1 as a placeholder.
                Document doc = new Document(-1, user, path, maxMark, grade, topics);
                db.saveDoc(doc);
                return doc;
            }

            @Override
            protected void done() {
                publishButton.setEnabled(true);
                publishButton.setText("Publish Document");
                try {
                    Document doc = get();
                    documents.add(doc);
                    applyFilterAndSort();
                    showStatus(statusLabel, statusTimer, "Uploaded successfully.", true);
                    // Reset the form for the next upload.
                    selectedPath = null;
                    fileValid = false;
                    fileLabel.setText("No file selected.");
                    fileLabel.setForeground(Color.BLACK);
                    markField.setText("");
                    gradeChooser.setSelectedIndex(0);
                    topicSelector.clearSelection();
                } catch (Exception ex) {
                    showStatus(statusLabel, statusTimer,
                            "Upload failed. Please check the file and try again.", false);
                }
            }
        }.execute();
    }

    /**
     * deleteDocument
     * Confirms then deletes the given (owned) document on a background worker.
     */
    private void deleteDocument(final Document doc) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + doc.getName() + "\"? This cannot be undone.", "Delete Document",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                db.deleteDoc(doc);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    documents.remove(doc);
                    documentsCopy.remove(doc);
                    refreshList();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(HomePanel.this,
                            "Could not delete the document. Please try again.",
                            "PeerAssist", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * refreshList
     * Rebuilds the document list from the current filtered/sorted view.
     */
    private void refreshList() {
        listPanel.removeAll();
        listPanel.add(Box.createVerticalStrut(8));
        for (final Document doc : documentsCopy) {
            DocumentButton button = new DocumentButton(doc);
            button.addActionListener(e -> mainDisplay.setToDocumentPanel(doc));

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 216));
            row.add(button, BorderLayout.CENTER);

            if (doc.getUser() != null && doc.getUser().getID() != null
                    && doc.getUser().getID().equals(user.getID())) {
                JButton delete = new JButton("Delete");
                delete.setFont(new Font("Helvetica", Font.PLAIN, 14));
                delete.setBackground(new Color(199, 70, 70));
                delete.setForeground(Color.WHITE);
                delete.setFocusPainted(false);
                delete.addActionListener(e -> deleteDocument(doc));
                JPanel east = new JPanel(new BorderLayout());
                east.setOpaque(false);
                east.add(delete, BorderLayout.NORTH);
                row.add(east, BorderLayout.EAST);
            }

            listPanel.add(row);
        }
        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * applyFilterAndSort
     * Re-derives the visible list from the search text and selected sort.
     */
    private void applyFilterAndSort() {
        String query = searchBar.getRealText();
        documentsCopy = search(documents, query);
        sort((String) sortSelector.getSelectedItem());
        refreshList();
    }

    /**
     * sort
     * Sorts the visible documents by the given sort type.
     */
    private void sort(String sortType) {
        if ("Alphabetical".equals(sortType)) {
            documentsCopy.sort(new Document.NameComparator());
        } else if ("Average Mark".equals(sortType)) {
            documentsCopy.sort(new Document.MarkComparator());
        } else if ("Grade Level".equals(sortType)) {
            documentsCopy.sort(new Document.GradeComparator());
        } else if ("Review Amount".equals(sortType)) {
            documentsCopy.sort(new Document.ReviewComparator());
        }
    }

    /**
     * search
     * Filters documents whose name contains the given text (case-insensitive).
     */
    private ArrayList<Document> search(ArrayList<Document> docs, String value) {
        String needle = value == null ? "" : value.toLowerCase();
        ArrayList<Document> result = new ArrayList<>();
        for (Document doc : docs) {
            if (doc.getName() != null && doc.getName().toLowerCase().contains(needle)) {
                result.add(doc);
            }
        }
        return result;
    }

    // ---- small UI helpers -------------------------------------------------

    private JPanel labeled(String text, Component field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(sectionLabel(text), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Helvetica", Font.PLAIN, 16));
        l.setForeground(new Color(26, 72, 21));
        return l;
    }

    private void showStatus(JLabel label, Timer timer, String message, boolean ok) {
        label.setText(message);
        label.setForeground(ok ? OK_GREEN : ERROR_RED);
        timer.restart();
    }

    /**
     * createDecimalField
     * Creates a text field that only accepts a non-negative decimal number.
     */
    private JTextField createDecimalField(String placeholder) {
        CustomTextField field = new CustomTextField(placeholder);
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DecimalFilter());
        return field;
    }

    /**
     * DecimalFilter
     * Restricts input to a non-negative decimal (digits with at most one dot).
     */
    private static class DecimalFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }
            String result = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                    .insert(offset, string).toString();
            if (isValid(result)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, text);
            if (isValid(sb.toString())) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValid(String s) {
            return s.matches("\\d*\\.?\\d*");
        }
    }
}
