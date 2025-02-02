package org.example; /**
 * [LoginDisplay.java]
 * Display Frame class for users to login/sign up before entering main program
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

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
    private boolean newUser = false;
    private ArrayList<User> users;

    /**
     * Constructor for LoginDisplay.
     *
     * @param users The list of users registered on the platform.
     */
    public LoginDisplay(ArrayList<User> users) {
        this.users = users;
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

    /**
     * Adds the user interface components to the panel.
     *
     * @return The panel containing the user interface components.
     */
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

                // Login check
                for (User user: users) {
                    if ((username.equals(user.getName())) && (password.equals(user.getPassword()))) {
                        this.user = user;
                    }
                }
                if (user == null) {
                    failedLogin = true;
                }
            });

            // Switch between login and signup
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

            // Components only used in signing up
            Integer[] grades = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12};
            JComboBox<Integer> gradeChooser = new JComboBox<>(grades);
            gradeChooser.setSelectedIndex(0);
            gradeChooser.setBounds(DisplayConst.size.width/2 + 120,400, 60, 40);

            JToggleButton[] subjectButtons = new JToggleButton[11];
            for (int i = 0; i < 11; i ++) {
                subjectButtons[i] = new JToggleButton(DisplayConst.subjectArr[i]);
                subjectButtons[i].setBounds(350 + (200 * i) - 1100 * (i/6), 550 + 100 *  (i/6), 150, 50);
                buttonPanel.add(subjectButtons[i]);
            }

            JButton signUpButton = new JButton("Create Account");
            signUpButton.setBounds(DisplayConst.size.width/2 - 100, 800, 200, 50);
            signUpButton.addActionListener(e -> {

                //Takes values from all components to create a user;
                String username = usernameField.getText();
                String password = passwordField.getText();
                String email = emailField.getText();
                int grade = (int) gradeChooser.getSelectedItem();
                ArrayList<String> subjects = new ArrayList<>();
                for (int i = 0; i < 11; i ++) {
                    if (subjectButtons[i].isSelected()) {
                        subjects.add(DisplayConst.subjectArr[i]);
                    }
                }
                if ((fieldIsValid(username)) && (fieldIsValid(password)) && (fieldIsValid(email))) {
                    this.user = new User(users.size(), username, grade, email, password, subjects);
                    newUser = true;
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

    /**
     * Checks if the provided field value is valid.
     *
     * @param str The field value to be checked.
     * @return True if the field value is valid, false otherwise.
     */
    private boolean fieldIsValid(String str) {
        if ((str == null) || (str.contains(" "))) {
            return false;
        }
        return true;
    }

    /**
     * Reloads the interface panel (for switching between login/signup)
     */
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

    /**
     * Disposes of the frame.
     */
    public void dispose() {
        frame.dispose();
    }

    /**
     * Gets the logged-in user.
     *
     * @return The logged-in user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Checks if the user is a new user.
     *
     * @return True if the user is a new user, false otherwise.
     */
    public boolean isNewUser() {
        return newUser;
    }

    /*
    * Inner panel for drawing elements
     */
    class GridAreaPanel extends JPanel {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setDoubleBuffered(true);

            Graphics2D g2d = (Graphics2D) g;

            g2d.drawImage(DisplayConst.logo, 890, 0, null);
            g.setColor(Color.black);
            g.drawString("Release 1.0.0", 30, DisplayConst.size.height - 100);

            // Error/Invalid messages
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
