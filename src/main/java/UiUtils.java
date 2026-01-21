import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class UiUtils {

    /**
     * Displays a critical error message with an "X" icon.
     */
    public static void showError(String message) {
        showMessage(message, Extension.name + " Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a warning message with a "!" icon.
     */
    public static void showWarning(String message) {
        showMessage(message, Extension.name + " Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Displays a simple info message with an "i" icon.
     */
    public static void showInfo(String message) {
        showMessage(message, Extension.name + " Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Private helper to handle thread safety and display.
     */
    private static void showMessage(String message, String title, int messageType) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    messageType
            );
        });
    }
}