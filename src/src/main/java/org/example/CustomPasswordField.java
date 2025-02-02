package org.example; /**
 * [CustomPasswordField.java]
 * A password field that shows text when empty and censors the password
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

public class CustomPasswordField extends JTextField {

    private String backgroundText;
    private String actualText;

    /**
     * CustomPasswordField.
     * Creates a passwordfield with a
     * @param backgroundText The text to be displayed when the field is empty.
     */
    public CustomPasswordField(String backgroundText) {
        this.backgroundText = backgroundText;
        setForeground(Color.GRAY);
        setText(backgroundText);

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
        if (!getText().equals(backgroundText)) { // Censors password
            setForeground(Color.BLACK);
            String originalText = getText();
            String hiddenText = "";
            for (int i = 0; i < originalText.length(); i ++) {
                hiddenText += "*";
            }
            setText(hiddenText);
            super.paintComponent(g);
            setText(originalText);

        } else {
            super.paintComponent(g);

        }




        // Draw background text when the field is empty
        if (String.valueOf(getText()).isEmpty() && !hasFocus()) {
            g.setColor(Color.GRAY);
            setForeground(Color.GRAY);
            g.drawString(backgroundText, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
        }
    }


}
