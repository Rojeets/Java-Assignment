package frames;

import data.DataHandler;
import models.User;
import models.UserRole;

import javax.swing.*;
import java.awt.*;

// Registration screen for new customers
public class RegisterFrame extends JFrame {

    private DataHandler dataHandler;
    private JTextField  nameField, usernameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;

    public RegisterFrame(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        setupUI();
    }

    private void setupUI() {
        setTitle("Hall Symphony - Customer Registration");
        setSize(450, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(245, 248, 245));

        // Title
        JLabel titleLabel = new JLabel("New Customer Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 130, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Form
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 10));
        formPanel.setBackground(new Color(245, 248, 245));

        Font labelFont = new Font("Arial", Font.PLAIN, 13);
        Font fieldFont = new Font("Arial", Font.PLAIN, 13);

        nameField            = new JTextField();
        usernameField        = new JTextField();
        emailField           = new JTextField();
        phoneField           = new JTextField();
        passwordField        = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        JTextField[] fields = {nameField, usernameField, emailField, phoneField, passwordField, confirmPasswordField};
        for (JTextField f : fields) {
            f.setFont(fieldFont);
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        }

        String[] labels = {"Full Name *", "Username *", "Email", "Phone", "Password *", "Confirm Password *"};
        JTextField[] inputFields = {nameField, usernameField, emailField, phoneField, passwordField, confirmPasswordField};

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(labelFont);
            formPanel.add(lbl);
            formPanel.add(inputFields[i]);
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(new Color(245, 248, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(50, 150, 80));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Arial", Font.BOLD, 13));
        registerBtn.setFocusPainted(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(180, 80, 80));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 13));
        cancelBtn.setFocusPainted(false);

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(titleLabel,   BorderLayout.NORTH);
        mainPanel.add(formPanel,    BorderLayout.CENTER);
        mainPanel.add(buttonPanel,  BorderLayout.SOUTH);

        add(mainPanel);

        registerBtn.addActionListener(e -> doRegister());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void doRegister() {
        String name     = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmPasswordField.getPassword());

        // Basic validation
        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Username, and Password are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (dataHandler.isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(this, "Username already taken. Please choose another.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = dataHandler.generateUserId(UserRole.CUSTOMER);
        User newUser = new User(userId, username, password, name, email, phone, UserRole.CUSTOMER);
        boolean success = dataHandler.registerUser(newUser);

        if (success) {
            JOptionPane.showMessageDialog(this,
                "Registration successful!\nYou can now login with your username and password.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
