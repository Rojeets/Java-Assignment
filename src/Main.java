import frames.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Entry point of the Hall Booking Management System
public class Main {
    public static void main(String[] args) {
        // Use SwingUtilities to run on the Event Dispatch Thread (safe for Swing)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Use system look and feel so buttons look like the OS
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new LoginFrame().setVisible(true);
            }
        });
    }
}
