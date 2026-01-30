import javax.swing.*;
import java.awt.*;

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

    public static JPanel getHintText() {
        String[] tips = {
                "If a path is empty, any path is matched",
                "Target host should be a valid hostname or IP address",
                "If a request is made to a domain, Host Redirector will not match it to the associated IP address.",
                "Rules are matched from the top to the bottom of the table."
        };
        JLabel tipsTitle = new JLabel("Help / Hints");
        Font font = tipsTitle.getFont();
        tipsTitle.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
        tipsTitle.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        tipsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JList<String> bulletList = new JList<>(tips);
        bulletList.setEnabled(false);      // makes it informational
        bulletList.setFocusable(false);
        bulletList.setAlignmentX(Component.LEFT_ALIGNMENT);

        bulletList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, false, false);

                label.setText("• " + value.toString());
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8)); // spacing like docs
                label.setAlignmentX(Component.LEFT_ALIGNMENT);

                return label;
            }
        });

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(tipsTitle);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(bulletList);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return infoPanel;
    }


    public static void addLeftAlignVertical(JPanel panel, JComponent... components) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i = 0; i < components.length; i++) {
            JComponent comp = components[i];
            // Force the component to align to the left
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(comp);
            // Add a 10px strut after every component EXCEPT the last one
            if (i < components.length - 1) {
                panel.add(Box.createVerticalStrut(10));
            }
        }
    }
}