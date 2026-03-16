package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Issue {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String    issueId;
    private String    customerId;
    private String    customerName;
    private String    hallId;
    private String    hallName;
    private String    bookingId;
    private String    description;
    private String    status;       // OPEN, IN_PROGRESS, DONE, CLOSED, CANCELLED
    private String    priority;     // LOW, MEDIUM, HIGH
    private String    assignedTo;
    private LocalDate createdDate;
    private LocalDate resolvedDate;
    private String    resolutionNotes;

    public Issue(String issueId, String customerId, String customerName,
                 String hallId, String hallName, String bookingId, String description) {
        this.issueId          = issueId;
        this.customerId       = customerId;
        this.customerName     = customerName;
        this.hallId           = hallId;
        this.hallName         = hallName;
        this.bookingId        = bookingId;
        this.description      = description;
        this.status           = "OPEN";
        this.priority         = "MEDIUM";
        this.assignedTo       = "";
        this.createdDate      = LocalDate.now();
        this.resolvedDate     = null;
        this.resolutionNotes  = "";
    }

    // Getters
    public String    getIssueId()         { return issueId; }
    public String    getCustomerId()      { return customerId; }
    public String    getCustomerName()    { return customerName; }
    public String    getHallId()          { return hallId; }
    public String    getHallName()        { return hallName; }
    public String    getBookingId()       { return bookingId; }
    public String    getDescription()     { return description; }
    public String    getStatus()          { return status; }
    public String    getPriority()        { return priority; }
    public String    getAssignedTo()      { return assignedTo; }
    public LocalDate getCreatedDate()     { return createdDate; }
    public LocalDate getResolvedDate()    { return resolvedDate; }
    public String    getResolutionNotes() { return resolutionNotes; }

    // Setters
    public void setStatus(String s) {
        this.status = s;
        if (s.equals("DONE") || s.equals("CLOSED")) {
            this.resolvedDate = LocalDate.now();
        }
    }
    public void setPriority(String p)        { this.priority = p; }
    public void setAssignedTo(String a)      { this.assignedTo = a; }
    public void setResolutionNotes(String n) { this.resolutionNotes = n; }

    @Override
    public String toString() {
        return issueId + "|" + customerId + "|" + customerName + "|"
             + hallId + "|" + hallName + "|" + bookingId + "|"
             + description + "|" + status + "|" + priority + "|" + assignedTo + "|"
             + createdDate.format(DATE_FMT) + "|"
             + (resolvedDate != null ? resolvedDate.format(DATE_FMT) : "") + "|"
             + resolutionNotes;
    }

    public static Issue fromString(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 13) {
                Issue i = new Issue(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
                i.setStatus(p[7]);
                i.setPriority(p[8]);
                i.setAssignedTo(p[9]);
                i.setResolutionNotes(p[12]);
                return i;
            }
        } catch (Exception e) {
            System.err.println("Error reading issue: " + e.getMessage());
        }
        return null;
    }
}
