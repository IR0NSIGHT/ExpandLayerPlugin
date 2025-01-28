package org.ironsight.wpplugin.expandLayerTool.Gui;

import org.ironsight.wpplugin.expandLayerTool.operations.IPositionValueSetter;
import org.ironsight.wpplugin.expandLayerTool.operations.InputOutputProvider;
import org.ironsight.wpplugin.expandLayerTool.operations.LayerMapping;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class OutputComboBox extends JComboBox<String> {
    Map<String, IPositionValueSetter> stringToGetter = new HashMap<>();

    public OutputComboBox() {
        InputOutputProvider.INSTANCE.subscribe(this::updateSelf);
        addItem(new LayerMapping.TestInputOutput());
        updateSelf();
    }

    public void updateSelf() {
        for (IPositionValueSetter s : InputOutputProvider.INSTANCE.setters) {
            addItem(s);
        }
    }

    private void addItem(IPositionValueSetter getter) {
        this.stringToGetter.put(getter.getName(), getter);
        this.addItem(getter.getName());
    }

    public IPositionValueSetter getSelectedProvider() {
        return stringToGetter.get((String) getSelectedItem());
    }

    public void SetSelected(IPositionValueSetter getter) {
        this.setSelectedItem(getter.getName());
    }
}

