import org.bouncycastle.util.Arrays;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class HomePanel extends JLayeredPane {

    ArrayList<Document> documents;
    ArrayList<Document> documentsCopy;
    JScrollPane scrollPane;
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    HomePanel(ArrayList<Document> documents) {
        this.documents = documents;
        documents.sort(new Document.NameComparator());
        documentsCopy = new ArrayList<>();
        documentsCopy.addAll(documents);

        JPanel documentPanel = newDocumentPanel();
        scrollPane = new JScrollPane(documentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBounds(50, 150, 900, 800);

        add(scrollPane,0);
        add(newButtonPanel(),1);

    }

    private JPanel newDocumentPanel() {
        JPanel documentPanel = new JPanel();

        documentPanel.setBackground(new Color(150, 217, 136));
        documentPanel.setOpaque(true);
        ArrayList<DocumentButton> documentList = new ArrayList<>();
        for (int i = 0; i < documentsCopy.size(); i ++) {
            documentList.add(new DocumentButton(documentsCopy.get(i)));
            documentList.get(i).setBounds(100, 50 + 250 * i, 650, 200);
            documentPanel.add(documentList.get(i));
        }

        documentPanel.setPreferredSize(new Dimension(600, 300 + 250 * documents.size()));
        documentPanel.setLayout(null);
        return documentPanel;
    }

    private JPanel newButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(0,0, size.width, size.height);
        buttonPanel.setBackground(new Color(150, 217, 136));
        buttonPanel.setOpaque(true);
        buttonPanel.setBorder(null);

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
        sortSelector.setSelectedIndex(0);
        sortSelector.addActionListener(e -> {
            String sort = sortSelector.getSelectedItem().toString();
            if (sort.equals("Alphabetical")) {
                documentsCopy.sort(new Document.NameComparator());
            } else if (sort.equals("Average Mark")) {
                documentsCopy.sort(new Document.MarkComparator());
            } else if (sort.equals("Grade Level")) {
                documentsCopy.sort(new Document.GradeComparator());
            } else if (sort.equals("Review Amount")) {
                documentsCopy.sort(new Document.ReviewComparator());
            }
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

        buttonPanel.add(reverseButton);
        buttonPanel.add(searchBar);
        buttonPanel.add(sortLabel);
        buttonPanel.add(sortSelector);
        buttonPanel.setLayout(null);
        return buttonPanel;
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
