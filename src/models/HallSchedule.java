package models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HallSchedule {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private String    scheduleId;
    private String    hallId;
    private String    hallName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String    scheduleType; // AVAILABILITY or MAINTENANCE
    private String    remarks;
    private String    status;

    public HallSchedule(String scheduleId, String hallId, String hallName,
                        LocalDate startDate, LocalDate endDate,
                        LocalTime startTime, LocalTime endTime,
                        String scheduleType, String remarks) {
        this.scheduleId   = scheduleId;
        this.hallId       = hallId;
        this.hallName     = hallName;
        this.startDate    = startDate;
        this.endDate      = endDate;
        this.startTime    = startTime;
        this.endTime      = endTime;
        this.scheduleType = scheduleType;
        this.remarks      = remarks;
        this.status       = "ACTIVE";
    }

    // Getters
    public String    getScheduleId()   { return scheduleId; }
    public String    getHallId()       { return hallId; }
    public String    getHallName()     { return hallName; }
    public LocalDate getStartDate()    { return startDate; }
    public LocalDate getEndDate()      { return endDate; }
    public LocalTime getStartTime()    { return startTime; }
    public LocalTime getEndTime()      { return endTime; }
    public String    getScheduleType() { return scheduleType; }
    public String    getRemarks()      { return remarks; }
    public String    getStatus()       { return status; }

    // Setters
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public void setEndDate(LocalDate d)   { this.endDate = d; }
    public void setStartTime(LocalTime t) { this.startTime = t; }
    public void setEndTime(LocalTime t)   { this.endTime = t; }
    public void setRemarks(String r)      { this.remarks = r; }
    public void setStatus(String s)       { this.status = s; }

    @Override
    public String toString() {
        return scheduleId + "|" + hallId + "|" + hallName + "|"
             + startDate.format(DATE_FMT) + "|" + endDate.format(DATE_FMT) + "|"
             + startTime.format(TIME_FMT) + "|" + endTime.format(TIME_FMT) + "|"
             + scheduleType + "|" + remarks + "|" + status;
    }

    public static HallSchedule fromString(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 10) {
                HallSchedule s = new HallSchedule(
                    p[0], p[1], p[2],
                    LocalDate.parse(p[3], DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    LocalDate.parse(p[4], DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    LocalTime.parse(p[5], DateTimeFormatter.ofPattern("HH:mm")),
                    LocalTime.parse(p[6], DateTimeFormatter.ofPattern("HH:mm")),
                    p[7], p[8]
                );
                s.setStatus(p[9]);
                return s;
            }
        } catch (Exception e) {
            System.err.println("Error reading schedule: " + e.getMessage());
        }
        return null;
    }
}
