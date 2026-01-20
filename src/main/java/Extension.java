import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Extension implements BurpExtension {

    private JPanel mainPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JCheckBox proxyCheckBox;
    private JCheckBox intruderCheckBox;
    private JCheckBox repeaterCheckBox;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("Host Redirector");
        initializeTab();
        montoyaApi.userInterface().registerSuiteTab("Redirect Hosts", mainPanel);
    }

    private void initializeTab() {
        mainPanel = new JPanel(new BorderLayout());

        // table
        String[] columns = {"Original host", "Target host"};
        Object[][] sampleData = {
                {"production.pwnlogs.dev", "development.pwnlogs.dev"}
        };

        tableModel = new DefaultTableModel(sampleData, columns);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.add(scrollPane, BorderLayout.WEST);

        // buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 20));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        // add button
        JButton addButton = new JButton("Add");
        addButton.addActionListener((ActionEvent e) -> {
            tableModel.addRow(new Object[]{"", ""});
        });
        // delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener((ActionEvent e) -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Select a row to delete");
            }
        });
        // Add buttons to panel with spacing
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(deleteButton);

        // checklist
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBorder(new EmptyBorder(5, 20, 5, 100));
        JLabel checklistLabel = new JLabel("Enable for:");
        checklistLabel.setFont(checklistLabel.getFont().deriveFont(Font.BOLD));
        checklistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        proxyCheckBox = new JCheckBox("Proxy");
        intruderCheckBox = new JCheckBox("Intruder");
        repeaterCheckBox = new JCheckBox("Repeater");

        checkboxPanel.add(checklistLabel);
        checkboxPanel.add(Box.createVerticalStrut(10));
        checkboxPanel.add(proxyCheckBox);
        checkboxPanel.add(Box.createVerticalStrut(5));
        checkboxPanel.add(intruderCheckBox);
        checkboxPanel.add(Box.createVerticalStrut(5));
        checkboxPanel.add(repeaterCheckBox);

        // assembling the tab
        // -------------------
        // Right-side container (buttons + checklist)
        // -------------------
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JSeparator verticalSeparator = new JSeparator(JSeparator.VERTICAL);

        buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        checkboxPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        rightPanel.add(buttonPanel);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(verticalSeparator);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(checkboxPanel);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(5, 5, 5, 5));
        container.add(scrollPane, BorderLayout.CENTER); // table left
        container.add(rightPanel, BorderLayout.EAST);   // buttons right
        mainPanel.add(container, BorderLayout.CENTER);

    }
}