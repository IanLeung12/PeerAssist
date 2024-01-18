import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LoginDisplay {

    private JFrame frame;
    private JLayeredPane lframe;
    private JPanel gPanel;
    private JPanel bPanel;
    private String action = "login";
    private User user;
    private boolean failedLogin = false;
    private boolean failedSignUp = false;
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    public LoginDisplay() {
        this.frame = new JFrame("Login");
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
        JButton switchButton;

        CustomTextField usernameField = new CustomTextField("Enter Username");
        CustomPasswordField passwordField = new CustomPasswordField("Enter Password");
        usernameField.setBounds((size.width/2 - 300), 100, 600, 50);
        passwordField.setBounds((size.width/2 - 300), 200, 600, 50);
        usernameField.setFont(new Font("Helvetica", Font.PLAIN, 24));
        passwordField.setFont(new Font("Helvetica", Font.PLAIN, 24));
        buttonPanel.add(usernameField);
        buttonPanel.add(passwordField);

        if (action.equals("login")) {
            JButton loginButton = new JButton("Login");
            loginButton.setBounds(size.width/2 - 100, 300, 200, 50);
            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = (passwordField.getText());
                if (username.equals("ianleung") && (password.equals("abcdef123"))) {
                    this.user = new User(1, "Ian Leung", 11, "ian@gmail.com", "abcdef123", null);
                } else {
                    failedLogin = true;
                }
            });

            switchButton = new JButton("Create an account");
            switchButton.setBounds(size.width/2 - 75, 900, 150, 30);
            switchButton.addActionListener(e -> {
                action = "sign up";
                failedLogin = false;
                newBPanel();
            });

            buttonPanel.add(loginButton);
            buttonPanel.add(switchButton);

        } else if (action.equals("sign up")) {
            CustomTextField emailField = new CustomTextField("Enter Email");
            emailField.setBounds((size.width/2 - 300), 300, 600, 50);
            emailField.setFont(new Font("Helvetica", Font.PLAIN, 24));


            Integer[] grades = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12};
            JComboBox<Integer> gradeChooser = new JComboBox<>(grades);
            gradeChooser.setSelectedIndex(0);
            gradeChooser.setBounds(size.width/2 + 120,400, 60, 40);

            JToggleButton[] subjectButtons = new JToggleButton[11];
            String[] subjectArr = new String[]{"Math", "English", "Physics", "Chemistry", "Biology", "Art", "Music",
                    "Computer Science", "Business", "French", "Social Studies"};
            for (int i = 0; i < 11; i ++) {
                subjectButtons[i] = new JToggleButton(subjectArr[i]);
                System.out.println();
                subjectButtons[i].setBounds(350 + (200 * i) - 1100 * (i/6), 550 + 100 *  (i/6), 150, 50);
                buttonPanel.add(subjectButtons[i]);
            }


            JButton signUpButton = new JButton("Create Account");
            signUpButton.setBounds(size.width/2 - 100, 800, 200, 50);
            signUpButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = passwordField.getText();
                String email = emailField.getText();
                int grade = (int) gradeChooser.getSelectedItem();
                ArrayList<String> subjects = new ArrayList<>();
                for (int i = 0; i < 11; i ++) {
                    if (subjectButtons[i].isSelected()) {
                        subjects.add(subjectArr[i]);
                    }
                }
                if ((fieldIsValid(username)) && (fieldIsValid(password)) && (fieldIsValid(email))) {
                    this.user = new User(1, username, grade, email, password, subjects);
                } else {
                    failedSignUp = true;
                }


            });

            switchButton = new JButton("Login instead");
            switchButton.setBounds(size.width/2 - 75, 900, 150, 30);
            switchButton.addActionListener(e -> {
                action = "login";
                newBPanel();
                failedSignUp = false;
            });

            buttonPanel.add(emailField);
            buttonPanel.add(gradeChooser);
            buttonPanel.add(signUpButton);
            buttonPanel.add(switchButton);
        }

        buttonPanel.setLayout(null);

        return buttonPanel;
    }

    private boolean fieldIsValid(String str) {
        if ((str == null) || (str.contains(" "))) {
            return false;
        }
        return true;
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

    public void newBPanel() {
        lframe.remove(bPanel);
        bPanel = addButtons();
        bPanel.setBounds(0, 0, size.width, size.height);
        bPanel.setOpaque(false);
        lframe.add(bPanel, 1, 0);
//
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

            if (failedLogin) {
                g2d.setColor(Color.red);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 14));
                g2d.drawString("Login failed. Try again or sign up to make a new account", size.width/ 2 - 175, 390);
            }

            if (failedSignUp) {
                g2d.setColor(Color.red);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 14));
                g2d.drawString("Sign Up failed. Make sure your username, pasword, and email are valid", size.width/ 2 - 210, 875);
            }

            if (action.equals("sign up")) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 24));
                g2d.drawString("Enter Current Grade: ", size.width/2 - 140, 430);
                g2d.drawString("Select Favourite Subjects: ", size.width/2 - 160, 500);
            }
        }
    }
}
