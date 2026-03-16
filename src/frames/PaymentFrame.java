package frames;

import models.Booking;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class PaymentFrame extends JFrame {

    private Booking booking;
    private JFrame parent;
    private boolean paymentSuccessful = false;
    private String transactionId;

    public PaymentFrame(JFrame parent, Booking booking) {
        this.parent = parent;
        this.booking = booking;
        setupUI();
    }

    private void setupUI() {
        setTitle("Payment - Hall Symphony");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setResizable(false);

        Font boldFont = new Font("Arial", Font.BOLD, 14);
        Font plainFont = new Font("Arial", Font.PLAIN, 13);
        Font titleFont = new Font("Arial", Font.BOLD, 18);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel("MAKE PAYMENT", SwingConstants.CENTER);
        titleLbl.setFont(titleFont);
        titleLbl.setForeground(new Color(50, 130, 80));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLbl);

        JPanel summaryPanel = new JPanel(new GridLayout(5, 2, 5, 8));
        summaryPanel.setBackground(new Color(248, 248, 248));
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Booking Summary", javax.swing.border.TitledBorder.LEFT, 
            javax.swing.border.TitledBorder.TOP, boldFont, new Color(50, 130, 80)));
        summaryPanel.setMaximumSize(new Dimension(410, 130));

        addLabelField(summaryPanel, "Booking ID:", booking.getBookingId(), plainFont, true);
        addLabelField(summaryPanel, "Hall Name:", booking.getHallName(), plainFont, true);
        addLabelField(summaryPanel, "Event:", booking.getEventName(), plainFont, true);
        addLabelField(summaryPanel, "Date:", booking.getBookingDate().toString(), plainFont, true);
        
        JLabel amountTitle = new JLabel("Total Amount:");
        amountTitle.setFont(boldFont);
        amountTitle.setForeground(new Color(50, 130, 80));
        JLabel amountValue = new JLabel("RM " + String.format("%.2f", booking.getTotalAmount()));
        amountValue.setFont(new Font("Arial", Font.BOLD, 15));
        amountValue.setForeground(new Color(50, 130, 80));
        summaryPanel.add(amountTitle);
        summaryPanel.add(amountValue);

        mainPanel.add(summaryPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        methodPanel.setBackground(Color.WHITE);
        methodPanel.setMaximumSize(new Dimension(410, 40));
        JLabel methodLabel = new JLabel("Payment Method:");
        methodLabel.setFont(boldFont);
        String[] methods = {"Credit Card", "Debit Card", "Online Banking"};
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        methodCombo.setFont(plainFont);
        methodCombo.setPreferredSize(new Dimension(200, 30));
        methodPanel.add(methodLabel);
        methodPanel.add(methodCombo);
        mainPanel.add(methodPanel);

        mainPanel.add(Box.createVerticalStrut(10));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Card Details", javax.swing.border.TitledBorder.LEFT, 
            javax.swing.border.TitledBorder.TOP, boldFont, new Color(50, 130, 80)));
        cardPanel.setMaximumSize(new Dimension(410, 160));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField cardNumberField = new JTextField();
        cardNumberField.setFont(plainFont);
        cardNumberField.setPreferredSize(new Dimension(200, 30));

        JTextField cardHolderField = new JTextField();
        cardHolderField.setFont(plainFont);
        cardHolderField.setPreferredSize(new Dimension(200, 30));

        JComboBox<String> expMonthCombo = new JComboBox<>();
        JComboBox<String> expYearCombo = new JComboBox<>();
        JPasswordField cvvField = new JPasswordField();
        cvvField.setFont(plainFont);
        cvvField.setPreferredSize(new Dimension(80, 30));

        for (int i = 1; i <= 12; i++) {
            expMonthCombo.addItem(String.format("%02d", i));
        }
        int currentYear = LocalDateTime.now().getYear();
        for (int i = currentYear; i <= currentYear + 10; i++) {
            expYearCombo.addItem(String.valueOf(i));
        }

        JPanel expPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        expPanel.setBackground(Color.WHITE);
        expMonthCombo.setPreferredSize(new Dimension(60, 30));
        expYearCombo.setPreferredSize(new Dimension(80, 30));
        expPanel.add(expMonthCombo);
        expPanel.add(new JLabel("/"));
        expPanel.add(expYearCombo);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        cardPanel.add(new JLabel("Card Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cardPanel.add(cardNumberField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        cardPanel.add(new JLabel("Card Holder:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cardPanel.add(cardHolderField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        cardPanel.add(new JLabel("Expiry Date:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cardPanel.add(expPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        cardPanel.add(new JLabel("CVV:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cardPanel.add(cvvField, gbc);

        mainPanel.add(cardPanel);

        mainPanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setMaximumSize(new Dimension(410, 50));

        JButton payBtn = makeButton("Pay Now", new Color(50, 130, 80));
        payBtn.setPreferredSize(new Dimension(130, 38));
        
        JButton cancelBtn = makeButton("Cancel", new Color(180, 80, 80));
        cancelBtn.setPreferredSize(new Dimension(100, 38));

        buttonPanel.add(payBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel);

        add(mainPanel);

        payBtn.addActionListener(e -> {
            if (!validatePayment(cardNumberField, cardHolderField, cvvField)) {
                return;
            }
            processPayment();
        });

        cancelBtn.addActionListener(e -> {
            dispose();
            parent.setEnabled(true);
            parent.toFront();
        });

        setVisible(true);
    }

    private void addLabelField(JPanel panel, String label, String value, Font font, boolean editable) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(font);
        JTextField field = new JTextField(value);
        field.setFont(font);
        field.setEditable(editable);
        if (!editable) {
            field.setBackground(new Color(240, 240, 240));
        }
        panel.add(lbl);
        panel.add(field);
    }

    private boolean validatePayment(JTextField cardNumber, JTextField cardHolder, JPasswordField cvv) {
        String cardNum = cardNumber.getText().trim().replace(" ", "");
        String holder = cardHolder.getText().trim();
        String cvvStr = new String(cvv.getPassword());

        if (cardNum.isEmpty() || cardNum.length() < 13 || cardNum.length() > 19) {
            JOptionPane.showMessageDialog(this, "Please enter a valid card number (13-19 digits)!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!cardNum.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Card number must contain only digits!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (holder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter card holder name!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (cvvStr.isEmpty() || cvvStr.length() < 3 || cvvStr.length() > 4) {
            JOptionPane.showMessageDialog(this, "Please enter a valid CVV (3-4 digits)!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void processPayment() {
        try {
            setVisible(false);
            
            JOptionPane.showMessageDialog(this, "Processing payment...", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
            
            Thread.sleep(1500);
            
            transactionId = "TXN" + System.currentTimeMillis();
            paymentSuccessful = true;
            
            JOptionPane.showMessageDialog(this, 
                "Payment Successful!\n\nTransaction ID: " + transactionId + 
                "\nAmount Paid: RM " + String.format("%.2f", booking.getTotalAmount()),
                "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            parent.setEnabled(true);
            parent.toFront();
            
        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(this, "Payment interrupted!", "Error", JOptionPane.ERROR_MESSAGE);
            setVisible(true);
        }
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    public String getTransactionId() {
        return transactionId;
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
