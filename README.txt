=== Hall Booking Management System ===
CT038-3-2-OODJ | Object Oriented Development with Java

HOW TO RUN:
1. Open the project in IntelliJ IDEA, Eclipse, or any Java IDE.
2. Make sure Java 11 or higher is installed.
3. Set the source root to: src/
4. Run Main.java

DEFAULT LOGIN ACCOUNTS:
  Role          | Username   | Password
  Administrator | admin      | admin123
  Manager       | manager    | manager123
  Scheduler     | scheduler  | scheduler123
  Customer      | customer1  | customer123

DATA FILES:
  All data is stored in the data/ folder (auto-created on first run):
  - data/users.txt
  - data/halls.txt
  - data/bookings.txt
  - data/schedules.txt
  - data/issues.txt

PROJECT STRUCTURE:
  src/
    Main.java                   -- Entry point
    abstraction/
      Person.java               -- Abstract parent class (Abstraction)
    models/
      UserRole.java             -- Enum: SCHEDULER, CUSTOMER, ADMINISTRATOR, MANAGER
      HallType.java             -- Enum: AUDITORIUM, BANQUET_HALL, MEETING_ROOM
      User.java                 -- User model (extends Person - Inheritance)
      Hall.java                 -- Hall model
      Booking.java              -- Booking model
      HallSchedule.java         -- Schedule model
      Issue.java                -- Issue/complaint model
    data/
      DataHandler.java          -- Reads/writes all .txt files
    frames/
      LoginFrame.java           -- Login screen
      RegisterFrame.java        -- Customer registration
      SchedulerFrame.java       -- Scheduler dashboard
      CustomerFrame.java        -- Customer dashboard
      AdminFrame.java           -- Administrator dashboard
      ManagerFrame.java         -- Manager dashboard
