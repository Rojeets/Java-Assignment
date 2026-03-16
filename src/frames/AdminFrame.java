package frames;

import data.DataHandler;
import models.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Dashboard for Administrator role
// Admin manages scheduler accounts, views all users and all bookings
public class AdminFrame extends JFrame {

    private User        currentUser;
    private DataHandler dataHandler;
    private DefaultTableModel userTableModel;
    private DefaultTableModel bookingTableModel;
    private DefaultTableModel paymentTableModel;
    private JTable userTable;
    private JTable paymentTable;

    public AdminFrame(User user, DataHandler dataHandler) {
        this.currentUser = user;
        this.dataHandler = dataHandler;
        setupUI();
        loadUsers();
        loadAllBookings();
    }

    private void setupUI() {
        setTitle("Hall Symphony - Administrator Dashboard (" + currentUser.getName() + ")");
        setSize(940, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(80, 80, 160));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName() + " (Administrator)");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        JButton logoutBtn = makeButton("Logout", new Color(200, 70, 70));
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(logoutBtn,    BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));
        tabs.addTab("Scheduler Management", buildSchedulerPanel());
        tabs.addTab("User Management",       buildUserPanel());
        tabs.addTab("All Bookings",          buildBookingsPanel());
        tabs.addTab("All Payments",          buildPaymentsPanel());

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabs,   BorderLayout.CENTER);
        add(mainPanel);

        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
    }

    // ==================== SCHEDULER MANAGEMENT TAB ====================

    private JPanel buildSchedulerPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"User ID", "Username", "Name", "Email", "Phone", "Status", "Created Date"};
        DefaultTableModel schedModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable schedTable = new JTable(schedModel);
        schedTable.setRowHeight(24);
        schedTable.setFont(new Font("Arial", Font.PLAIN, 12));
        schedTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        schedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Load schedulers
        Runnable loadSchedulers = () -> {
            schedModel.setRowCount(0);
            for (User u : dataHandler.getAllUsers()) {
                if (u.getRole() == UserRole.SCHEDULER) {
                    schedModel.addRow(new Object[]{
                        u.getUserId(), u.getUsername(), u.getName(),
                        u.getEmail(), u.getPhone(), u.isActive() ? "Active" : "Blocked", u.getCreatedDate()
                    });
                }
            }
        };
        loadSchedulers.run();

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton addBtn    = makeButton("Add Scheduler",    new Color(50, 150, 80));
        JButton editBtn   = makeButton("Edit Scheduler",   new Color(100, 130, 200));
        JButton deleteBtn = makeButton("Delete Scheduler", new Color(200, 80, 80));
        JButton blockBtn  = makeButton("Block/Unblock",    new Color(180, 130, 50));
        buttonBar.add(addBtn);
        buttonBar.add(editBtn);
        buttonBar.add(deleteBtn);
        buttonBar.add(blockBtn);

        panel.add(buttonBar,                   BorderLayout.NORTH);
        panel.add(new JScrollPane(schedTable), BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            showAddUserDialog(UserRole.SCHEDULER, loadSchedulers);
        });
        editBtn.addActionListener(e -> {
            int row = schedTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Select a scheduler first.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            String uid = (String) schedModel.getValueAt(row, 0);
            showEditUserDialog(uid, loadSchedulers);
        });
        deleteBtn.addActionListener(e -> {
            int row = schedTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Select a scheduler first.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            String uid  = (String) schedModel.getValueAt(row, 0);
            String name = (String) schedModel.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(panel, "Delete scheduler \"" + name + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dataHandler.deleteUser(uid);
                loadSchedulers.run();
            }
        });
        blockBtn.addActionListener(e -> {
            int row = schedTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Select a scheduler first.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            String uid    = (String) schedModel.getValueAt(row, 0);
            String status = (String) schedModel.getValueAt(row, 5);
            boolean shouldBlock = status.equals("Active");
            dataHandler.blockUser(uid, shouldBlock);
            loadSchedulers.run();
        });

        return panel;
    }

    // ==================== USER MANAGEMENT TAB ====================

    private JPanel buildUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.add(new JLabel("Filter by Role:"));
        String[] roles = {"All", "CUSTOMER", "SCHEDULER", "MANAGER"};
        JComboBox<String> roleFilter = new JComboBox<>(roles);
        filterBar.add(roleFilter);

        String[] cols = {"User ID", "Username", "Name", "Email", "Phone", "Role", "Status"};
        userTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userTableModel);
        userTable.setRowHeight(24);
        userTable.setFont(new Font("Arial", Font.PLAIN, 12));
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton blockBtn  = makeButton("Block/Unblock", new Color(180, 130, 50));
        JButton deleteBtn = makeButton("Delete User",   new Color(200, 80, 80));
        JButton refreshBtn= makeButton("Refresh",       new Color(130, 130, 130));
        buttonBar.add(blockBtn);
        buttonBar.add(deleteBtn);
        buttonBar.add(refreshBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        panel.add(topBar,                       BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable),  BorderLayout.CENTER);

        roleFilter.addActionListener(e -> loadUsers((String) roleFilter.getSelectedItem()));
        blockBtn.addActionListener(e -> blockSelectedUser());
        deleteBtn.addActionListener(e -> deleteSelectedUser());
        refreshBtn.addActionListener(e -> loadUsers());

        return panel;
    }

    private void loadUsers() { loadUsers("All"); }

    private void loadUsers(String roleFilter) {
        userTableModel.setRowCount(0);
        for (User u : dataHandler.getAllUsers()) {
            if (u.getRole() == UserRole.ADMINISTRATOR) continue; // Don't show admin accounts
            if (roleFilter.equals("All") || u.getRole().toString().equals(roleFilter)) {
                userTableModel.addRow(new Object[]{
                    u.getUserId(), u.getUsername(), u.getName(), u.getEmail(),
                    u.getPhone(), u.getRole(), u.isActive() ? "Active" : "Blocked"
                });
            }
        }
    }

    private void blockSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
        String uid    = (String) userTableModel.getValueAt(row, 0);
        String status = (String) userTableModel.getValueAt(row, 6);
        boolean shouldBlock = status.equals("Active");
        String name = (String) userTableModel.getValueAt(row, 2);
        String action = shouldBlock ? "block" : "unblock";
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to " + action + " user \"" + name + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataHandler.blockUser(uid, shouldBlock);
            loadUsers();
        }
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
        String uid  = (String) userTableModel.getValueAt(row, 0);
        String name = (String) userTableModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user \"" + name + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataHandler.deleteUser(uid);
            loadUsers();
        }
    }

    // ==================== ALL BOOKINGS TAB ====================

    private JPanel buildBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.add(new JLabel("Filter by Status:"));
        String[] statuses = {"All", "CONFIRMED", "CANCELLED"};
        JComboBox<String> statusFilter = new JComboBox<>(statuses);
        filterBar.add(statusFilter);

        String[] cols = {"Booking ID", "Customer", "Hall", "Event", "Date", "Start", "End", "Amount (RM)", "Status"};
        bookingTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(24);
        bookingTable.setFont(new Font("Arial", Font.PLAIN, 12));
        bookingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JButton refreshBtn = makeButton("Refresh", new Color(130, 130, 130));
        JPanel buttonBar   = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.add(refreshBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        panel.add(topBar,                         BorderLayout.NORTH);
        panel.add(new JScrollPane(bookingTable),  BorderLayout.CENTER);

        statusFilter.addActionListener(e -> loadAllBookings((String) statusFilter.getSelectedItem()));
        refreshBtn.addActionListener(e -> loadAllBookings());

        return panel;
    }

    private void loadAllBookings() { loadAllBookings("All"); }

    private void loadAllBookings(String filter) {
        bookingTableModel.setRowCount(0);
        for (Booking b : dataHandler.getAllBookings()) {
            if (filter.equals("All") || b.getStatus().equals(filter)) {
                bookingTableModel.addRow(new Object[]{
                    b.getBookingId(), b.getCustomerName(), b.getHallName(), b.getEventName(),
                    b.getBookingDate(), b.getStartTime(), b.getEndTime(),
                    String.format("%.2f", b.getTotalAmount()), b.getStatus()
                });
            }
        }
    }

    // ==================== SHARED DIALOGS ====================

    private void showAddUserDialog(UserRole role, Runnable reload) {
        JDialog dialog = new JDialog(this, "Add New " + role.toString(), true);
        dialog.setSize(380, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField nameField = new JTextField();
        JTextField userField = new JTextField();
        JTextField emailField= new JTextField();
        JTextField phoneField= new JTextField();
        JPasswordField passField = new JPasswordField();

        panel.add(new JLabel("Full Name *:")); panel.add(nameField);
        panel.add(new JLabel("Username *:")); panel.add(userField);
        panel.add(new JLabel("Password *:")); panel.add(passField);
        panel.add(new JLabel("Email:"));      panel.add(emailField);
        panel.add(new JLabel("Phone:"));      panel.add(phoneField);

        JButton saveBtn   = makeButton("Save",   new Color(50, 150, 80));
        JButton cancelBtn = makeButton("Cancel", new Color(180, 80, 80));
        panel.add(saveBtn); panel.add(cancelBtn);

        dialog.add(panel);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name, Username, and Password are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (dataHandler.isUsernameTaken(user)) {
                JOptionPane.showMessageDialog(dialog, "Username already taken!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String uid = dataHandler.generateUserId(role);
            User newUser = new User(uid, user, pass, name, emailField.getText().trim(), phoneField.getText().trim(), role);
            if (dataHandler.registerUser(newUser)) {
                JOptionPane.showMessageDialog(dialog, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                reload.run();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showEditUserDialog(String userId, Runnable reload) {
        User user = null;
        for (User u : dataHandler.getAllUsers()) {
            if (u.getUserId().equals(userId)) { user = u; break; }
        }
        if (user == null) return;

        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setSize(360, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField nameField  = new JTextField(user.getName());
        JTextField emailField = new JTextField(user.getEmail());
        JTextField phoneField = new JTextField(user.getPhone());

        panel.add(new JLabel("Full Name:")); panel.add(nameField);
        panel.add(new JLabel("Email:"));     panel.add(emailField);
        panel.add(new JLabel("Phone:"));     panel.add(phoneField);

        JButton saveBtn   = makeButton("Save",   new Color(50, 150, 80));
        JButton cancelBtn = makeButton("Cancel", new Color(180, 80, 80));
        panel.add(saveBtn); panel.add(cancelBtn);

        final User finalUser = user;
        dialog.add(panel);
        saveBtn.addActionListener(e -> {
            finalUser.setName(nameField.getText().trim());
            finalUser.setEmail(emailField.getText().trim());
            finalUser.setPhone(phoneField.getText().trim());
            if (dataHandler.updateUser(finalUser)) {
                JOptionPane.showMessageDialog(dialog, "User updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                reload.run();
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
        JButton exportBtn = makeButton("Export Report", new Color(50, 130, 80));
        buttonBar.add(refreshBtn);
        buttonBar.add(exportBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(paymentTable), BorderLayout.CENTER);

        filterCombo.addActionListener(e -> loadPayments((String) filterCombo.getSelectedItem()));
        refreshBtn.addActionListener(e -> loadPayments());
        exportBtn.addActionListener(e -> exportPaymentReport());

        filterBar.add(filterCombo);

        loadPayments();

        return panel;
    }

    private void loadPayments() {
        loadPayments("All");
    }

    private void loadPayments(String filter) {
        paymentTableModel.setRowCount(0);
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
            }
        }
    }

    private void exportPaymentReport() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Payment report exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
