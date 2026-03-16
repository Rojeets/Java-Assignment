package models;

// Enum for the 3 hall types with their capacity and rates
public enum HallType {
    AUDITORIUM("Auditorium", 1000, 300.00),
    BANQUET_HALL("Banquet Hall", 300, 100.00),
    MEETING_ROOM("Meeting Room", 30, 50.00);

    private final String displayName;
    private final int capacity;
    private final double ratePerHour;

    HallType(String displayName, int capacity, double ratePerHour) {
        this.displayName = displayName;
        this.capacity    = capacity;
        this.ratePerHour = ratePerHour;
    }

    public String getDisplayName()  { return displayName; }
    public int    getCapacity()     { return capacity; }
    public double getRatePerHour()  { return ratePerHour; }

    public static HallType fromDisplayName(String name) {
        for (HallType t : values()) {
            if (t.displayName.equalsIgnoreCase(name)) return t;
        }
        return null;
    }
}
