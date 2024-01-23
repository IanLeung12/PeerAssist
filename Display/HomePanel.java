import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.io.File;
import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

public class HomePanel extends JLayeredPane {

    private ArrayList<Document> documents;
    private ArrayList<Document> documentsCopy;
    private JScrollPane scrollPane;
    private MainDisplay mainDisplay;

    HomePanel(ArrayList<Document> documents, MainDisplay mainDisplay) {
        this.mainDisplay = mainDisplay;
        this.documents = documents;
        documents.sort(new Document.NameComparator());
        documentsCopy = new ArrayList<>();
        documentsCopy.addAll(documents);


        JPanel documentPanel = newDocumentPanel();
        scrollPane = new JScrollPane(documentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBounds(50, 150, 900, 800);


        add(scrollPane,0);
        add( new MainPanel(),1);

        setBackground(new Color(150, 217, 136));
        setSize(DisplayConst.size);
        setVisible(true);

    }


    private JPanel newDocumentPanel() {
        JPanel documentPanel = new JPanel();

        documentPanel.setBackground(new Color(150, 217, 136));
        documentPanel.setOpaque(true);
        ArrayList<DocumentButton> documentList = new ArrayList<>();
        for (int i = 0; i < documentsCopy.size(); i ++) {
            DocumentButton documentButton = new DocumentButton(documentsCopy.get(i));
            documentList.add(documentButton);
            documentList.get(i).setBounds(100, 50 + 250 * i, 650, 200);
            documentPanel.add(documentList.get(i));
            documentButton.addActionListener(e -> {
                mainDisplay.setToDocumentPanel(documentButton.getDocument());
            });
        }

        documentPanel.setPreferredSize(new Dimension(600, 100 + 250 * documentsCopy.size()));
        documentPanel.setLayout(null);
        return documentPanel;
    }

    private class MainPanel extends JPanel {
        private String fileName;
        private String path;
        private String publishText;

        MainPanel() {
            fileName = "No file selected.";
            publishText = "";

            setBounds(0, 0, DisplayConst.size.width, DisplayConst.size.height);
            setBackground(new Color(150, 217, 136));
            setOpaque(true);
            setBorder(null);

            JTextField searchBar = new CustomTextField("Search");
            searchBar.setBounds(50, 50, 500, 50);
            searchBar.setFont(new Font("Helvetica", Font.PLAIN, 24));
            searchBar.setBackground(new Color(225, 241, 183));
            searchBar.addActionListener(e -> {
                documentsCopy = search(documents, searchBar.getText());
                scrollPane.setViewportView(newDocumentPanel());
            });

            JLabel sortLabel = new JLabel("Sort by:");
            sortLabel.setFont(new Font("Helvetica", Font.PLAIN, 24));
            sortLabel.setForeground(new Color(26, 72, 21));
            sortLabel.setBounds(575, 50, 100, 50);

            String[] sorts = new String[]{"Alphabetical", "Average Mark", "Grade Level", "Review Amount"};
            JComboBox<String> sortSelector = new JComboBox<>(sorts);
            sortSelector.setBounds(675, 50, 200, 50);
            sortSelector.setFont(new Font("Helvetica", Font.PLAIN, 20));
            sortSelector.setSelectedIndex(0);
            sortSelector.addActionListener(e -> {
                sort(sortSelector.getSelectedItem().toString());
                scrollPane.setViewportView(newDocumentPanel());
            });

            JButton reverseButton = new JButton(new ImageIcon("Pictures/ReverseIcon.png"));
            reverseButton.setBounds(900, 50, 50, 50);
            reverseButton.setBorderPainted(false);
            reverseButton.setBackground(new Color(150, 217, 136));
            reverseButton.addActionListener(e -> {
                Collections.reverse(documentsCopy);
                scrollPane.setViewportView(newDocumentPanel());
            });

            JButton chooseButton = new JButton("Select File");
            chooseButton.setBounds(1150, 250,  150, 50);
            chooseButton.setFont(new Font("Helvetica", Font.PLAIN, 18));
            chooseButton.addActionListener(e -> {
               JFileChooser fileChooser = new JFileChooser();
               int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    fileName = selectedFile.getName();
                    path = selectedFile.getAbsolutePath();
                    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    if (!fileType.equals("pdf")) {
                        fileName = "Invalid. File must be a pdf.";
                    }
                }
            });

            Integer[] grades = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12};
            JComboBox<Integer> gradeChooser = new JComboBox<>(grades);
            gradeChooser.setSelectedIndex(0);
            gradeChooser.setBounds(1350,325, 60, 40);

            JTextField markField = createNumericTextField("Enter Here...");
            markField.setBounds(1350, 400, 200, 40);
            markField.setFont(new Font("Helvetica", Font.PLAIN, 20));

            JToggleButton[] subjectButtons = new JToggleButton[11];
            for (int i = 0; i < 11; i ++) {
                subjectButtons[i] = new JToggleButton(DisplayConst.subjectArr[i]);
                subjectButtons[i].setBounds(1150 + (200 * i) - 600 * (i/3), 520 + 60 *  (i/3), 150, 40);
                add(subjectButtons[i]);
            }

            JButton publishButton = new JButton("Publish Document");
            publishButton.setBounds(1225, 800, 400, 50);
            publishButton.setFont(new Font("Helvetica", Font.BOLD, 32));

            publishButton.addActionListener(e -> {
                if ((fileName.equals("Invalid. File must be a pdf.")) || (fileName.equals("No file selected."))) {
                    publishText = "Publish File Error. Make sure file is correct";
                } else if (markField.getText().isEmpty()) {
                    publishText = "Publish File Error. Make sure mark is a number";
                } else {
                    ArrayList<String> topics = new ArrayList<>();
                    for (int i = 0; i < 11; i ++) {
                        if (subjectButtons[i].isSelected()) {
                            topics.add(DisplayConst.subjectArr[i]);
                        }
                    }
                    documents.add(new Document(path, Double.parseDouble(markField.getText()),
                            grades[gradeChooser.getSelectedIndex()], topics));
                    documentsCopy = search(documents, searchBar.getText().equals("Search") ? "" : searchBar.getText());
                    System.out.println(searchBar.getText());
                    sort(sortSelector.getSelectedItem().toString());
                    scrollPane.setViewportView(newDocumentPanel());
                    publishText = "Your file has been successfully uploaded.";
                    fileName = "No file selected.";
                    gradeChooser.setSelectedIndex(0);
                    markField.setText("");
                }
            });

            add(gradeChooser);
            add(markField);
            add(chooseButton);
            add(reverseButton);
            add(publishButton);
            add(searchBar);
            add(sortLabel);
            add(sortSelector);
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

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            setDoubleBuffered(true);

            g2d.drawImage(DisplayConst.logo, 1050, 20, null);
            g.setColor(new Color(185, 239, 173));
            g.fillRoundRect(1050, 150, 750, 800, 50, 50);

            g.setColor(Color.black);
            g.setFont(new Font("Helvetica", Font.BOLD, 42));
            g.drawString("Upload a Document:", 1230, 205);

            g.setFont(new Font("Helvetica", Font.PLAIN, 26));
            if (fileName.equals("Invalid. File must be a pdf.")) {
                g.setColor(Color.red);
            }
            g.drawString(fileName, 1320, 285);

            g.setColor(Color.black);
            g.drawString("Grade Level:", 1150, 350);
            g.drawString("Maximum Mark:", 1150, 425);
            g.drawString("Topics:", 1375, 500);

            if (!publishText.equals("Your file has been successfully uploaded.")) {
                g.setColor(Color.red);
            }
            g.drawString(publishText, 1200, 900);



        }
    }

    private void sort(String sortType) {
        if (sortType.equals("Alphabetical")) {
            documentsCopy.sort(new Document.NameComparator());
        } else if (sortType.equals("Average Mark")) {
            documentsCopy.sort(new Document.MarkComparator());
        } else if (sortType.equals("Grade Level")) {
            documentsCopy.sort(new Document.GradeComparator());
        } else if (sortType.equals("Review Amount")) {
            documentsCopy.sort(new Document.ReviewComparator());
        }
    }
    private ArrayList<Document> search(ArrayList<Document> documents, String value) {
        ArrayList<Document> newDocuments = new ArrayList<>();
        for (Document document: documents) {
            if (document.getName().toLowerCase().contains(value.toLowerCase())) {
                newDocuments.add(document);
            }
        }
        return newDocuments;

    }
}
