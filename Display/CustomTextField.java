import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;



public class CustomTextField extends JTextField {

    private String backgroundText;

    public CustomTextField(String backgroundText) {
        this.backgroundText = backgroundText;

        // Set initial text
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
        super.paintComponent(g);

        // Draw background text when the field is empty
        if (getText().isEmpty() && !hasFocus()) {
            g.setColor(Color.GRAY);
            g.drawString(backgroundText, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
        }
    }


}
