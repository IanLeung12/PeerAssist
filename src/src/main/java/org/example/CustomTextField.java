package org.example; /**
 * [CustomTextField.java]
 * A custom JTextField that shows text when the field is empty
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CustomTextField extends JTextField {

    private String backgroundText;

    /**
     * CustomTextField
     * Creates a CustomTextField with the specified background text.
     * @param backgroundText The text to be displayed as background when the field is empty
     */
    public CustomTextField(String backgroundText) {
        this.backgroundText = backgroundText;

        // Set initial text
        setForeground(Color.BLACK);
        setText("");


        // Add focus listener to handle background text behavior
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(backgroundText)) {
                    setText("");
                    setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(backgroundText);
                    setForeground(Color.GRAY);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background text when the field is empty
        if (getText().isEmpty() && !hasFocus()) {
            g.setColor(Color.GRAY);
            g.drawString(backgroundText, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
        }
    }


}
