import java.awt.Graphics;
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
    private JPanel iPanel;
    private String action = "login";
    private User user;
    private boolean failedLogin = false;
    private boolean failedSignUp = false;

    public LoginDisplay() {
        this.frame = new JFrame("Login");
        lframe = new JLayeredPane();
        lframe.setSize(DisplayConst.size);

        gPanel = new GridAreaPanel();
        gPanel.setBackground(new Color(150, 217, 136));
        gPanel.setSize(DisplayConst.size);
        gPanel.setOpaque(true);
        iPanel = addUserInterface();
        iPanel.setSize(DisplayConst.size);
        iPanel.setOpaque(false);

        lframe.add(gPanel, 0, 0);
        lframe.add(iPanel, 1, 0);

        frame.add(lframe);
        frame.pack();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(DisplayConst.size);
        frame.setVisible(true);
    }

    private JPanel addUserInterface() {
        JPanel buttonPanel = new JPanel();
        JButton switchButton;

        CustomTextField usernameField = new CustomTextField("Enter Username");
        CustomPasswordField passwordField = new CustomPasswordField("Enter Password");
        usernameField.setBounds((DisplayConst.size.width/2 - 300), 100, 600, 50);
        passwordField.setBounds((DisplayConst.size.width/2 - 300), 200, 600, 50);
        usernameField.setFont(new Font("Helvetica", Font.PLAIN, 24));
        passwordField.setFont(new Font("Helvetica", Font.PLAIN, 24));
        buttonPanel.add(usernameField);
        buttonPanel.add(passwordField);

        if (action.equals("login")) {
            JButton loginButton = new JButton("Login");
            loginButton.setBounds(DisplayConst.size.width/2 - 100, 300, 200, 50);
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
            switchButton.setBounds(DisplayConst.size.width/2 - 75, 900, 150, 30);
            switchButton.addActionListener(e -> {
                action = "sign up";
                failedLogin = false;
                newIPanel();
            });

            buttonPanel.add(loginButton);
            buttonPanel.add(switchButton);

        } else if (action.equals("sign up")) {
            CustomTextField emailField = new CustomTextField("Enter Email");
            emailField.setBounds((DisplayConst.size.width/2 - 300), 300, 600, 50);
            emailField.setFont(new Font("Helvetica", Font.PLAIN, 24));


            Integer[] grades = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12};
            JComboBox<Integer> gradeChooser = new JComboBox<>(grades);
            gradeChooser.setSelectedIndex(0);
            gradeChooser.setBounds(DisplayConst.size.width/2 + 120,400, 60, 40);

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
            signUpButton.setBounds(DisplayConst.size.width/2 - 100, 800, 200, 50);
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
            switchButton.setBounds(DisplayConst.size.width/2 - 75, 900, 150, 30);
            switchButton.addActionListener(e -> {
                action = "login";
                newIPanel();
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

    public void newIPanel() {
        lframe.remove(iPanel);
        iPanel = addUserInterface();
        iPanel.setSize(DisplayConst.size);
        iPanel.setOpaque(false);
        lframe.add(iPanel, 1, 0);
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
            super.paintComponent(g);
            setDoubleBuffered(true);

            Graphics2D g2d = (Graphics2D) g;


            if (failedLogin) {
                g2d.setColor(Color.red);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 14));
                g2d.drawString("Login failed. Try again or sign up to make a new account", DisplayConst.size.width/ 2 - 175, 390);
            }

            if (failedSignUp) {
                g2d.setColor(Color.red);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 14));
                g2d.drawString("Sign Up failed. Make sure your username, pasword, and email are valid", DisplayConst.size.width/ 2 - 210, 875);
            }

            if (action.equals("sign up")) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 24));
                g2d.drawString("Enter Current Grade: ", DisplayConst.size.width/2 - 140, 430);
                g2d.drawString("Select Favourite Subjects: ", DisplayConst.size.width/2 - 160, 500);
            }
        }
    }
}
