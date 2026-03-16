package frames;

import data.DataHandler;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Dashboard for Customer role
// Customers can book halls, view their bookings, cancel bookings, and raise issues
public class CustomerFrame extends JFrame {

    private User currentUser;
    private DataHandler dataHandler;
    private DefaultTableModel bookingTableModel;
    private DefaultTableModel issueTableModel;
    private DefaultTableModel paymentTableModel;
    private JTable bookingTable;
    private JTable paymentTable;

    public CustomerFrame(User user, DataHandler dataHandler) {
        this.currentUser = user;
        this.dataHandler = dataHandler;
        setupUI();
        loadMyBookings();
        loadMyIssues();
    }

    private void setupUI() {
        setTitle("Hall Symphony - Customer Dashboard (" + currentUser.getName() + ")");
        setSize(900, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(50, 130, 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName() + " (Customer)");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        topBtns.setOpaque(false);
        JButton profileBtn = makeButton("My Profile", new Color(80, 160, 110));
        JButton logoutBtn  = makeButton("Logout",     new Color(200, 70, 70));
        topBtns.add(profileBtn);
        topBtns.add(logoutBtn);
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(topBtns, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));
        tabs.addTab("Book a Hall",      buildBookHallPanel());
        tabs.addTab("My Bookings",      buildMyBookingsPanel());
        tabs.addTab("My Payments",     buildMyPaymentsPanel());
        tabs.addTab("My Issues",        buildMyIssuesPanel());

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabs,   BorderLayout.CENTER);

        add(mainPanel);

        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        profileBtn.addActionListener(e -> showProfileDialog());
    }

    // ==================== BOOK A HALL TAB ====================

    private JPanel buildBookHallPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel info = new JLabel("Select a hall to book. Business hours: 8:00 AM - 6:00 PM. Cancellations allowed up to 3 days before.");
        info.setFont(new Font("Arial", Font.ITALIC, 12));
        info.setForeground(new Color(90, 90, 90));

        // Available halls table
        String[] cols = {"Hall ID", "Hall Name", "Type", "Capacity", "Rate/Hour (RM)", "Description"};
        DefaultTableModel availModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable availTable = new JTable(availModel);
        availTable.setRowHeight(24);
        availTable.setFont(new Font("Arial", Font.PLAIN, 12));
        availTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        availTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Load available halls
        for (Hall h : dataHandler.getAllHalls()) {
            if (h.isAvailable()) {
                availModel.addRow(new Object[]{
                    h.getHallId(), h.getHallName(), h.getHallType().getDisplayName(),
                    h.getCapacity(), String.format("%.2f", h.getRatePerHour()), h.getDescription()
                });
            }
        }

        JScrollPane scrollPane = new JScrollPane(availTable);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton bookBtn = makeButton("Book Selected Hall", new Color(50, 130, 80));
        bottomBar.add(bookBtn);

        panel.add(info,       BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomBar,  BorderLayout.SOUTH);

        bookBtn.addActionListener(e -> {
            int row = availTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Please select a hall first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String hallId   = (String) availModel.getValueAt(row, 0);
            String hallName = (String) availModel.getValueAt(row, 1);
            String hallType = (String) availModel.getValueAt(row, 2);
            double rate     = dataHandler.getAllHalls().stream()
                                .filter(h -> h.getHallId().equals(hallId))
                                .mapToDouble(Hall::getRatePerHour).findFirst().orElse(0);
            showBookingDialog(hallId, hallName, hallType, rate);
        });

        return panel;
    }

    private void showBookingDialog(String hallId, String hallName, String hallType, double ratePerHour) {
        JDialog dialog = new JDialog(this, "Book Hall: " + hallName, true);
        dialog.setSize(430, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        JTextField eventNameField = new JTextField();
        JTextField eventDescField = new JTextField();
        JTextField dateField      = new JTextField(LocalDate.now().plusDays(1).toString());

        String[] times = {"08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00"};
        JComboBox<String> startCombo = new JComboBox<>(times);
        JComboBox<String> endCombo   = new JComboBox<>(times);
        endCombo.setSelectedIndex(2);

        JLabel priceLabel = new JLabel("---");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 13));
        priceLabel.setForeground(new Color(50, 130, 80));

        // Update price estimate on time change
        Runnable updatePrice = () -> {
            try {
                LocalTime st = LocalTime.parse((String) startCombo.getSelectedItem());
                LocalTime et = LocalTime.parse((String) endCombo.getSelectedItem());
                int h = et.getHour() - st.getHour();
                if (h > 0) priceLabel.setText(String.format("RM %.2f (%d hour(s))", h * ratePerHour, h));
                else priceLabel.setText("Invalid time range");
            } catch (Exception ex) { priceLabel.setText("---"); }
        };
        startCombo.addActionListener(e -> updatePrice.run());
        endCombo.addActionListener(e   -> updatePrice.run());
        updatePrice.run();

        panel.add(new JLabel("Event Name *:")); panel.add(eventNameField);
        panel.add(new JLabel("Description:")); panel.add(eventDescField);
        panel.add(new JLabel("Booking Date (yyyy-MM-dd) *:")); panel.add(dateField);
        panel.add(new JLabel("Start Time *:")); panel.add(startCombo);
        panel.add(new JLabel("End Time *:"));   panel.add(endCombo);
        panel.add(new JLabel("Estimated Cost:")); panel.add(priceLabel);

        JButton confirmBtn = makeButton("Confirm Booking", new Color(50, 130, 80));
        JButton cancelBtn  = makeButton("Cancel",          new Color(180, 80, 80));
        panel.add(confirmBtn); panel.add(cancelBtn);

        dialog.add(panel);
        confirmBtn.addActionListener(e -> {
            try {
                String eventName = eventNameField.getText().trim();
                if (eventName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Event name is required!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LocalDate bookDate  = LocalDate.parse(dateField.getText().trim());
                LocalTime startTime = LocalTime.parse((String) startCombo.getSelectedItem());
                LocalTime endTime   = LocalTime.parse((String) endCombo.getSelectedItem());

                if (bookDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "Booking date cannot be in the past!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!endTime.isAfter(startTime)) {
                    JOptionPane.showMessageDialog(dialog, "End time must be after start time!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Check business hours
                if (startTime.isBefore(LocalTime.of(8, 0)) || endTime.isAfter(LocalTime.of(18, 0))) {
                    JOptionPane.showMessageDialog(dialog, "Booking must be within business hours (8:00 - 18:00)!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!dataHandler.isHallAvailable(hallId, bookDate, startTime, endTime)) {
                    JOptionPane.showMessageDialog(dialog, "Hall is not available at this time. Please choose another slot.", "Unavailable", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String bookingId = dataHandler.generateBookingId();
                final Booking booking  = new Booking(bookingId, currentUser.getUserId(),
                    currentUser.getName(), hallId, hallName, hallType,
                    bookDate, startTime, endTime, ratePerHour,
                    eventName, eventDescField.getText().trim());

                dialog.dispose();
                setEnabled(false);
                PaymentFrame paymentFrame = new PaymentFrame(this, booking);
                
                paymentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        if (paymentFrame.isPaymentSuccessful()) {
                            booking.setPaymentInfo(paymentFrame.getTransactionId());
                            if (dataHandler.addBooking(booking)) {
                                showReceipt(booking);
                                loadMyBookings();
                                loadMyPayments();
                            }
                        } else {
                            setEnabled(true);
                            toFront();
                        }
                    }
                });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // Show payment receipt as a JFrame (as required by assignment)
    private void showReceipt(Booking booking) {
        JFrame receiptFrame = new JFrame("Booking Receipt - " + booking.getBookingId());
        receiptFrame.setSize(420, 480);
        receiptFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);

        Font boldFont  = new Font("Arial", Font.BOLD, 14);
        Font plainFont = new Font("Arial", Font.PLAIN, 13);

        JLabel titleLbl = new JLabel("===  BOOKING RECEIPT  ===", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 18));
        titleLbl.setForeground(new Color(50, 130, 80));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        String[][] receiptData = {
            {"Booking ID:",      booking.getBookingId()},
            {"Customer:",        booking.getCustomerName()},
            {"Hall Name:",       booking.getHallName()},
            {"Hall Type:",       booking.getHallType()},
            {"Event Name:",      booking.getEventName()},
            {"Date:",            booking.getBookingDate().toString()},
            {"Start Time:",      booking.getStartTime().toString()},
            {"End Time:",        booking.getEndTime().toString()},
            {"Duration:",        booking.getHours() + " hour(s)"},
            {"Total Amount:",    String.format("RM %.2f", booking.getTotalAmount())},
            {"Status:",          booking.getStatus()},
            {"Booked On:",       booking.getCreatedDate().toString()},
        };

        for (String[] row : receiptData) {
            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setBackground(Color.WHITE);
            JLabel keyLbl = new JLabel(row[0]);
            keyLbl.setFont(boldFont);
            keyLbl.setPreferredSize(new Dimension(130, 22));
            JLabel valLbl = new JLabel(row[1]);
            valLbl.setFont(plainFont);
            rowPanel.add(keyLbl, BorderLayout.WEST);
            rowPanel.add(valLbl, BorderLayout.CENTER);
            panel.add(rowPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        JLabel thankYou = new JLabel("Thank you for your booking!", SwingConstants.CENTER);
        thankYou.setFont(new Font("Arial", Font.ITALIC, 13));
        thankYou.setForeground(Color.GRAY);
        thankYou.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(thankYou);

        JButton closeBtn = makeButton("Close", new Color(100, 100, 180));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> receiptFrame.dispose());
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(closeBtn);

        receiptFrame.add(new JScrollPane(panel));
        receiptFrame.setVisible(true);
    }

    // ==================== MY BOOKINGS TAB ====================

    private JPanel buildMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Booking ID", "Hall Name", "Type", "Event", "Date", "Start", "End", "Hours", "Amount (RM)", "Status", "Payment"};
        bookingTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(24);
        bookingTable.setFont(new Font("Arial", Font.PLAIN, 12));
        bookingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(bookingTable);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.add(new JLabel("Filter:"));
        String[] filters = {"All", "CONFIRMED", "CANCELLED"};
        JComboBox<String> filterCombo = new JComboBox<>(filters);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton payBtn    = makeButton("Make Payment",    new Color(50, 130, 200));
        JButton cancelBtn = makeButton("Cancel Booking",   new Color(200, 80, 80));
        JButton issueBtn  = makeButton("Raise Issue",      new Color(200, 130, 50));
        JButton refreshBtn = makeButton("Refresh",        new Color(130, 130, 130));
        buttonBar.add(payBtn);
        buttonBar.add(cancelBtn);
        buttonBar.add(issueBtn);
        buttonBar.add(refreshBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        panel.add(topBar,     BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        filterCombo.addActionListener(e -> loadMyBookings((String) filterCombo.getSelectedItem()));
        payBtn.addActionListener(e    -> makePaymentOnBooking());
        cancelBtn.addActionListener(e  -> cancelSelectedBooking());
        issueBtn.addActionListener(e   -> raiseIssueOnBooking());
        refreshBtn.addActionListener(e -> loadMyBookings());

        filterBar.add(filterCombo);

        return panel;
    }

    private void loadMyBookings() {
        loadMyBookings("All");
    }

    private void loadMyBookings(String filter) {
        bookingTableModel.setRowCount(0);
        for (Booking b : dataHandler.getBookingsByCustomer(currentUser.getUserId())) {
            if (filter.equals("All") || b.getStatus().equals(filter)) {
                String payStatus = b.getPaymentStatus() != null ? b.getPaymentStatus() : "PENDING";
                bookingTableModel.addRow(new Object[]{
                    b.getBookingId(), b.getHallName(), b.getHallType(), b.getEventName(),
                    b.getBookingDate(), b.getStartTime(), b.getEndTime(),
                    b.getHours(), String.format("%.2f", b.getTotalAmount()), b.getStatus(), payStatus
                });
            }
        }
    }

    private void cancelSelectedBooking() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String bookingId = (String) bookingTableModel.getValueAt(row, 0);
        String status    = (String) bookingTableModel.getValueAt(row, 9);
        if (!status.equals("CONFIRMED")) {
            JOptionPane.showMessageDialog(this, "Only confirmed bookings can be cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Check 3-day rule
        Object dateObj = bookingTableModel.getValueAt(row, 4);
        LocalDate bookDate = LocalDate.parse(dateObj.toString());
        if (LocalDate.now().plusDays(3).isAfter(bookDate)) {
            JOptionPane.showMessageDialog(this, "Cancellation is only allowed at least 3 days before the booking date.", "Policy", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel booking " + bookingId + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dataHandler.cancelBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMyBookings();
            }
        }
    }

    private void makePaymentOnBooking() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to make payment.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String bookingId = (String) bookingTableModel.getValueAt(row, 0);
        String paymentStatus = (String) bookingTableModel.getValueAt(row, 10);
        
        if ("PAID".equals(paymentStatus)) {
            JOptionPane.showMessageDialog(this, "This booking has already been paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if ("CANCELLED".equals(bookingTableModel.getValueAt(row, 9))) {
            JOptionPane.showMessageDialog(this, "Cannot make payment for cancelled bookings.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        final Booking booking = dataHandler.getAllBookings().stream()
            .filter(b -> b.getBookingId().equals(bookingId))
            .findFirst().orElse(null);
        
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setEnabled(false);
        PaymentFrame paymentFrame = new PaymentFrame(this, booking);
        
        paymentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                if (paymentFrame.isPaymentSuccessful()) {
                    dataHandler.updatePayment(bookingId, paymentFrame.getTransactionId());
                    loadMyBookings();
                    loadMyPayments();
                    JOptionPane.showMessageDialog(CustomerFrame.this, 
                        "Payment successful!\nTransaction ID: " + paymentFrame.getTransactionId(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                setEnabled(true);
                toFront();
            }
        });
    }

    private void raiseIssueOnBooking() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to raise an issue.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String bookingId = (String) bookingTableModel.getValueAt(row, 0);
        String hallName  = (String) bookingTableModel.getValueAt(row, 1);

        // Find hallId
        String hallId = "";
        for (Booking b : dataHandler.getAllBookings()) {
            if (b.getBookingId().equals(bookingId)) { hallId = b.getHallId(); break; }
        }
        final String finalHallId = hallId;

        JDialog dialog = new JDialog(this, "Raise Issue", true);
        dialog.setSize(380, 230);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setSelectedItem("MEDIUM");

        panel.add(new JLabel("Describe the Issue *:"));
        panel.add(new JScrollPane(descArea));
        panel.add(new JLabel("Priority:"));
        panel.add(priorityCombo);

        JButton submitBtn = makeButton("Submit Issue", new Color(200, 130, 50));
        JButton cancelBtn = makeButton("Cancel",       new Color(180, 80, 80));
        panel.add(submitBtn); panel.add(cancelBtn);

        dialog.add(panel);
        submitBtn.addActionListener(e -> {
            String desc = descArea.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please describe the issue!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String issueId = dataHandler.generateIssueId();
            Issue issue = new Issue(issueId, currentUser.getUserId(), currentUser.getName(),
                finalHallId, hallName, bookingId, desc);
            issue.setPriority((String) priorityCombo.getSelectedItem());
            if (dataHandler.addIssue(issue)) {
                JOptionPane.showMessageDialog(dialog, "Issue submitted successfully! Issue ID: " + issueId, "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadMyIssues();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // ==================== MY ISSUES TAB ====================

    private JPanel buildMyIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Issue ID", "Hall", "Booking ID", "Description", "Priority", "Status", "Date Submitted"};
        issueTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable issueTable = new JTable(issueTableModel);
        issueTable.setRowHeight(24);
        issueTable.setFont(new Font("Arial", Font.PLAIN, 12));
        issueTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton refreshBtn = makeButton("Refresh", new Color(130, 130, 130));
        buttonBar.add(refreshBtn);
        refreshBtn.addActionListener(e -> loadMyIssues());

        panel.add(buttonBar,              BorderLayout.NORTH);
        panel.add(new JScrollPane(issueTable), BorderLayout.CENTER);

        return panel;
    }

    private void loadMyIssues() {
        issueTableModel.setRowCount(0);
        for (Issue i : dataHandler.getIssuesByCustomer(currentUser.getUserId())) {
            issueTableModel.addRow(new Object[]{
                i.getIssueId(), i.getHallName(), i.getBookingId(),
                i.getDescription(), i.getPriority(), i.getStatus(), i.getCreatedDate()
            });
        }
    }

    // ==================== MY PAYMENTS TAB ====================

    private JPanel buildMyPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Booking ID", "Hall Name", "Event", "Date", "Amount (RM)", "Payment Status", "Transaction ID", "Payment Date"};
        paymentTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        paymentTable = new JTable(paymentTableModel);
        paymentTable.setRowHeight(24);
        paymentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        paymentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.add(new JLabel("Filter:"));
        String[] filters = {"All", "PENDING", "PAID"};
        JComboBox<String> filterCombo = new JComboBox<>(filters);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton payBtn = makeButton("Make Payment", new Color(50, 130, 200));
        JButton refreshBtn = makeButton("Refresh", new Color(130, 130, 130));
        buttonBar.add(payBtn);
        buttonBar.add(refreshBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(filterBar, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(paymentTable), BorderLayout.CENTER);

        filterCombo.addActionListener(e -> loadMyPayments((String) filterCombo.getSelectedItem()));
        payBtn.addActionListener(e -> makePaymentFromPaymentsTab());
        refreshBtn.addActionListener(e -> loadMyPayments());

        filterBar.add(filterCombo);

        loadMyPayments();

        return panel;
    }

    private void loadMyPayments() {
        loadMyPayments("All");
    }

    private void loadMyPayments(String filter) {
        paymentTableModel.setRowCount(0);
        for (Booking b : dataHandler.getBookingsByCustomer(currentUser.getUserId())) {
            String payStatus = b.getPaymentStatus() != null ? b.getPaymentStatus() : "PENDING";
            if (filter.equals("All") || payStatus.equals(filter)) {
                paymentTableModel.addRow(new Object[]{
                    b.getBookingId(), b.getHallName(), b.getEventName(),
                    b.getBookingDate(), String.format("%.2f", b.getTotalAmount()),
                    payStatus, 
                    b.getTransactionId() != null ? b.getTransactionId() : "-",
                    b.getPaymentDate() != null ? b.getPaymentDate().toString() : "-"
                });
            }
        }
    }

    private void makePaymentFromPaymentsTab() {
        int row = paymentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to make payment.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String bookingId = (String) paymentTableModel.getValueAt(row, 0);
        String paymentStatus = (String) paymentTableModel.getValueAt(row, 5);
        
        if ("PAID".equals(paymentStatus)) {
            JOptionPane.showMessageDialog(this, "This booking has already been paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        final Booking booking = dataHandler.getAllBookings().stream()
            .filter(b -> b.getBookingId().equals(bookingId))
            .findFirst().orElse(null);
        
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setEnabled(false);
        PaymentFrame paymentFrame = new PaymentFrame(this, booking);
        
        paymentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                if (paymentFrame.isPaymentSuccessful()) {
                    dataHandler.updatePayment(bookingId, paymentFrame.getTransactionId());
                    loadMyPayments();
                    loadMyBookings();
                    JOptionPane.showMessageDialog(CustomerFrame.this, 
                        "Payment successful!\nTransaction ID: " + paymentFrame.getTransactionId(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                setEnabled(true);
                toFront();
            }
        });
    }

    // ==================== PROFILE DIALOG ====================

    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "My Profile", true);
        dialog.setSize(370, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        JTextField nameField  = new JTextField(currentUser.getName());
        JTextField emailField = new JTextField(currentUser.getEmail());
        JTextField phoneField = new JTextField(currentUser.getPhone());
        JPasswordField oldPassField = new JPasswordField();
        JPasswordField newPassField = new JPasswordField();

        panel.add(new JLabel("Full Name:"));  panel.add(nameField);
        panel.add(new JLabel("Email:"));      panel.add(emailField);
        panel.add(new JLabel("Phone:"));      panel.add(phoneField);
        panel.add(new JLabel("Current Password (to change):"));  panel.add(oldPassField);
        panel.add(new JLabel("New Password (optional):"));       panel.add(newPassField);

        JButton saveBtn   = makeButton("Save Changes", new Color(50, 130, 80));
        JButton cancelBtn = makeButton("Cancel",       new Color(180, 80, 80));
        panel.add(saveBtn); panel.add(cancelBtn);

        dialog.add(panel);
        saveBtn.addActionListener(e -> {
            currentUser.setName(nameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());

            String oldPass = new String(oldPassField.getPassword());
            String newPass = new String(newPassField.getPassword());

            if (!newPass.isEmpty()) {
                if (!oldPass.equals(currentUser.getPassword())) {
                    JOptionPane.showMessageDialog(dialog, "Current password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newPass.length() < 6) {
                    JOptionPane.showMessageDialog(dialog, "New password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                currentUser.setPassword(newPass);
            }

            if (dataHandler.updateUser(currentUser)) {
                JOptionPane.showMessageDialog(dialog, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                setTitle("Hall Symphony - Customer Dashboard (" + currentUser.getName() + ")");
                dialog.dispose();
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
}
