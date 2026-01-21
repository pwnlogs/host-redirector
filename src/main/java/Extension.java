import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.logging.Logging;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Extension implements BurpExtension {

    public static String name = "Host Redirector";
    public static Logging logging;
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?!-)[A-Za-z0-9-]+([\\-\\.]{1}[a-z0-9]+)*\\.[A-Za-z]{2,6}$"
    );

    public boolean isActive = false;
    public List<String> srcHosts = new ArrayList<>();
    public List<String> dstHosts = new ArrayList<>();
    public Map<ToolType, Boolean> isToolTypeEnabled = new HashMap<>() {{
        put(ToolType.PROXY, true);
        put(ToolType.INTRUDER, true);
        put(ToolType.REPEATER, true);
        put(ToolType.SCANNER, true);
    }};
    public boolean updateHostHeader = false;

    private JPanel mainPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JCheckBox activateCheckBox;
    private SourceToolCheckBox proxyCheckBox;
    private SourceToolCheckBox intruderCheckBox;
    private SourceToolCheckBox repeaterCheckBox;
    private SourceToolCheckBox scannerCheckBox;
    private JCheckBox updateHeader;
    private JButton addButton;
    private JButton deleteButton;
    private JTextArea infoText;

    private RedirectHandler redirectHandler;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName(name);
        Extension.logging = montoyaApi.logging();
        registerHandlers();
        Registration registration = montoyaApi.http().registerHttpHandler(redirectHandler);
        initializeTab();
        montoyaApi.userInterface().registerSuiteTab("Redirect Hosts", mainPanel);
    }

    private void registerHandlers() {
        redirectHandler = new RedirectHandler(this);
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
        addButton = new JButton("Add");
        addButton.addActionListener((ActionEvent e) -> {
            tableModel.addRow(new Object[]{"source", "destination"});
        });
        // delete button
        deleteButton = new JButton("Delete");
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

        activateCheckBox = new JCheckBox("Activate Extension", false);
        activateCheckBox.addItemListener(e -> activate(e.getStateChange() == ItemEvent.SELECTED));
        proxyCheckBox = new SourceToolCheckBox("Proxy", ToolType.PROXY, this);
        intruderCheckBox = new SourceToolCheckBox("Intruder", ToolType.INTRUDER, this);
        repeaterCheckBox = new SourceToolCheckBox("Repeater", ToolType.REPEATER, this);
        scannerCheckBox = new SourceToolCheckBox("Scanner", ToolType.SCANNER, this);
        updateHeader = new JCheckBox("Update HTTP 'Host' header?\n(find & replace original host with destination host on Host header value");
        updateHeader.addItemListener(e -> updateHeaderStatusUpdate(e.getStateChange() == ItemEvent.SELECTED));

        checkboxPanel.add(activateCheckBox);
        checkboxPanel.add(Box.createVerticalStrut(50));
        checkboxPanel.add(checklistLabel);
        checkboxPanel.add(Box.createVerticalStrut(10));
        checkboxPanel.add(proxyCheckBox);
        checkboxPanel.add(Box.createVerticalStrut(5));
        checkboxPanel.add(intruderCheckBox);
        checkboxPanel.add(Box.createVerticalStrut(5));
        checkboxPanel.add(repeaterCheckBox);
        checkboxPanel.add(Box.createVerticalStrut(5));
        checkboxPanel.add(scannerCheckBox);
        checkboxPanel.add(Box.createVerticalStrut(50));
        checkboxPanel.add(updateHeader);

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

        // ---- Multiline text area ----
        infoText = new JTextArea(10, 20);
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setEditable(false);
        infoText.setText("Extension not activated");

        JScrollPane textScrollPane = new JScrollPane(infoText);
        textScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        // ---- Right wrapper (vertical layout) ----
        JPanel rightWrapper = new JPanel();
        rightWrapper.setLayout(new BoxLayout(rightWrapper, BoxLayout.Y_AXIS));
        rightWrapper.add(rightPanel);
        rightWrapper.add(Box.createVerticalStrut(10));
        rightWrapper.add(textScrollPane);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(5, 5, 5, 5));
        container.add(scrollPane, BorderLayout.CENTER); // table left
        container.add(rightWrapper, BorderLayout.EAST);   // buttons right
        mainPanel.add(container, BorderLayout.CENTER);

    }

    public void activate(boolean state) {
        if (state && syncHostLists() && setEditableTable(false)) {
            this.isActive = true;
            setInfoText("Extension Active.\nDeactivate the extension to update the table.");
            return;
        }
        this.isActive = false;
        this.activateCheckBox.setSelected(false);
        setEditableTable(true);
        setInfoText("Extension not activated");;
    }

    private boolean syncHostLists() {
        this.srcHosts.clear();
        this.dstHosts.clear();

        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            // source host
            Object value = tableModel.getValueAt(i, 0);
            if (value == null) {
                UiUtils.showError("Null value at Original Host of row " + rowCount);
                return false;
            }
            String host = value.toString();
            if (isNotValidDomain(host)) {
                UiUtils.showError(host + " is not a valid domain.");
                return false;
            }
            if (srcHosts.contains(host)) {
                UiUtils.showError(host + " has duplicate entries.");
                return false;
            }
            srcHosts.add(host);

            // destination host
            value = tableModel.getValueAt(i, 1);
            if (value == null) {
                UiUtils.showError("Null value at Original Host of row " + rowCount);
                return false;
            }
            host = value.toString();
            if (isNotValidDomain(host)) {
                UiUtils.showError(host + " is not a valid domain.");
                return false;
            }
            if (srcHosts.contains(host)) {
                UiUtils.showError(host + " has duplicate entries.");
                return false;
            }
            dstHosts.add(host);
        }
        return true;
    }

    private boolean isNotValidDomain(String host) {
        return !DOMAIN_PATTERN.matcher(host).matches();
    }

    private boolean setEditableTable(boolean isEditable) {
        table.setEnabled(isEditable);
        addButton.setEnabled(isEditable);
        deleteButton.setEnabled(isEditable);
        return true;
    }

    private void setInfoText(String text) {
        infoText.setText(text);
    }

    private void appendInfoText(String text) {
        infoText.append("\n" + text);
    }

    private void updateHeaderStatusUpdate(boolean isEnabled) {
        this.updateHostHeader = isEnabled;
    }

}