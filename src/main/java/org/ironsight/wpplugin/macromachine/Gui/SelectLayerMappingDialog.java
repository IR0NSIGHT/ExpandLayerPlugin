package org.ironsight.wpplugin.macromachine.Gui;

import org.ironsight.wpplugin.macromachine.operations.LayerMapping;
import org.ironsight.wpplugin.macromachine.operations.LayerMappingContainer;
import org.ironsight.wpplugin.macromachine.operations.SaveableAction;
import org.ironsight.wpplugin.macromachine.operations.ValueProviders.IDisplayUnit;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class SelectLayerMappingDialog extends JDialog {
    public SelectLayerMappingDialog(ArrayList<SaveableAction> layerMappings, Consumer<SaveableAction> onSubmit,
                                    @Nullable SaveableAction newAction) {
        super();
        init(layerMappings, onSubmit, newAction);
        this.setModal(true);
        this.pack();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setTitle("Select Layer Mapping");
        for (int i = 0; i < 20; i++)
            LayerMappingContainer.addDefaultMappings(LayerMappingContainer.INSTANCE);
        ArrayList<SaveableAction> layerMappings = new ArrayList<>(LayerMappingContainer.INSTANCE.queryAll());
        Dialog dlg = new SelectLayerMappingDialog(layerMappings , System.out::println, LayerMapping.getNewEmptyAction());
        dlg.setVisible(true);
    }

    private void init(ArrayList<SaveableAction> layerMappings, Consumer<SaveableAction> onSubmit,  @Nullable SaveableAction specialTopAction) {
        JList<SaveableAction> list = new JList<>();
        DefaultListModel<SaveableAction> listModel = new DefaultListModel<>();
        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new MappingTableCellRenderer());

        layerMappings.sort(Comparator.comparing(IDisplayUnit::getName));
        if (specialTopAction != null)
            listModel.addElement(specialTopAction);
        for (SaveableAction mapping : layerMappings) {
            listModel.addElement(mapping);
        }
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            SaveableAction selected = list.getSelectedValue();
            onSubmit.accept(selected);
            this.dispose();
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            this.dispose();
        });

        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane pane = new JScrollPane(list);
        panel.add(pane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(panel, BorderLayout.CENTER);
    }


}
