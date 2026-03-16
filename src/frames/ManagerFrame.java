package frames;

import data.DataHandler;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

// Dashboard for Manager role
// Manager views sales reports (weekly/monthly/yearly) and manages customer issues
public class ManagerFrame extends JFrame {

    private User        currentUser;
    private DataHandler dataHandler;
    private DefaultTableModel issueTableModel;
    private DefaultTableModel paymentTableModel;
    private JTable issueTable;
    private JTable paymentTable;

    public ManagerFrame(User user, DataHandler dataHandler) {
        this.currentUser = user;
        this.dataHandler = dataHandler;
        setupUI();
        loadIssues();
    }

    private void setupUI() {
        setTitle("Hall Symphony - Manager Dashboard (" + currentUser.getName() + ")");
        setSize(940, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(140, 80, 160));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName() + " (Manager)");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        JButton logoutBtn = makeButton("Logout", new Color(200, 70, 70));
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(logoutBtn,    BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));
        tabs.addTab("Sales Dashboard",      buildSalesPanel());
        tabs.addTab("Issue Management",     buildIssuePanel());
        tabs.addTab("All Payments",        buildPaymentsPanel());

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabs,   BorderLayout.CENTER);
        add(mainPanel);

        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
    }

    // ==================== SALES DASHBOARD TAB ====================

    private JPanel buildSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(new JLabel("View Sales:"));
        String[] periodOptions = {"Weekly", "Monthly", "Yearly"};
        JComboBox<String> periodCombo = new JComboBox<>(periodOptions);
        periodCombo.setSelectedItem("Monthly");
        filterPanel.add(periodCombo);
        JButton viewBtn = makeButton("View Report", new Color(80, 80, 160));
        filterPanel.add(viewBtn);

        // Summary labels
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        JLabel totalLabel   = new JLabel("Total Bookings: --", SwingConstants.CENTER);
        JLabel revenueLabel = new JLabel("Total Revenue: RM --", SwingConstants.CENTER);
        JLabel cancelLabel  = new JLabel("Cancelled: --", SwingConstants.CENTER);
        Font bigFont = new Font("Arial", Font.BOLD, 14);
        totalLabel.setFont(bigFont);
        revenueLabel.setFont(bigFont);
        cancelLabel.setFont(bigFont);
        revenueLabel.setForeground(new Color(50, 130, 50));
        cancelLabel.setForeground(new Color(180, 60, 60));
        summaryPanel.add(totalLabel);
        summaryPanel.add(revenueLabel);
        summaryPanel.add(cancelLabel);

        // Bookings table
        String[] cols = {"Booking ID", "Customer", "Hall", "Event", "Date", "Hours", "Amount (RM)", "Status"};
        DefaultTableModel salesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable salesTable = new JTable(salesModel);
        salesTable.setRowHeight(24);
        salesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        salesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JPanel topSection = new JPanel(new BorderLayout(5, 5));
        topSection.add(filterPanel,  BorderLayout.NORTH);
        topSection.add(summaryPanel, BorderLayout.CENTER);

        panel.add(topSection,                    BorderLayout.NORTH);
        panel.add(new JScrollPane(salesTable),   BorderLayout.CENTER);

        // Load sales based on period
        Runnable loadSales = () -> {
            salesModel.setRowCount(0);
            String period = (String) periodCombo.getSelectedItem();
            LocalDate now   = LocalDate.now();
            LocalDate from;

            if (period.equals("Weekly")) {
                from = now.minusDays(7);
            } else if (period.equals("Monthly")) {
                from = now.minusDays(30);
            } else { // Yearly
                from = now.minusDays(365);
            }

            int totalCount  = 0;
            double revenue  = 0;
            int cancelled   = 0;

            for (Booking b : dataHandler.getAllBookings()) {
                if (!b.getBookingDate().isBefore(from) && !b.getBookingDate().isAfter(now)) {
                    salesModel.addRow(new Object[]{
                        b.getBookingId(), b.getCustomerName(), b.getHallName(), b.getEventName(),
                        b.getBookingDate(), b.getHours(), String.format("%.2f", b.getTotalAmount()), b.getStatus()
                    });
                    totalCount++;
                    if (b.getStatus().equals("CONFIRMED")) revenue += b.getTotalAmount();
                    if (b.getStatus().equals("CANCELLED"))  cancelled++;
                }
            }
            totalLabel.setText("Total Bookings: " + totalCount);
            revenueLabel.setText("Total Revenue: RM " + String.format("%.2f", revenue));
            cancelLabel.setText("Cancelled: " + cancelled);
        };

        viewBtn.addActionListener(e -> loadSales.run());
        loadSales.run(); // Load monthly view on start

        return panel;
    }

    // ==================== ISSUE MANAGEMENT TAB ====================

    private JPanel buildIssuePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.add(new JLabel("Filter by Status:"));
        String[] statuses = {"All", "OPEN", "IN_PROGRESS", "DONE", "CLOSED", "CANCELLED"};
        JComboBox<String> statusFilter = new JComboBox<>(statuses);
        filterBar.add(statusFilter);

        String[] cols = {"Issue ID", "Customer", "Hall", "Booking ID", "Description", "Priority", "Status", "Assigned To", "Date"};
        issueTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        issueTable = new JTable(issueTableModel);
        issueTable.setRowHeight(24);
        issueTable.setFont(new Font("Arial", Font.PLAIN, 12));
        issueTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton respondBtn  = makeButton("Respond to Issue", new Color(80, 80, 160));
        JButton refreshBtn  = makeButton("Refresh",          new Color(130, 130, 130));
        buttonBar.add(respondBtn);
        buttonBar.add(refreshBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        panel.add(topBar,                        BorderLayout.NORTH);
        panel.add(new JScrollPane(issueTable),   BorderLayout.CENTER);

        statusFilter.addActionListener(e -> loadIssues((String) statusFilter.getSelectedItem()));
        respondBtn.addActionListener(e  -> respondToIssue());
        refreshBtn.addActionListener(e  -> loadIssues());

        return panel;
    }

    private void loadIssues() { loadIssues("All"); }

    private void loadIssues(String filter) {
        issueTableModel.setRowCount(0);
        for (Issue i : dataHandler.getAllIssues()) {
            if (filter.equals("All") || i.getStatus().equals(filter)) {
                issueTableModel.addRow(new Object[]{
                    i.getIssueId(), i.getCustomerName(), i.getHallName(),
                    i.getBookingId(), i.getDescription(), i.getPriority(),
                    i.getStatus(), i.getAssignedTo(), i.getCreatedDate()
                });
            }
        }
    }

    private void respondToIssue() {
        int row = issueTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an issue to respond to.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String issueId = (String) issueTableModel.getValueAt(row, 0);

        // Find the issue object
        Issue selectedIssue = null;
        for (Issue i : dataHandler.getAllIssues()) {
            if (i.getIssueId().equals(issueId)) { selectedIssue = i; break; }
        }
        if (selectedIssue == null) return;

        JDialog dialog = new JDialog(this, "Respond to Issue: " + issueId, true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Info display
        panel.add(new JLabel("Issue ID:"));
        panel.add(new JLabel(selectedIssue.getIssueId()));

        panel.add(new JLabel("Description:"));
        JTextArea descArea = new JTextArea(selectedIssue.getDescription());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        panel.add(new JScrollPane(descArea));

        // Status dropdown
        String[] statusOptions = {"OPEN", "IN_PROGRESS", "DONE", "CLOSED", "CANCELLED"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setSelectedItem(selectedIssue.getStatus());
        panel.add(new JLabel("Update Status:"));
        panel.add(statusCombo);

        // Assign to scheduler
        List<User> schedulers = dataHandler.getAllUsers();
        String[] schedulerNames = {"(Not Assigned)"};
        java.util.List<String> names = new java.util.ArrayList<>();
        names.add("(Not Assigned)");
        for (User u : schedulers) {
            if (u.getRole() == UserRole.SCHEDULER && u.isActive()) names.add(u.getName());
        }
        schedulerNames = names.toArray(new String[0]);
        JComboBox<String> assignCombo = new JComboBox<>(schedulerNames);
        if (!selectedIssue.getAssignedTo().isEmpty()) assignCombo.setSelectedItem(selectedIssue.getAssignedTo());
        panel.add(new JLabel("Assign to Scheduler:"));
        panel.add(assignCombo);

        JTextField notesField = new JTextField(selectedIssue.getResolutionNotes());
        panel.add(new JLabel("Resolution Notes:"));
        panel.add(notesField);

        JButton saveBtn   = makeButton("Save Response", new Color(80, 80, 160));
        JButton cancelBtn = makeButton("Cancel",        new Color(180, 80, 80));
        panel.add(saveBtn); panel.add(cancelBtn);

        final Issue finalIssue = selectedIssue;
        dialog.add(panel);
        saveBtn.addActionListener(e -> {
            finalIssue.setStatus((String) statusCombo.getSelectedItem());
            String assigned = (String) assignCombo.getSelectedItem();
            finalIssue.setAssignedTo(assigned.equals("(Not Assigned)") ? "" : assigned);
            finalIssue.setResolutionNotes(notesField.getText().trim());
            if (dataHandler.updateIssue(finalIssue)) {
                JOptionPane.showMessageDialog(dialog, "Issue updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadIssues();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        return btn;
    }

    // ==================== ALL PAYMENTS TAB ====================

    private JPanel buildPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Booking ID", "Customer", "Hall Name", "Event", "Date", "Amount (RM)", "Payment Status", "Transaction ID", "Payment Date"};
        paymentTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        paymentTable = new JTable(paymentTableModel);
        paymentTable.setRowHeight(24);
        paymentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        paymentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.add(new JLabel("Filter:"));
        String[] filters = {"All", "PENDING", "PAID"};
        JComboBox<String> filterCombo = new JComboBox<>(filters);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton refreshBtn = makeButton("Refresh", new Color(130, 130, 130));
        JButton reportBtn = makeButton("Generate Report", new Color(50, 130, 80));
        buttonBar.add(refreshBtn);
        buttonBar.add(reportBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(paymentTable), BorderLayout.CENTER);

        filterCombo.addActionListener(e -> loadPayments((String) filterCombo.getSelectedItem()));
        refreshBtn.addActionListener(e -> loadPayments());
        reportBtn.addActionListener(e -> generatePaymentReport());

        filterBar.add(filterCombo);

        loadPayments();

        return panel;
    }

    private void loadPayments() {
        loadPayments("All");
    }

    private void loadPayments(String filter) {
        paymentTableModel.setRowCount(0);
        double totalPending = 0;
        double totalPaid = 0;
        
        for (Booking b : dataHandler.getAllBookings()) {
            String payStatus = b.getPaymentStatus() != null ? b.getPaymentStatus() : "PENDING";
            if (filter.equals("All") || payStatus.equals(filter)) {
                paymentTableModel.addRow(new Object[]{
                    b.getBookingId(), b.getCustomerName(), b.getHallName(), b.getEventName(),
                    b.getBookingDate(), String.format("%.2f", b.getTotalAmount()),
                    payStatus, 
                    b.getTransactionId() != null ? b.getTransactionId() : "-",
                    b.getPaymentDate() != null ? b.getPaymentDate().toString() : "-"
                });
                if ("PAID".equals(payStatus)) {
                    totalPaid += b.getTotalAmount();
                } else {
                    totalPending += b.getTotalAmount();
                }
            }
        }
    }

    private void generatePaymentReport() {
        JOptionPane.showMessageDialog(this, "Payment report generated!\nTotal Paid: RM " + 
            String.format("%.2f", calculateTotalPaid()) + "\nTotal Pending: RM " + 
            String.format("%.2f", calculateTotalPending()),
            "Payment Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private double calculateTotalPaid() {
        double total = 0;
        for (Booking b : dataHandler.getAllBookings()) {
            if ("PAID".equals(b.getPaymentStatus())) {
                total += b.getTotalAmount();
            }
        }
        return total;
    }

    private double calculateTotalPending() {
        double total = 0;
        for (Booking b : dataHandler.getAllBookings()) {
            String payStatus = b.getPaymentStatus() != null ? b.getPaymentStatus() : "PENDING";
            if ("PENDING".equals(payStatus)) {
                total += b.getTotalAmount();
            }
        }
        return total;
    }
}
