package frames;

import data.DataHandler;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Dashboard for Scheduler role
// Scheduler can manage halls and set availability/maintenance schedules
public class SchedulerFrame extends JFrame {

    private User        currentUser;
    private DataHandler dataHandler;
    private JTable      hallTable;
    private JTable      scheduleTable;
    private DefaultTableModel hallTableModel;
    private DefaultTableModel scheduleTableModel;

    public SchedulerFrame(User user, DataHandler dataHandler) {
        this.currentUser = user;
        this.dataHandler = dataHandler;
        setupUI();
        loadHalls();
        loadSchedules();
    }

    private void setupUI() {
        setTitle("Hall Symphony - Scheduler Dashboard (" + currentUser.getName() + ")");
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(70, 130, 180));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName() + " (Scheduler)");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(200, 70, 70));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));
        tabs.addTab("Hall Management",     buildHallPanel());
        tabs.addTab("Hall Schedules",      buildSchedulePanel());

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabs,   BorderLayout.CENTER);

        add(mainPanel);

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // ==================== HALL MANAGEMENT TAB ====================

    private JPanel buildHallPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table columns
        String[] cols = {"Hall ID", "Hall Name", "Type", "Capacity", "Rate/Hour (RM)", "Available", "Description"};
        hallTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        hallTable = new JTable(hallTableModel);
        hallTable.setRowHeight(24);
        hallTable.setFont(new Font("Arial", Font.PLAIN, 12));
        hallTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        hallTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(hallTable);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.add(new JLabel("Filter by Type:"));
        String[] types = {"All", "Auditorium", "Banquet Hall", "Meeting Room"};
        JComboBox<String> typeFilter = new JComboBox<>(types);
        typeFilter.addActionListener(e -> filterHalls((String) typeFilter.getSelectedItem()));
        filterBar.add(typeFilter);

        // Button bar
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton addBtn    = makeButton("Add Hall",    new Color(50, 150, 80));
        JButton editBtn   = makeButton("Edit Hall",   new Color(100, 130, 200));
        JButton deleteBtn = makeButton("Delete Hall", new Color(200, 80, 80));
        JButton refreshBtn= makeButton("Refresh",     new Color(130, 130, 130));
        buttonBar.add(addBtn);
        buttonBar.add(editBtn);
        buttonBar.add(deleteBtn);
        buttonBar.add(refreshBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterBar, BorderLayout.WEST);
        topPanel.add(buttonBar, BorderLayout.EAST);

        panel.add(topPanel,    BorderLayout.NORTH);
        panel.add(scrollPane,  BorderLayout.CENTER);

        addBtn.addActionListener(e    -> showAddHallDialog());
        editBtn.addActionListener(e   -> showEditHallDialog());
        deleteBtn.addActionListener(e -> deleteHall());
        refreshBtn.addActionListener(e-> loadHalls());

        return panel;
    }

    private void loadHalls() {
        hallTableModel.setRowCount(0);
        List<Hall> halls = dataHandler.getAllHalls();
        for (Hall h : halls) {
            hallTableModel.addRow(new Object[]{
                h.getHallId(), h.getHallName(), h.getHallType().getDisplayName(),
                h.getCapacity(), String.format("%.2f", h.getRatePerHour()),
                h.isAvailable() ? "Yes" : "No", h.getDescription()
            });
        }
    }

    private void filterHalls(String type) {
        hallTableModel.setRowCount(0);
        for (Hall h : dataHandler.getAllHalls()) {
            if (type.equals("All") || h.getHallType().getDisplayName().equals(type)) {
                hallTableModel.addRow(new Object[]{
                    h.getHallId(), h.getHallName(), h.getHallType().getDisplayName(),
                    h.getCapacity(), String.format("%.2f", h.getRatePerHour()),
                    h.isAvailable() ? "Yes" : "No", h.getDescription()
                });
            }
        }
    }

    private void showAddHallDialog() {
        JDialog dialog = new JDialog(this, "Add New Hall", true);
        dialog.setSize(380, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField nameField = new JTextField();
        String[] typeOptions = {"Auditorium", "Banquet Hall", "Meeting Room"};
        JComboBox<String> typeCombo = new JComboBox<>(typeOptions);
        JTextField descField = new JTextField();

        panel.add(new JLabel("Hall Name *:")); panel.add(nameField);
        panel.add(new JLabel("Hall Type *:")); panel.add(typeCombo);
        panel.add(new JLabel("Description:")); panel.add(descField);

        JButton saveBtn   = makeButton("Save",   new Color(50, 150, 80));
        JButton cancelBtn = makeButton("Cancel", new Color(180, 80, 80));
        panel.add(saveBtn); panel.add(cancelBtn);

        dialog.add(panel);
        dialog.setVisible(false);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Hall Name is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            HallType hallType = HallType.fromDisplayName((String) typeCombo.getSelectedItem());
            String hallId = dataHandler.generateHallId();
            Hall hall = new Hall(hallId, name, hallType);
            hall.setDescription(descField.getText().trim());
            if (dataHandler.addHall(hall)) {
                JOptionPane.showMessageDialog(dialog, "Hall added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadHalls();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showEditHallDialog() {
        int selectedRow = hallTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a hall to edit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String hallId = (String) hallTableModel.getValueAt(selectedRow, 0);
        Hall hall = null;
        for (Hall h : dataHandler.getAllHalls()) {
            if (h.getHallId().equals(hallId)) { hall = h; break; }
        }
        if (hall == null) return;

        JDialog dialog = new JDialog(this, "Edit Hall", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField nameField = new JTextField(hall.getHallName());
        JTextField descField = new JTextField(hall.getDescription());
        String[] availOpts   = {"Yes", "No"};
        JComboBox<String> availCombo = new JComboBox<>(availOpts);
        availCombo.setSelectedItem(hall.isAvailable() ? "Yes" : "No");

        panel.add(new JLabel("Hall Name:")); panel.add(nameField);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Available:")); panel.add(availCombo);

        JButton saveBtn   = makeButton("Save",   new Color(50, 150, 80));
        JButton cancelBtn = makeButton("Cancel", new Color(180, 80, 80));
        panel.add(saveBtn); panel.add(cancelBtn);

        final Hall finalHall = hall;
        dialog.add(panel);
        saveBtn.addActionListener(e -> {
            finalHall.setHallName(nameField.getText().trim());
            finalHall.setDescription(descField.getText().trim());
            finalHall.setAvailable(availCombo.getSelectedItem().equals("Yes"));
            if (dataHandler.updateHall(finalHall)) {
                JOptionPane.showMessageDialog(dialog, "Hall updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadHalls();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void deleteHall() {
        int selectedRow = hallTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a hall to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String hallId   = (String) hallTableModel.getValueAt(selectedRow, 0);
        String hallName = (String) hallTableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete \"" + hallName + "\"?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dataHandler.deleteHall(hallId)) {
                JOptionPane.showMessageDialog(this, "Hall deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadHalls();
            }
        }
    }

    // ==================== SCHEDULE TAB ====================

    private JPanel buildSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Schedule ID", "Hall Name", "Type", "Start Date", "End Date", "Start Time", "End Time", "Status", "Remarks"};
        scheduleTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        scheduleTable = new JTable(scheduleTableModel);
        scheduleTable.setRowHeight(24);
        scheduleTable.setFont(new Font("Arial", Font.PLAIN, 12));
        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton addAvailBtn   = makeButton("Add Availability Schedule", new Color(50, 150, 80));
        JButton addMaintBtn   = makeButton("Add Maintenance Schedule",  new Color(200, 130, 50));
        JButton deleteBtn     = makeButton("Delete Schedule",           new Color(200, 80, 80));
        JButton refreshBtn    = makeButton("Refresh",                   new Color(130, 130, 130));
        buttonBar.add(addAvailBtn);
        buttonBar.add(addMaintBtn);
        buttonBar.add(deleteBtn);
        buttonBar.add(refreshBtn);

        panel.add(buttonBar,  BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        addAvailBtn.addActionListener(e  -> showAddScheduleDialog("AVAILABILITY"));
        addMaintBtn.addActionListener(e  -> showAddScheduleDialog("MAINTENANCE"));
        deleteBtn.addActionListener(e    -> deleteSchedule());
        refreshBtn.addActionListener(e   -> loadSchedules());

        return panel;
    }

    private void loadSchedules() {
        scheduleTableModel.setRowCount(0);
        for (HallSchedule s : dataHandler.getAllSchedules()) {
            scheduleTableModel.addRow(new Object[]{
                s.getScheduleId(), s.getHallName(), s.getScheduleType(),
                s.getStartDate(), s.getEndDate(), s.getStartTime(), s.getEndTime(),
                s.getStatus(), s.getRemarks()
            });
        }
    }

    private void showAddScheduleDialog(String type) {
        JDialog dialog = new JDialog(this, "Add " + type + " Schedule", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Build hall dropdown
        List<Hall> halls = dataHandler.getAllHalls();
        String[] hallNames = new String[halls.size()];
        for (int i = 0; i < halls.size(); i++) hallNames[i] = halls.get(i).getHallName();
        JComboBox<String> hallCombo = new JComboBox<>(hallNames);

        JTextField startDateField = new JTextField(LocalDate.now().toString());
        JTextField endDateField   = new JTextField(LocalDate.now().toString());
        String[] timeOptions = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
                                "14:00", "15:00", "16:00", "17:00", "18:00"};
        JComboBox<String> startTimeCombo = new JComboBox<>(timeOptions);
        JComboBox<String> endTimeCombo   = new JComboBox<>(timeOptions);
        endTimeCombo.setSelectedIndex(2); // default end time 10:00
        JTextField remarksField = new JTextField();

        panel.add(new JLabel("Hall *:"));       panel.add(hallCombo);
        panel.add(new JLabel("Start Date (yyyy-MM-dd) *:")); panel.add(startDateField);
        panel.add(new JLabel("End Date (yyyy-MM-dd) *:"));   panel.add(endDateField);
        panel.add(new JLabel("Start Time *:"));  panel.add(startTimeCombo);
        panel.add(new JLabel("End Time *:"));    panel.add(endTimeCombo);
        panel.add(new JLabel("Remarks:"));       panel.add(remarksField);

        JButton saveBtn   = makeButton("Save",   new Color(50, 150, 80));
        JButton cancelBtn = makeButton("Cancel", new Color(180, 80, 80));
        panel.add(saveBtn); panel.add(cancelBtn);

        dialog.add(panel);
        saveBtn.addActionListener(e -> {
            try {
                int idx = hallCombo.getSelectedIndex();
                Hall selectedHall = halls.get(idx);
                LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
                LocalDate endDate   = LocalDate.parse(endDateField.getText().trim());
                LocalTime startTime = LocalTime.parse((String) startTimeCombo.getSelectedItem());
                LocalTime endTime   = LocalTime.parse((String) endTimeCombo.getSelectedItem());

                if (endDate.isBefore(startDate)) {
                    JOptionPane.showMessageDialog(dialog, "End date cannot be before start date!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!endTime.isAfter(startTime)) {
                    JOptionPane.showMessageDialog(dialog, "End time must be after start time!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String schedId = dataHandler.generateScheduleId();
                HallSchedule schedule = new HallSchedule(schedId, selectedHall.getHallId(),
                    selectedHall.getHallName(), startDate, endDate, startTime, endTime,
                    type, remarksField.getText().trim());

                if (dataHandler.addSchedule(schedule)) {
                    JOptionPane.showMessageDialog(dialog, "Schedule added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadSchedules();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void deleteSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String schedId = (String) scheduleTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this schedule?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dataHandler.deleteSchedule(schedId)) {
                JOptionPane.showMessageDialog(this, "Schedule deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSchedules();
            }
        }
    }

    // Helper to make styled buttons
    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        return btn;
    }
}
