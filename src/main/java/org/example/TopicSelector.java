package org.example; /**
 * [TopicSelector.java]
 * A compact dropdown control for choosing any number of topics. Renders as a
 * single button that opens a popup of checkboxes; toggling a checkbox keeps the
 * popup open (clicking outside dismisses it). Replaces the previous grid of
 * eleven toggle buttons so topic selection takes far less space.
 * @author Ian Leung
 * @version 3.0
 */

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;
import java.util.ArrayList;

public class TopicSelector extends JButton {

    private final JPopupMenu popup = new JPopupMenu();
    private final ArrayList<JCheckBox> boxes = new ArrayList<>();
    private final String placeholder;

    /**
     * TopicSelector
     * Creates a topic dropdown for the given topic names.
     * @param topics The selectable topic names.
     */
    public TopicSelector(String[] topics) {
        this(topics, "Select topics");
    }

    /**
     * TopicSelector
     * Creates a topic dropdown with a custom empty-state label.
     * @param topics      The selectable topic names.
     * @param placeholder The text shown when nothing is selected.
     */
    public TopicSelector(String[] topics, String placeholder) {
        this.placeholder = placeholder;
        popup.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        if (topics != null) {
            for (String topic : topics) {
                JCheckBox box = new JCheckBox(topic);
                box.setOpaque(false);
                // Toggling a plain JCheckBox does not dismiss the popup, so the
                // user can pick several topics in one open.
                box.addItemListener(e -> updateText());
                boxes.add(box);
                popup.add(box);
            }
        }
        setFocusPainted(false);
        addActionListener(e -> popup.show(this, 0, getHeight()));
        updateText();
    }

    /**
     * updateText
     * Updates the button label to reflect how many topics are selected.
     */
    private void updateText() {
        int count = 0;
        for (JCheckBox box : boxes) {
            if (box.isSelected()) {
                count++;
            }
        }
        String label = count == 0
                ? placeholder
                : count + (count == 1 ? " topic selected" : " topics selected");
        setText(label + "  ▾");
    }

    /**
     * getSelectedTopics
     * Returns the names of the currently selected topics.
     * @return The selected topic names (possibly empty).
     */
    public ArrayList<String> getSelectedTopics() {
        ArrayList<String> selected = new ArrayList<>();
        for (JCheckBox box : boxes) {
            if (box.isSelected()) {
                selected.add(box.getText());
            }
        }
        return selected;
    }

    /**
     * clearSelection
     * Deselects all topics.
     */
    public void clearSelection() {
        for (JCheckBox box : boxes) {
            box.setSelected(false);
        }
        updateText();
    }
}
