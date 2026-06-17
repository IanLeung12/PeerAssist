package org.example; /**
 * [CustomPasswordField.java]
 * A password field that shows placeholder text when empty and masks the typed
 * value. The placeholder is rendered purely as an overlay and is never part of
 * the field's real value; callers read the typed password via {@link #getRealText()}.
 * Unlike the previous version, this never mutates the document inside paint and
 * allows spaces in passwords.
 * @author Ian Leung
 * @version 3.0
 */

import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class CustomPasswordField extends JPasswordField {

    private final String backgroundText;

    /**
     * CustomPasswordField
     * Creates a password field with the given placeholder text.
     * @param backgroundText The text shown as a placeholder when the field is empty.
     */
    public CustomPasswordField(String backgroundText) {
        this.backgroundText = backgroundText == null ? "" : backgroundText;
        setForeground(Color.BLACK);
        setEchoChar('•');
        setText("");
    }

    /**
     * getRealText
     * Returns the password the user actually typed. The placeholder is only an
     * overlay (never stored in the document), so this returns the field contents,
     * preserving spaces.
     * @return The typed password, or "" if nothing has been entered.
     */
    public String getRealText() {
        char[] chars = getPassword();
        return chars == null ? "" : new String(chars);
    }

    /**
     * paintComponent
     * Paints the masked field, then draws the placeholder overlay when the field
     * is empty and unfocused. The placeholder is drawn purely with Graphics and is
     * never written into the document.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getPassword().length == 0 && !hasFocus() && !backgroundText.isEmpty()) {
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
