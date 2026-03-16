package models;

import abstraction.Person;

// INHERITANCE - User extends Person (inherits id, name, email, phone)
// ENCAPSULATION - private fields with getters/setters
public class User extends Person {
    private String username;
    private String password;
    private UserRole role;
    private boolean active;
    private String createdDate;

    public User(String userId, String username, String password, String name,
                String email, String phone, UserRole role) {
        super(userId, name, email, phone); // Call parent constructor
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = true;
        this.createdDate = java.time.LocalDate.now().toString();
    }

    // POLYMORPHISM - Overriding abstract method from Person
    @Override
    public void displayInfo() {
        System.out.println("=== USER INFO ===");
        System.out.println("ID: " + id);
        System.out.println("Username: " + username);
        System.out.println("Name: " + name);
        System.out.println("Role: " + role);
        System.out.println("Status: " + (active ? "Active" : "Blocked"));
    }

    // Getters
    public String getUserId()     { return id; }
    public String getUsername()   { return username; }
    public String getPassword()   { return password; }
    public UserRole getRole()     { return role; }
    public boolean isActive()     { return active; }
    public String getCreatedDate(){ return createdDate; }

    // Setters - ENCAPSULATION
    public void setPassword(String password)    { this.password = password; }
    public void setName(String name)            { this.name = name; }
    public void setEmail(String email)          { this.email = email; }
    public void setPhone(String phone)          { this.phone = phone; }
    public void setActive(boolean active)       { this.active = active; }

    // Save to text file format
    @Override
    public String toString() {
        return id + "|" + username + "|" + password + "|" + name + "|"
             + email + "|" + phone + "|" + role + "|" + active + "|" + createdDate;
    }

    // Read from text file format
    public static User fromString(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 9) {
                User u = new User(p[0], p[1], p[2], p[3], p[4], p[5], UserRole.valueOf(p[6]));
                u.setActive(Boolean.parseBoolean(p[7]));
                return u;
            }
        } catch (Exception e) {
            System.err.println("Error reading user: " + e.getMessage());
        }
        return null;
    }
}
