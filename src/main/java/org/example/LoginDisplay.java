package org.example; /**
 * [LoginDisplay.java]
 * Login / sign-up window shown before the main program. Uses real layout
 * managers (BorderLayout + CardLayout + GridBagLayout) so it fits any screen,
 * runs authentication off the Event Dispatch Thread on a SwingWorker, and shows
 * specific, inline error messages derived from {@link AuthException}.
 * @author Ian Leung
 * @version 3.0
 */

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class LoginDisplay {

    private static final Color BACKGROUND = new Color(150, 217, 136);
    private static final Color ERROR_RED = new Color(176, 0, 32);
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MIN_PASSWORD = 6;

    private final JFrame frame;
    private final Database db;
    private Consumer<User> onLoginSuccess;

    /**
     * Constructor for LoginDisplay.
     *
     * @param db The Supabase database client.
     */
    public LoginDisplay(Database db) {
        this.db = db;
        this.frame = new JFrame("PeerAssist — Sign In");

        frame.setLayout(new BorderLayout());
        frame.add(buildLogoHeader(), BorderLayout.NORTH);
        frame.add(buildLoginCard(), BorderLayout.CENTER);

        frame.getContentPane().setBackground(BACKGROUND);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DisplayConst.enableFullScreen(frame);
        frame.setSize(DisplayConst.size);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * setOnLoginSuccess
     * Registers the callback invoked (on the EDT) with the authenticated user
     * after a successful login or sign up.
     *
     * @param cb The success callback.
     */
    public void setOnLoginSuccess(Consumer<User> cb) {
        this.onLoginSuccess = cb;
    }

    /**
     * dispose
     * Disposes of the window.
     */
    public void dispose() {
        frame.dispose();
    }

    /**
     * buildLogoHeader
     * Builds the top banner containing the scaled logo.
     */
    private JPanel buildLogoHeader() {
        JPanel header = new JPanel();
        header.setBackground(BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(24, 0, 8, 0));
        JLabel logo = new JLabel(scaledLogo(120));
        header.add(logo);
        return header;
    }

    /**
     * buildLoginCard
     * Builds the login form (email + password) with inline validation and async
     * submission. Returns a panel that centres the form.
     */
    private JScrollPane buildLoginCard() {
        CustomTextField emailField = new CustomTextField("Enter Email");
        CustomPasswordField passwordField = new CustomPasswordField("Enter Password");
        styleField(emailField);
        styleField(passwordField);

        JLabel errorLabel = newErrorLabel();
        final Timer errorTimer = newErrorTimer(errorLabel);

        JButton loginButton = newPrimaryButton("Log In");
        JButton switchButton = newLinkButton("New here? Create an account");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = baseConstraints();

        addRow(form, c, 0, new JLabel("Welcome back", SwingConstants.CENTER), 28f, Font.BOLD);
        c.gridy = 1; form.add(emailField, c);
        c.gridy = 2; form.add(passwordField, c);
        c.gridy = 3; form.add(errorLabel, c);
        c.gridy = 4; form.add(loginButton, c);
        c.gridy = 5; form.add(switchButton, c);

        Runnable submit = () -> {
            String email = emailField.getRealText().trim();
            String password = passwordField.getRealText();
            if (email.isEmpty() || password.isEmpty()) {
                showError(errorLabel, errorTimer, "Please enter your email and password.");
                return;
            }
            if (!EMAIL.matcher(email).matches()) {
                showError(errorLabel, errorTimer, AuthException.userMessageFor(AuthException.Kind.INVALID_EMAIL));
                return;
            }
            doLogin(email, password, loginButton, errorLabel, errorTimer);
        };
        loginButton.addActionListener(e -> submit.run());
        onEnter(emailField, submit);
        onEnter(passwordField, submit);
        switchButton.addActionListener(e -> swapCard(buildSignUpCard(), loginButton));

        frame.getRootPane().setDefaultButton(loginButton);
        return centeringWrapper(form);
    }

    /**
     * buildSignUpCard
     * Builds the sign-up form (username, email, password, grade, subjects) with
     * inline validation and async submission.
     */
    private JScrollPane buildSignUpCard() {
        CustomTextField usernameField = new CustomTextField("Enter Username");
        CustomTextField emailField = new CustomTextField("Enter Email");
        CustomPasswordField passwordField = new CustomPasswordField("Enter Password (min 6 chars)");
        styleField(usernameField);
        styleField(emailField);
        styleField(passwordField);

        Integer[] grades = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        JComboBox<Integer> gradeChooser = new JComboBox<>(grades);
        gradeChooser.setSelectedIndex(0);

        final TopicSelector topicSelector = new TopicSelector(DisplayConst.subjectArr);

        JLabel errorLabel = newErrorLabel();
        final Timer errorTimer = newErrorTimer(errorLabel);

        JButton createButton = newPrimaryButton("Create Account");
        JButton switchButton = newLinkButton("Already have an account? Log in");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = baseConstraints();

        addRow(form, c, 0, new JLabel("Create your account", SwingConstants.CENTER), 28f, Font.BOLD);
        c.gridy = 1; form.add(usernameField, c);
        c.gridy = 2; form.add(emailField, c);
        c.gridy = 3; form.add(passwordField, c);
        c.gridy = 4; form.add(labeledRow("Current grade:", gradeChooser), c);
        c.gridy = 5; form.add(labeledRow("Favourite subjects (optional):", topicSelector), c);
        c.gridy = 6; form.add(errorLabel, c);
        c.gridy = 7; form.add(createButton, c);
        c.gridy = 8; form.add(switchButton, c);

        Runnable submit = () -> {
            String username = usernameField.getRealText().trim();
            String email = emailField.getRealText().trim();
            String password = passwordField.getRealText();
            if (username.isEmpty()) {
                showError(errorLabel, errorTimer, "Please enter a username.");
                return;
            }
            if (!EMAIL.matcher(email).matches()) {
                showError(errorLabel, errorTimer, AuthException.userMessageFor(AuthException.Kind.INVALID_EMAIL));
                return;
            }
            if (password.length() < MIN_PASSWORD) {
                showError(errorLabel, errorTimer, AuthException.userMessageFor(AuthException.Kind.WEAK_PASSWORD));
                return;
            }
            ArrayList<String> subjects = topicSelector.getSelectedTopics();
            int grade = (Integer) gradeChooser.getSelectedItem();
            doSignUp(username, grade, email, password, subjects, createButton, errorLabel, errorTimer);
        };
        createButton.addActionListener(e -> submit.run());
        onEnter(usernameField, submit);
        onEnter(emailField, submit);
        onEnter(passwordField, submit);
        switchButton.addActionListener(e -> swapCard(buildLoginCard(), createButton));

        frame.getRootPane().setDefaultButton(createButton);
        return centeringWrapper(form);
    }

    /**
     * doLogin
     * Runs the login request on a SwingWorker and reports the result.
     */
    private void doLogin(final String email, final String password, final JButton button,
                         final JLabel errorLabel, final Timer errorTimer) {
        setBusy(button, true, "Signing in…");
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return db.logIn(email, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    succeed(user);
                } catch (ExecutionException ex) {
                    setBusy(button, false, "Log In");
                    showError(errorLabel, errorTimer, messageFor(ex.getCause()));
                } catch (Exception ex) {
                    setBusy(button, false, "Log In");
                    showError(errorLabel, errorTimer,
                            AuthException.userMessageFor(AuthException.Kind.UNKNOWN));
                }
            }
        }.execute();
    }

    /**
     * doSignUp
     * Runs the sign-up request on a SwingWorker and reports the result.
     */
    private void doSignUp(final String username, final int grade, final String email,
                          final String password, final ArrayList<String> subjects,
                          final JButton button, final JLabel errorLabel, final Timer errorTimer) {
        setBusy(button, true, "Creating account…");
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return db.signUp(username, grade, email, password, subjects);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    succeed(user);
                } catch (ExecutionException ex) {
                    setBusy(button, false, "Create Account");
                    showError(errorLabel, errorTimer, messageFor(ex.getCause()));
                } catch (Exception ex) {
                    setBusy(button, false, "Create Account");
                    showError(errorLabel, errorTimer,
                            AuthException.userMessageFor(AuthException.Kind.UNKNOWN));
                }
            }
        }.execute();
    }

    /**
     * succeed
     * Invokes the success callback with the authenticated user.
     */
    private void succeed(User user) {
        if (onLoginSuccess != null) {
            onLoginSuccess.accept(user);
        }
    }

    /**
     * messageFor
     * Extracts a specific user-facing message from an auth failure cause.
     */
    private String messageFor(Throwable cause) {
        if (cause instanceof AuthException) {
            return ((AuthException) cause).getUserMessage();
        }
        return AuthException.userMessageFor(AuthException.Kind.UNKNOWN);
    }

    /**
     * swapCard
     * Replaces the centre card with the given panel and refreshes the window.
     *
     * @param card           The new centre card.
     * @param oldDefaultBtn  The button to clear as the root default button.
     */
    private void swapCard(java.awt.Component card, JButton oldDefaultBtn) {
        BorderLayout layout = (BorderLayout) frame.getContentPane().getLayout();
        java.awt.Component current = layout.getLayoutComponent(BorderLayout.CENTER);
        if (current != null) {
            frame.remove(current);
        }
        frame.add(card, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // ---- small UI helpers -------------------------------------------------

    /**
     * centeringWrapper
     * Wraps a form so it stays centred, and adds a scroll pane so tall forms
     * remain reachable on small screens.
     */
    private JScrollPane centeringWrapper(JPanel form) {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(form, new GridBagConstraints());

        JScrollPane scroll = new JScrollPane(center,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        return c;
    }

    private void addRow(JPanel form, GridBagConstraints c, int y, JLabel label, float size, int style) {
        label.setFont(label.getFont().deriveFont(style, size));
        label.setForeground(new Color(26, 72, 21));
        c.gridy = y;
        form.add(label, c);
    }

    private JLabel leftLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Helvetica", Font.PLAIN, 16));
        l.setForeground(new Color(26, 72, 21));
        return l;
    }

    private JPanel labeledRow(String text, java.awt.Component field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(leftLabel(text), BorderLayout.WEST);
        row.add(field, BorderLayout.EAST);
        return row;
    }

    private void styleField(javax.swing.JTextField field) {
        field.setFont(new Font("Helvetica", Font.PLAIN, 20));
        field.setPreferredSize(new Dimension(420, 44));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 150, 80)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    private JButton newPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Helvetica", Font.BOLD, 20));
        b.setBackground(new Color(59, 138, 51));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(420, 48));
        return b;
    }

    private JButton newLinkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Helvetica", Font.PLAIN, 15));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(new Color(26, 72, 21));
        return b;
    }

    private JLabel newErrorLabel() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setFont(new Font("Helvetica", Font.PLAIN, 15));
        l.setForeground(ERROR_RED);
        return l;
    }

    private Timer newErrorTimer(final JLabel label) {
        Timer t = new Timer(7000, e -> label.setText(" "));
        t.setRepeats(false);
        return t;
    }

    private void showError(JLabel label, Timer timer, String message) {
        label.setText(message);
        timer.restart();
    }

    private void setBusy(JButton button, boolean busy, String text) {
        button.setEnabled(!busy);
        button.setText(text);
    }

    private void onEnter(javax.swing.JTextField field, final Runnable action) {
        field.addActionListener(e -> action.run());
    }

    /**
     * scaledLogo
     * Returns the logo scaled to the given height (aspect preserved). Safe even
     * when the underlying image is the 1x1 fallback.
     */
    private ImageIcon scaledLogo(int height) {
        BufferedImage src = DisplayConst.logo;
        if (src == null || src.getWidth() <= 1) {
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }
        Image scaled = src.getScaledInstance(-1, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
