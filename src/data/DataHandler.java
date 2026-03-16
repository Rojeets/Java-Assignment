package data;

import models.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

// This class handles reading and writing all data to text files
public class DataHandler {
    private static final String DATA_DIR      = "data/";
    private static final String USERS_FILE    = DATA_DIR + "users.txt";
    private static final String HALLS_FILE    = DATA_DIR + "halls.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + "bookings.txt";
    private static final String SCHEDULES_FILE= DATA_DIR + "schedules.txt";
    private static final String ISSUES_FILE   = DATA_DIR + "issues.txt";

    public DataHandler() {
        createDataDirectory();
        initializeDefaultData();
    }

    private void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // Creates default accounts and halls if files don't exist yet
    private void initializeDefaultData() {
        if (!new File(USERS_FILE).exists()) {
            try {
                List<String> defaultUsers = Arrays.asList(
                    "ADMIN001|admin|admin123|System Admin|admin@hallsymphony.com|0123456789|ADMINISTRATOR|true|" + LocalDate.now(),
                    "MGR001|manager|manager123|John Manager|manager@hallsymphony.com|0123456790|MANAGER|true|" + LocalDate.now(),
                    "SCH001|scheduler|scheduler123|Sarah Scheduler|scheduler@hallsymphony.com|0123456791|SCHEDULER|true|" + LocalDate.now(),
                    "CUST001|customer1|customer123|Alice Wong|alice@email.com|0123456792|CUSTOMER|true|" + LocalDate.now()
                );
                Files.write(Paths.get(USERS_FILE), defaultUsers);
            } catch (IOException e) {
                System.err.println("Error creating users file: " + e.getMessage());
            }
        }

        if (!new File(HALLS_FILE).exists()) {
            try {
                List<String> defaultHalls = Arrays.asList(
                    "HALL001|Symphony Auditorium|Auditorium|1000|300.0|true|Grand auditorium for large events",
                    "HALL002|Golden Banquet Hall|Banquet Hall|300|100.0|true|Elegant hall for weddings and parties",
                    "HALL003|Meeting Room A|Meeting Room|30|50.0|true|Small meeting room with projector",
                    "HALL004|Meeting Room B|Meeting Room|30|50.0|true|Small meeting room with whiteboard",
                    "HALL005|Premium Banquet Hall|Banquet Hall|300|100.0|true|Luxury banquet hall with garden view"
                );
                Files.write(Paths.get(HALLS_FILE), defaultHalls);
            } catch (IOException e) {
                System.err.println("Error creating halls file: " + e.getMessage());
            }
        }

        // Create empty files if they don't exist
        createEmptyFileIfNotExists(BOOKINGS_FILE);
        createEmptyFileIfNotExists(SCHEDULES_FILE);
        createEmptyFileIfNotExists(ISSUES_FILE);
    }

    private void createEmptyFileIfNotExists(String filePath) {
        if (!new File(filePath).exists()) {
            try {
                Files.createFile(Paths.get(filePath));
            } catch (IOException e) {
                System.err.println("Error creating file " + filePath + ": " + e.getMessage());
            }
        }
    }

    // ==================== USER METHODS ====================

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    User user = User.fromString(line);
                    if (user != null) users.add(user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
        return users;
    }

    public User authenticateUser(String username, String password) {
        for (User user : getAllUsers()) {
            if (user.getUsername().equals(username)
                && user.getPassword().equals(password)
                && user.isActive()) {
                return user;
            }
        }
        return null;
    }

    public boolean isUsernameTaken(String username) {
        for (User user : getAllUsers()) {
            if (user.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public boolean registerUser(User user) {
        if (isUsernameTaken(user.getUsername())) return false;
        try {
            List<String> lines = Collections.singletonList(user.toString());
            Files.write(Paths.get(USERS_FILE), lines, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(User updatedUser) {
        try {
            List<User> users = getAllUsers();
            List<String> lines = new ArrayList<>();
            for (User u : users) {
                if (u.getUserId().equals(updatedUser.getUserId())) {
                    lines.add(updatedUser.toString());
                } else {
                    lines.add(u.toString());
                }
            }
            Files.write(Paths.get(USERS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(String userId) {
        try {
            List<User> users = getAllUsers();
            List<String> lines = new ArrayList<>();
            for (User u : users) {
                if (!u.getUserId().equals(userId)) lines.add(u.toString());
            }
            Files.write(Paths.get(USERS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public boolean blockUser(String userId, boolean block) {
        try {
            List<User> users = getAllUsers();
            List<String> lines = new ArrayList<>();
            for (User u : users) {
                if (u.getUserId().equals(userId)) {
                    u.setActive(!block);
                }
                lines.add(u.toString());
            }
            Files.write(Paths.get(USERS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error blocking user: " + e.getMessage());
            return false;
        }
    }

    // ==================== HALL METHODS ====================

    public List<Hall> getAllHalls() {
        List<Hall> halls = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(HALLS_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    Hall hall = Hall.fromString(line);
                    if (hall != null) halls.add(hall);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading halls: " + e.getMessage());
        }
        return halls;
    }

    public boolean addHall(Hall hall) {
        try {
            List<String> lines = Collections.singletonList(hall.toString());
            Files.write(Paths.get(HALLS_FILE), lines, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error adding hall: " + e.getMessage());
            return false;
        }
    }

    public boolean updateHall(Hall updatedHall) {
        try {
            List<Hall> halls = getAllHalls();
            List<String> lines = new ArrayList<>();
            for (Hall h : halls) {
                if (h.getHallId().equals(updatedHall.getHallId())) {
                    lines.add(updatedHall.toString());
                } else {
                    lines.add(h.toString());
                }
            }
            Files.write(Paths.get(HALLS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error updating hall: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteHall(String hallId) {
        try {
            List<Hall> halls = getAllHalls();
            List<String> lines = new ArrayList<>();
            for (Hall h : halls) {
                if (!h.getHallId().equals(hallId)) lines.add(h.toString());
            }
            Files.write(Paths.get(HALLS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error deleting hall: " + e.getMessage());
            return false;
        }
    }

    // ==================== BOOKING METHODS ====================

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(BOOKINGS_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    Booking b = Booking.fromString(line);
                    if (b != null) bookings.add(b);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading bookings: " + e.getMessage());
        }
        return bookings;
    }

    public List<Booking> getBookingsByCustomer(String customerId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : getAllBookings()) {
            if (b.getCustomerId().equals(customerId)) result.add(b);
        }
        return result;
    }

    public boolean addBooking(Booking booking) {
        try {
            List<String> lines = Collections.singletonList(booking.toString());
            Files.write(Paths.get(BOOKINGS_FILE), lines, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error adding booking: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBooking(Booking updated) {
        try {
            List<Booking> bookings = getAllBookings();
            List<String> lines = new ArrayList<>();
            for (Booking b : bookings) {
                if (b.getBookingId().equals(updated.getBookingId())) {
                    lines.add(updated.toString());
                } else {
                    lines.add(b.toString());
                }
            }
            Files.write(Paths.get(BOOKINGS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error updating booking: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelBooking(String bookingId) {
        try {
            List<Booking> bookings = getAllBookings();
            List<String> lines = new ArrayList<>();
            for (Booking b : bookings) {
                if (b.getBookingId().equals(bookingId)) {
                    b.setStatus("CANCELLED");
                }
                lines.add(b.toString());
            }
            Files.write(Paths.get(BOOKINGS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePayment(String bookingId, String transactionId) {
        try {
            List<Booking> bookings = getAllBookings();
            List<String> lines = new ArrayList<>();
            for (Booking b : bookings) {
                if (b.getBookingId().equals(bookingId)) {
                    b.setPaymentInfo(transactionId);
                }
                lines.add(b.toString());
            }
            Files.write(Paths.get(BOOKINGS_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error updating payment: " + e.getMessage());
            return false;
        }
    }

    // ==================== SCHEDULE METHODS ====================

    public List<HallSchedule> getAllSchedules() {
        List<HallSchedule> schedules = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(SCHEDULES_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    HallSchedule s = HallSchedule.fromString(line);
                    if (s != null) schedules.add(s);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading schedules: " + e.getMessage());
        }
        return schedules;
    }

    public boolean addSchedule(HallSchedule schedule) {
        try {
            List<String> lines = Collections.singletonList(schedule.toString());
            Files.write(Paths.get(SCHEDULES_FILE), lines, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error adding schedule: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSchedule(HallSchedule updated) {
        try {
            List<HallSchedule> schedules = getAllSchedules();
            List<String> lines = new ArrayList<>();
            for (HallSchedule s : schedules) {
                if (s.getScheduleId().equals(updated.getScheduleId())) {
                    lines.add(updated.toString());
                } else {
                    lines.add(s.toString());
                }
            }
            Files.write(Paths.get(SCHEDULES_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error updating schedule: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSchedule(String scheduleId) {
        try {
            List<HallSchedule> schedules = getAllSchedules();
            List<String> lines = new ArrayList<>();
            for (HallSchedule s : schedules) {
                if (!s.getScheduleId().equals(scheduleId)) lines.add(s.toString());
            }
            Files.write(Paths.get(SCHEDULES_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
            return false;
        }
    }

    // ==================== ISSUE METHODS ====================

    public List<Issue> getAllIssues() {
        List<Issue> issues = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(ISSUES_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    Issue i = Issue.fromString(line);
                    if (i != null) issues.add(i);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading issues: " + e.getMessage());
        }
        return issues;
    }

    public List<Issue> getIssuesByCustomer(String customerId) {
        List<Issue> result = new ArrayList<>();
        for (Issue i : getAllIssues()) {
            if (i.getCustomerId().equals(customerId)) result.add(i);
        }
        return result;
    }

    public boolean addIssue(Issue issue) {
        try {
            List<String> lines = Collections.singletonList(issue.toString());
            Files.write(Paths.get(ISSUES_FILE), lines, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error adding issue: " + e.getMessage());
            return false;
        }
    }

    public boolean updateIssue(Issue updated) {
        try {
            List<Issue> issues = getAllIssues();
            List<String> lines = new ArrayList<>();
            for (Issue i : issues) {
                if (i.getIssueId().equals(updated.getIssueId())) {
                    lines.add(updated.toString());
                } else {
                    lines.add(i.toString());
                }
            }
            Files.write(Paths.get(ISSUES_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error updating issue: " + e.getMessage());
            return false;
        }
    }

    // ==================== ID GENERATORS ====================

    public String generateUserId(UserRole role) {
        String prefix = "";
        switch (role) {
            case SCHEDULER:     prefix = "SCH";   break;
            case CUSTOMER:      prefix = "CUST";  break;
            case ADMINISTRATOR: prefix = "ADMIN"; break;
            case MANAGER:       prefix = "MGR";   break;
        }
        int maxNum = 0;
        for (User u : getAllUsers()) {
            if (u.getRole() == role) {
                try {
                    int num = Integer.parseInt(u.getUserId().replace(prefix, ""));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException e) { /* skip */ }
            }
        }
        return prefix + String.format("%03d", maxNum + 1);
    }

    public String generateHallId() {
        int maxNum = 0;
        for (Hall h : getAllHalls()) {
            try {
                int num = Integer.parseInt(h.getHallId().replace("HALL", ""));
                if (num > maxNum) maxNum = num;
            } catch (NumberFormatException e) { /* skip */ }
        }
        return "HALL" + String.format("%03d", maxNum + 1);
    }

    public String generateBookingId() {
        int maxNum = 0;
        for (Booking b : getAllBookings()) {
            try {
                int num = Integer.parseInt(b.getBookingId().replace("BOOK", ""));
                if (num > maxNum) maxNum = num;
            } catch (NumberFormatException e) { /* skip */ }
        }
        return "BOOK" + String.format("%05d", maxNum + 1);
    }

    public String generateScheduleId() {
        int maxNum = 0;
        for (HallSchedule s : getAllSchedules()) {
            try {
                int num = Integer.parseInt(s.getScheduleId().replace("SCHD", ""));
                if (num > maxNum) maxNum = num;
            } catch (NumberFormatException e) { /* skip */ }
        }
        return "SCHD" + String.format("%04d", maxNum + 1);
    }

    public String generateIssueId() {
        int maxNum = 0;
        for (Issue i : getAllIssues()) {
            try {
                int num = Integer.parseInt(i.getIssueId().replace("ISSUE", ""));
                if (num > maxNum) maxNum = num;
            } catch (NumberFormatException e) { /* skip */ }
        }
        return "ISSUE" + String.format("%04d", maxNum + 1);
    }

    // Check if hall is free at a given date and time
    public boolean isHallAvailable(String hallId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Check against confirmed bookings
        for (Booking b : getAllBookings()) {
            if (b.getHallId().equals(hallId)
                && b.getBookingDate().equals(date)
                && b.getStatus().equals("CONFIRMED")) {
                boolean overlaps = !(endTime.isBefore(b.getStartTime()) || startTime.isAfter(b.getEndTime()));
                if (overlaps) return false;
            }
        }
        // Check against maintenance/availability schedules
        for (HallSchedule s : getAllSchedules()) {
            if (s.getHallId().equals(hallId) && s.getStatus().equals("ACTIVE")
                && !date.isBefore(s.getStartDate()) && !date.isAfter(s.getEndDate())) {
                if (s.getScheduleType().equals("MAINTENANCE")) {
                    boolean overlaps = !(endTime.isBefore(s.getStartTime()) || startTime.isAfter(s.getEndTime()));
                    if (overlaps) return false;
                }
            }
        }
        return true;
    }
}
