package org.ironsight.wpplugin.macromachine.Gui;

import org.ironsight.wpplugin.macromachine.operations.ActionType;

import javax.swing.*;

public class ActionTypeComboBox extends JComboBox<String> {

    public ActionTypeComboBox() {
        for (ActionType t : ActionType.values())
            addItem(t.getDisplayName());
    }

    public ActionType getSelectedProvider() {
        return ActionType.values()[getSelectedIndex()];
    }

    public void setTo(ActionType type) {
        this.setSelectedIndex(type.ordinal());
        assert this.getSelectedProvider().equals(type);
    }
}

