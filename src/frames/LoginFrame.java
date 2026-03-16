package frames;

import data.DataHandler;
import models.User;
import models.UserRole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Login window - first screen users see
public class LoginFrame extends JFrame {

    private DataHandler dataHandler;
    private JTextField  usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        dataHandler = new DataHandler();
        setupUI();
    }

    private void setupUI() {
        setTitle("Hall Symphony - Login");
        setSize(420, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(245, 245, 250));

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("Hall Symphony Inc.", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        JLabel subtitleLabel = new JLabel("Hall Booking System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(200, 220, 255));
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 5, 10));
        formPanel.setBackground(new Color(245, 245, 250));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        // Status label (shows error messages)
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 8));
        buttonPanel.setBackground(new Color(245, 245, 250));

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton registerButton = new JButton("Register as Customer");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 13));
        registerButton.setBackground(new Color(100, 180, 100));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        mainPanel.add(titlePanel,  BorderLayout.NORTH);
        mainPanel.add(formPanel,   BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 250));
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Press Enter to login
        passwordField.addActionListener(e -> doLogin());
        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> openRegister());
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        User user = dataHandler.authenticateUser(username, password);

        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }

        // Open the correct dashboard based on user role
        this.dispose();
        if (user.getRole() == UserRole.ADMINISTRATOR) {
            new AdminFrame(user, dataHandler).setVisible(true);
        } else if (user.getRole() == UserRole.SCHEDULER) {
            new SchedulerFrame(user, dataHandler).setVisible(true);
        } else if (user.getRole() == UserRole.CUSTOMER) {
            new CustomerFrame(user, dataHandler).setVisible(true);
        } else if (user.getRole() == UserRole.MANAGER) {
            new ManagerFrame(user, dataHandler).setVisible(true);
        }
    }

    private void openRegister() {
        new RegisterFrame(dataHandler).setVisible(true);
    }
}
