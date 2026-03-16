package models;

// ENCAPSULATION - Hall fields are private with public getters/setters
public class Hall {
    private String hallId;
    private String hallName;
    private HallType hallType;
    private int capacity;
    private double ratePerHour;
    private boolean available;
    private String description;

    public Hall(String hallId, String hallName, HallType hallType) {
        this.hallId      = hallId;
        this.hallName    = hallName;
        this.hallType    = hallType;
        this.capacity    = hallType.getCapacity();
        this.ratePerHour = hallType.getRatePerHour();
        this.available   = true;
        this.description = "";
    }

    // Calculate total price for a booking
    public double calculatePrice(int hours) {
        return hours * ratePerHour;
    }

    // Getters
    public String   getHallId()      { return hallId; }
    public String   getHallName()    { return hallName; }
    public HallType getHallType()    { return hallType; }
    public int      getCapacity()    { return capacity; }
    public double   getRatePerHour() { return ratePerHour; }
    public boolean  isAvailable()    { return available; }
    public String   getDescription() { return description; }

    // Setters
    public void setHallName(String hallName)       { this.hallName = hallName; }
    public void setAvailable(boolean available)    { this.available = available; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return hallId + "|" + hallName + "|" + hallType.getDisplayName() + "|"
             + capacity + "|" + ratePerHour + "|" + available + "|" + description;
    }

    public static Hall fromString(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 7) {
                Hall h = new Hall(p[0], p[1], HallType.fromDisplayName(p[2]));
                h.setAvailable(Boolean.parseBoolean(p[5]));
                h.setDescription(p[6]);
                return h;
            }
        } catch (Exception e) {
            System.err.println("Error reading hall: " + e.getMessage());
        }
        return null;
    }
}
