package abstraction;

// ABSTRACTION - Abstract class that cannot be instantiated directly
// Forces all subclasses to implement the displayInfo() method
public abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;

    public Person(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Abstract method - subclasses MUST override this method
    public abstract void displayInfo();

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
