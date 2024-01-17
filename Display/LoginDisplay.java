import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class LoginDisplay {

    private JFrame frame;
    private JLayeredPane lframe;
    private JPanel gPanel;
    private JPanel bPanel;
    private boolean inLogin = true;
    private User user;
    private boolean unsuccessful = false;
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    public LoginDisplay() {
        this.frame = new JFrame("Game");
        lframe = new JLayeredPane();
        lframe.setBounds(0, 0, size.width, size.height);

        gPanel = new GridAreaPanel();
        gPanel.setBackground(new Color(25, 100, 23));
        gPanel.setBounds(0, 0, size.width, size.height);
        gPanel.setOpaque(true);
        bPanel = addButtons();
        bPanel.setBounds(0, 0, size.width, size.height);
        bPanel.setOpaque(false);

        lframe.add(gPanel, 0, 0);
        lframe.add(bPanel, 1, 0);

        frame.add(lframe);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size);
        frame.setVisible(true);
    }

    private JPanel addButtons() {
        JPanel buttonPanel = new JPanel();

        CustomTextField usernameField = new CustomTextField("Enter Username");
        CustomPasswordField passwordField = new CustomPasswordField("Enter Password");
        buttonPanel.add(usernameField);
        buttonPanel.add(passwordField);
        usernameField.setBounds((size.getSize().width/2 - 300), 300, 600, 50);
        passwordField.setBounds((size.getSize().width/2 - 300), 400, 600, 50);
        usernameField.setFont(new Font("Helvetica", Font.PLAIN, 24));
        passwordField.setFont(new Font("Helvetica", Font.PLAIN, 24));

        if (inLogin) {
            JButton loginButton = new JButton("Login");
            loginButton.setBounds(size.getSize().width/2 - 100, 500, 200, 50);
            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = (passwordField.getText());
                if (username.equals("ianleung") && (password.equals("abcdef123"))) {
                    this.user = new User(1, "Ian Leung", 11, "ian@gmail.com", "abcdef123", null);
                } else {
                    unsuccessful = true;
                }
                refresh();

            });

            JButton signUpButton = new JButton("Create an account");
            signUpButton.setBounds(size.getSize().width/2 - 100, 600, 200, 50);
            signUpButton.addActionListener(e -> { inLogin = false; });

            buttonPanel.add(loginButton);
            buttonPanel.add(signUpButton);
        } else {
            CustomTextField emailField = new CustomTextField("Enter Email");
            emailField.add(usernameField);
            emailField.setBounds((size.getSize().width/2 - 300), 300, 600, 50);
            emailField.setFont(new Font("Helvetica", Font.PLAIN, 24));
        }

        buttonPanel.setLayout(null);
        return buttonPanel;
    }

    /**
     * Image
     * Loads an image
     * @param path image location
     * @return the image
     */
    public BufferedImage image(String path){
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void refresh() {
        SwingUtilities.invokeLater(() ->frame.repaint());

    }

    public void dispose() {
        frame.dispose();
    }

    public User getUser() {
        return user;
    }

    class GridAreaPanel extends JPanel {

        public void paintComponent(Graphics g) {


            setDoubleBuffered(true);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setBackground(Color.white);

            if (unsuccessful) {
                g2d.setColor(Color.red);
                drawCenteredString("Login failed. Try again or sign up to make a new account", 0, 575, size.width, 16, g2d);
            }
        }
    }

    private void drawBorderedString(String str, int x, int y, float size, float stroke, Graphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        try {
            g2d.translate(x, y);

            g2d.setColor(Color.black);
            FontRenderContext frc = g2d.getFontRenderContext();
            TextLayout tl = new TextLayout(str, g2d.getFont().deriveFont(size), frc);
            Shape shape = tl.getOutline(null);

            g2d.setStroke(new BasicStroke(stroke));
            g2d.draw(shape);

            g2d.setColor(Color.white);
            g2d.fill(shape);
        } finally {
            // Restore the ginal state
            g2d.setTransform(originalTransform);
            g2d.setStroke(originalStroke);
            g2d.setColor(originalColor);
        }
    }

    private void drawCenteredString(String str, int x, int y, int width, float size,  Graphics2D g2d) {
        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout tl = new TextLayout(str, g2d.getFont().deriveFont(size), frc);

        // Calculate the x-coordinate to center the text
        int textX = x + width/ 2 - 150;

        // Calculate the y-coordinate to center the text
        g2d.drawString(str, textX, y);
    }
}
