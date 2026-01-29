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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Extension implements BurpExtension {

    public static String name = "Host Redirector";
    public static Logging logging;

    public boolean isActive = false;
    public List<String> srcHosts = new ArrayList<>();
    public List<String> dstHosts = new ArrayList<>();
    public List<Pattern> srcPath = new ArrayList<>();
    public int tableSize; // we prefill the table size to improve performance while matching
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
    private JTextArea msgText;

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
        String[] columns = {"Source Host / IP", "Target Host / IP", "Source Path (regex)"};
        Object[][] sampleData = {
                {"sub1.dev.sbits.dev", "sub2.dev.sbits.dev", "/r1.*"},
                {"sub2.dev.sbits.dev", "sub3.dev.sbits.dev", "/r2.*"},
                {"sub3.dev.sbits.dev", "sub4.dev.sbits.dev", ""},
                {"sub1.dev.sbits.dev", "sub4.dev.sbits.dev", ""},

                {"prod.app.com", "dev.app.com", ""}
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
        updateHeader = new JCheckBox("Update HTTP 'Host' header?");
        updateHeader.addItemListener(e -> updateHeaderStatusUpdate(e.getStateChange() == ItemEvent.SELECTED));
        JLabel updateHostHeaderHint = new JLabel("(Find & replace original host with destination host on the Host header value)");

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
        checkboxPanel.add(Box.createVerticalStrut(2));
        checkboxPanel.add(updateHostHeaderHint);

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
        msgText = new JTextArea(10, 20);
        msgText.setLineWrap(true);
        msgText.setWrapStyleWord(true);
        msgText.setEditable(false);
        msgText.setText("Extension not activated");

        JScrollPane msgScrollPane = new JScrollPane(msgText);
        msgScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel hintText = UiUtils.getHintText();
        hintText.setBorder(new EmptyBorder(5, 5, 5, 5));

        // ---- Right wrapper (vertical layout) ----
        JPanel rightWrapper = new JPanel();
        UiUtils.addLeftAlignVertical(rightWrapper, rightPanel, msgScrollPane, hintText);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(5, 5, 5, 5));
        container.add(scrollPane, BorderLayout.CENTER); // table left
        container.add(rightWrapper, BorderLayout.EAST);   // buttons right
        mainPanel.add(container, BorderLayout.CENTER);

    }

    public void activate(boolean state) {
        if (state && syncHostLists() && setEditableTable(false)) {
            this.isActive = true;
            this.tableSize = this.srcHosts.size();
            setMsgText("Extension Active.\nDeactivate the extension to update the table.");
            return;
        }
        this.isActive = false;
        this.activateCheckBox.setSelected(false);
        setEditableTable(true);
        setMsgText("Extension not activated");;
    }

    private boolean syncHostLists() {
        this.srcHosts.clear();
        this.dstHosts.clear();

        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            // source host
            Object value = tableModel.getValueAt(i, 0);
            String host;
            if (value == null) {
                UiUtils.showError("Null value at Original Host of row " + rowCount);
                return false;
            }
            host = value.toString();
            if (Utils.isNotValidDomainNorIP(host)) {
                UiUtils.showError(host + " is not a valid domain.");
                return false;
            }
            srcHosts.add(host);

            // destination host
            value = tableModel.getValueAt(i, 1);
            if (value == null) {
                host = ""; // this match will be dropped
            } else {
                host = value.toString();
                // either the host should be empty string - this will be dropped
                // or the host should be a valid domain
                if (!"".equals(host) && Utils.isNotValidDomainNorIP(host)) {
                    UiUtils.showError(host + " is not a valid domain nor IP address.");
                    return false;
                }
            }
            dstHosts.add(host);

            // source path
            value = tableModel.getValueAt(i, 2);
            String path;
            Pattern pathPattern = null;
            if (value != null) {
                path = value.toString();
                if (!"".equals(path)) {
                    pathPattern = Utils.getRegex(path);
                    if (pathPattern == null) {
                        UiUtils.showError("\"" + path + "\" is not a valid regex pattern.\nPath should either be empty or a valid regex.");
                        return false;
                    }
                }
            }
            srcPath.add(pathPattern);
        }
        return true;
    }

    private boolean setEditableTable(boolean isEditable) {
        table.setEnabled(isEditable);
        addButton.setEnabled(isEditable);
        deleteButton.setEnabled(isEditable);
        return true;
    }

    private void setMsgText(String text) {
        msgText.setText(text);
    }

    private void appendInfoText(String text) {
        msgText.append("\n" + text);
    }

    private void updateHeaderStatusUpdate(boolean isEnabled) {
        this.updateHostHeader = isEnabled;
    }

}