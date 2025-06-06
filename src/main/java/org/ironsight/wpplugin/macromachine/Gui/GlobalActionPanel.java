package org.ironsight.wpplugin.macromachine.Gui;

import org.ironsight.wpplugin.macromachine.MacroMachinePlugin;
import org.ironsight.wpplugin.macromachine.operations.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ironsight.wpplugin.macromachine.Gui.ActionEditor.createDialog;

// top level panel that contains a selection list of macros/layers/input/output on the left, like a file browser
// and an editor for the currently selected action on the right
public class GlobalActionPanel extends JPanel {
    public static final String MAPPING_EDITOR = "mappingEditor";
    public static final String INVALID_SELECTION = "invalidSelection";
    public static final String MACRO_DESIGNER = "macroDesigner";
    static JTextArea logPanel;
    static final int MAX_LOG_LINES = 2000;
    MacroTreePanel macroTreePanel;
    MacroDesigner macroDesigner;
    ActionEditor mappingEditor;
    //consumes macro to apply to map. callback for "user pressed apply-macro"
    Function<MappingMacro, Collection<ExecutionStatistic>> applyMacro;
    CardLayout layout;
    JPanel editorPanel;
    private UUID currentSelectedMacro;
    private UUID currentSelectedLayer;
    private SELECTION_TPYE selectionType = SELECTION_TPYE.INVALID;

    public GlobalActionPanel(Function<MappingMacro, Collection<ExecutionStatistic>> applyToMap) {
        this.applyMacro = applyToMap;

        init();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MappingMacroContainer macros = MappingMacroContainer.getInstance();
        LayerMappingContainer layers = LayerMappingContainer.INSTANCE;

        macros.readFromFile();
        layers.readFromFile();
    //    LayerMappingContainer.INSTANCE.subscribe(() -> LayerMappingContainer.INSTANCE.writeToFile());
    //    MappingMacroContainer.getInstance().subscribe(() -> MappingMacroContainer.getInstance().writeToFile());
        JDialog diag = createDialog(null, f -> Collections.emptyList());
        diag.setVisible(true);
    }

    /**
     * Returns the current timestamp in a human-readable format.
     *
     * @return The current timestamp as a String in the format "yyyy-MM-dd HH:mm:ss".
     */
    public static String getCurrentTimestamp() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Define the formatter for the desired human-readable format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the current date and time
        return now.format(formatter);
    }

    public static void ErrorPopUp(String message) {
        logMessage(message);
        JOptionPane.showMessageDialog(null, message, "Error",
                // Title of the dialog
                JOptionPane.ERROR_MESSAGE
                // Type of message (error icon)
        );

    }

    // Method to log messages
    public static void logMessage(String message) {
        if (logPanel != null) {
            // Append the new log message
            logPanel.append(getCurrentTimestamp()+":\n");
            logPanel.append(message + "\n");

            // Limit the number of lines in the log text area
            int lineCount = logPanel.getLineCount();
            if (lineCount > MAX_LOG_LINES) {
                try {
                    int end = logPanel.getLineEndOffset(lineCount - 1 - MAX_LOG_LINES);
                    logPanel.replaceRange("", 0, end);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Scroll to the end
            logPanel.setCaretPosition(logPanel.getDocument().getLength());
        } else {
            System.err.println(message);
        }
        MacroMachinePlugin.error(message);
    }

    private void applyToMap(MappingMacro macro) {
        Collection<ExecutionStatistic> statistic = applyMacro.apply(macro);
        logMessage("apply macro " + macro.getName() + " to map:\n" +
                statistic.stream().map(ExecutionStatistic::toString).collect(Collectors.joining("\n")));
    }

    private void onUpdate() {
        LayerMapping mapping = LayerMappingContainer.INSTANCE.queryById(currentSelectedLayer);
        MappingMacro macro = MappingMacroContainer.getInstance().queryById(currentSelectedMacro);
        if (macro == null && selectionType == SELECTION_TPYE.MACRO) selectionType = SELECTION_TPYE.INVALID;

        if (mapping == null && selectionType == SELECTION_TPYE.ACTION) selectionType = SELECTION_TPYE.INVALID;


        switch (selectionType) {
            case MACRO:
                macroDesigner.setMacro(macro, true);
                layout.show(editorPanel, MACRO_DESIGNER);
                break;
            case ACTION:
                mappingEditor.setMapping(mapping);
                layout.show(editorPanel, MAPPING_EDITOR);
                break;
            case INVALID:
                layout.show(editorPanel, INVALID_SELECTION);
                break;
        }
    }

    private void init() {
        MappingMacroContainer.getInstance().subscribe(this::onUpdate);
        LayerMappingContainer.INSTANCE.subscribe(this::onUpdate);

        this.setLayout(new BorderLayout());
        macroTreePanel = new MacroTreePanel(MappingMacroContainer.getInstance(),
                LayerMappingContainer.INSTANCE,
                this::applyToMap,
                this::onSelect);
        macroTreePanel.setMaximumSize(new Dimension(200, 0));

        macroDesigner = new MacroDesigner(this::onSubmitMacro);
        mappingEditor = new ActionEditor(this::onSubmitMapping);

        editorPanel = new JPanel(new CardLayout());
        editorPanel.add(mappingEditor, MAPPING_EDITOR);
        editorPanel.add(macroDesigner, MACRO_DESIGNER);
        editorPanel.add(new JPanel(), INVALID_SELECTION);
        layout = (CardLayout) editorPanel.getLayout();
        layout.show(editorPanel, MACRO_DESIGNER);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Designer", editorPanel);
        this.add(tabbedPane, BorderLayout.CENTER);

        JPanel executionPanel = new JPanel(new BorderLayout());
        logPanel = new JTextArea();
        logPanel.setEditable(false); // Make it read-only
        logPanel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane writeWindowScroll = new JScrollPane(logPanel);
        executionPanel.add(writeWindowScroll, BorderLayout.CENTER);
        tabbedPane.add("Log", executionPanel);

        this.add(macroTreePanel, BorderLayout.WEST);
        onUpdate();
    }

    private void onSelect(SaveableAction action) {
        if (action instanceof MappingMacro) {
            currentSelectedMacro = action.getUid();
            selectionType = SELECTION_TPYE.MACRO;
        } else if (action instanceof LayerMapping) {
            currentSelectedLayer = action.getUid();
            selectionType = SELECTION_TPYE.ACTION;
        }
        onUpdate();
    }

    private void onSubmitMapping(LayerMapping mapping) {
        LayerMappingContainer.INSTANCE.updateMapping(mapping, f -> {
        });
    }

    private void onSubmitMacro(MappingMacro macro) {
        MappingMacroContainer.getInstance().updateMapping(macro, e -> {
            ErrorPopUp("Unable to save macro: " + e);
        });
    }

    enum SELECTION_TPYE {
        MACRO, ACTION, INVALID
    }
}
