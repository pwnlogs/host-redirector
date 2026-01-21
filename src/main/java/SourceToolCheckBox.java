import burp.api.montoya.core.ToolType;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class SourceToolCheckBox extends JCheckBox {

    private final Extension extension;
    private final ToolType toolType;

    public SourceToolCheckBox(String label, ToolType toolType, Extension extension) {
        super(label);
        this.toolType = toolType;
        this.extension = extension;
        this.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                statusChanged(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
    }

    protected void statusChanged(boolean isEnabled) {
        extension.isToolTypeEnabled.put(this.toolType, isEnabled);
    }

}