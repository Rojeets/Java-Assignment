package models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// ENCAPSULATION - all fields private, accessed through methods
public class Booking {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private String    bookingId;
    private String    customerId;
    private String    customerName;
    private String    hallId;
    private String    hallName;
    private String    hallType;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int       hours;
    private double    totalAmount;
    private String    status;
    private LocalDate createdDate;
    private String    eventName;
    private String    eventDescription;
    private String    paymentStatus;
    private String    transactionId;
    private LocalDate paymentDate;

    public Booking(String bookingId, String customerId, String customerName,
                   String hallId, String hallName, String hallType,
                   LocalDate bookingDate, LocalTime startTime, LocalTime endTime,
                   double ratePerHour, String eventName, String eventDescription) {
        this.bookingId        = bookingId;
        this.customerId       = customerId;
        this.customerName     = customerName;
        this.hallId           = hallId;
        this.hallName         = hallName;
        this.hallType         = hallType;
        this.bookingDate      = bookingDate;
        this.startTime        = startTime;
        this.endTime          = endTime;
        this.hours            = endTime.getHour() - startTime.getHour();
        this.totalAmount      = hours * ratePerHour;
        this.status           = "CONFIRMED";
        this.createdDate      = LocalDate.now();
        this.eventName        = eventName;
        this.eventDescription = eventDescription;
        this.paymentStatus    = "PENDING";
        this.transactionId    = null;
        this.paymentDate      = null;
    }

    // Getters
    public String    getBookingId()        { return bookingId; }
    public String    getCustomerId()       { return customerId; }
    public String    getCustomerName()     { return customerName; }
    public String    getHallId()           { return hallId; }
    public String    getHallName()         { return hallName; }
    public String    getHallType()         { return hallType; }
    public LocalDate getBookingDate()      { return bookingDate; }
    public LocalTime getStartTime()        { return startTime; }
    public LocalTime getEndTime()          { return endTime; }
    public int       getHours()            { return hours; }
    public double    getTotalAmount()      { return totalAmount; }
    public String    getStatus()           { return status; }
    public LocalDate getCreatedDate()      { return createdDate; }
    public String    getEventName()        { return eventName; }
    public String    getEventDescription() { return eventDescription; }

    public void setStatus(String status)   { this.status = status; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public String getTransactionId() { return transactionId; }
    public LocalDate getPaymentDate() { return paymentDate; }
    
    public void setPaymentInfo(String transactionId) {
        this.paymentStatus = "PAID";
        this.transactionId = transactionId;
        this.paymentDate = LocalDate.now();
    }

    @Override
    public String toString() {
        String payStatus = paymentStatus != null ? paymentStatus : "PENDING";
        String txnId = transactionId != null ? transactionId : "";
        String payDate = paymentDate != null ? paymentDate.format(DATE_FMT) : "";
        return bookingId + "|" + customerId + "|" + customerName + "|"
             + hallId + "|" + hallName + "|" + hallType + "|"
             + bookingDate.format(DATE_FMT) + "|"
             + startTime.format(TIME_FMT) + "|"
             + endTime.format(TIME_FMT) + "|"
             + hours + "|" + totalAmount + "|" + status + "|"
             + createdDate.format(DATE_FMT) + "|" + eventName + "|" + eventDescription
             + "|" + payStatus + "|" + txnId + "|" + payDate;
    }

    public static Booking fromString(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 15) {
                Booking b = new Booking(
                    p[0], p[1], p[2], p[3], p[4], p[5],
                    LocalDate.parse(p[6], DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    LocalTime.parse(p[7], DateTimeFormatter.ofPattern("HH:mm")),
                    LocalTime.parse(p[8], DateTimeFormatter.ofPattern("HH:mm")),
                    0, p[13], p[14]
                );
                b.setStatus(p[11]);
                b.setTotalAmount(Double.parseDouble(p[10]));
                
                if (p.length >= 18) {
                    b.paymentStatus = p[15];
                    b.transactionId = p[16].isEmpty() ? null : p[16];
                    if (p[17] != null && !p[17].isEmpty()) {
                        b.paymentDate = LocalDate.parse(p[17], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                }
                return b;
            }
        } catch (Exception e) {
            System.err.println("Error reading booking: " + e.getMessage());
        }
        return null;
    }
}
