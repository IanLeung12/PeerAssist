package org.example; /**
 * [CustomTextField.java]
 * A custom JTextField that shows placeholder text when the field is empty.
 * The placeholder is rendered as an overlay and is never part of the field's
 * real value; callers read the typed value via {@link #getRealText()}.
 * @author Ian Leung
 * @version 3.0
 */

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class CustomTextField extends JTextField {

    private final String backgroundText;

    /**
     * CustomTextField
     * Creates a CustomTextField with the specified placeholder text.
     * @param backgroundText The text shown as a placeholder when the field is empty.
     */
    public CustomTextField(String backgroundText) {
        this.backgroundText = backgroundText == null ? "" : backgroundText;
        setForeground(Color.BLACK);
        setText("");
    }

    /**
     * getRealText
     * Returns the value the user actually typed. Because the placeholder is only
     * an overlay (never stored in the document), this simply returns the field's
     * text. Kept as the canonical submit API so callers do not depend on
     * placeholder handling.
     * @return The typed text, or "" if nothing has been entered.
     */
    public String getRealText() {
        String text = getText();
        return text == null ? "" : text;
    }

    /**
     * paintComponent
     * Paints the field, then draws the placeholder overlay when the field is
     * empty and unfocused. The placeholder is drawn purely with Graphics and is
     * never written into the document.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty() && !hasFocus() && !backgroundText.isEmpty()) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(Color.GRAY);
            Font font = getFont();
            if (font != null) {
                g2d.setFont(font);
            }
            FontMetrics fm = g2d.getFontMetrics();
            int x = getInsets().left + 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(backgroundText, x, y);
            g2d.dispose();
        }
    }
}
