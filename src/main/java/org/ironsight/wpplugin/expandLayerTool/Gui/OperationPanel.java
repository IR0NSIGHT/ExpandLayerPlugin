package org.ironsight.wpplugin.expandLayerTool.Gui;

import org.ironsight.wpplugin.expandLayerTool.operations.SelectEdgeOperation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.ironsight.wpplugin.expandLayerTool.Version.VERSION;
import static org.ironsight.wpplugin.expandLayerTool.operations.SelectEdgeOperation.DESCRIPTION;
import static org.ironsight.wpplugin.expandLayerTool.operations.SelectEdgeOperation.NAME;

public class OperationPanel extends JPanel {
    private SelectEdgeOperation.SelectEdgeOptions options;

    public void setRunner(Runnable runner) {
        this.runner = runner;
    }

    public static void main(String[] args) {
        SelectEdgeOperation.SelectEdgeOptions options = new SelectEdgeOperation.SelectEdgeOptions();

        OperationPanel panel = new OperationPanel(options);
        panel.setRunner(() -> {
            System.out.println("Running operation");
        });
        JFrame frame = new JFrame("gradient editor test");
        frame.add(panel);
        frame.setSize(new Dimension(300,1000));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public OperationPanel(SelectEdgeOperation.SelectEdgeOptions options) {
        this.options = options;
        JPanel outer = this;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel twoColumnsPanel = new JPanel(new GridLayout(0, 2));

        {   //SPINNER WIDTH
            {
                JLabel label = new JLabel("width:");
                twoColumnsPanel.add(label);
                SpinnerNumberModel model = new SpinnerNumberModel(options.width, 1, 100, 1f); // initialValue, min,
                JSpinner spinner = new JSpinner(model);
                spinner.addChangeListener(e -> options.width = ((Double) spinner.getValue()).intValue());
                spinner.setToolTipText("how wide the output edge should be");
                twoColumnsPanel.add(spinner);
            }
        }

        {
            // Create a JComboBox with options
            String[] listOptions = {"Outwards", "Inwards", "Both", "Out and keep"};
            JComboBox<String> dropdown = new JComboBox<>(listOptions);

// Map the list options to the corresponding directions
            Map<String, SelectEdgeOperation.SelectEdgeOptions.DIRECTION> directionMap = new HashMap<>();
            directionMap.put("Outwards", SelectEdgeOperation.SelectEdgeOptions.DIRECTION.OUTWARD);
            directionMap.put("Inwards", SelectEdgeOperation.SelectEdgeOptions.DIRECTION.INWARD);
            directionMap.put("Both", SelectEdgeOperation.SelectEdgeOptions.DIRECTION.BOTH);
            directionMap.put("Out and keep", SelectEdgeOperation.SelectEdgeOptions.DIRECTION.OUT_AND_KEEP);

// Reverse map to find the key by value
            Map<SelectEdgeOperation.SelectEdgeOptions.DIRECTION, String> reverseMap = new HashMap<>();
            directionMap.forEach((key, value) -> reverseMap.put(value, key));

// Add an action listener to handle option selection
            dropdown.addActionListener(e -> {
                String selectedOption = (String) dropdown.getSelectedItem();
                if (selectedOption != null) {
                    options.dir = directionMap.get(selectedOption);
                }
            });

// Set the selected item based on the current direction
            dropdown.setSelectedItem(reverseMap.get(options.dir));

            twoColumnsPanel.add(new JLabel("direction:"));
            dropdown.setToolTipText("in which direction the tool will grow the input layer");
            twoColumnsPanel.add(dropdown);
        }

        {   // INPUT
            JButton button = new JButton();
            button.setText(options.inputFromSelection ? "selection" : "cyan annotation");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.inputFromSelection = !options.inputFromSelection;
                    button.setText(options.inputFromSelection ? "selection" : "cyan annotation");
                }
            });
            button.setToolTipText("the layer to be used as an input");
            twoColumnsPanel.add(new JLabel("input:"));
            twoColumnsPanel.add(button);
        }

        {   // OUTPUT
            JButton button = new JButton();
            button.setText(options.outputAsSelection ? "selection" : "cyan annotation");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.outputAsSelection = !options.outputAsSelection;
                    button.setText(options.outputAsSelection ? "selection" : "cyan annotation");

                }
            });
            twoColumnsPanel.add(new JLabel("output:"));
            button.setToolTipText("the layer to be used as output. Will be painted on the map when the tool is run.");
            twoColumnsPanel.add(button);
        }

        {
            JButton button3 = new JButton("edit");
            // Add action listeners to handle button click events
            button3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showArrayEditorDialog();
                }
            });
            twoColumnsPanel.add(new JLabel("gradient"));
            button3.setToolTipText("the gradient that is used when the layer is expanded.");
            twoColumnsPanel.add(button3);
        }
        {   // CLEAN OUTPUT
            JCheckBox checkBox = new JCheckBox("clear output layer");
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.cleanOutput = checkBox.isSelected();
                }
            });
            checkBox.setSelected(options.cleanOutput);
            twoColumnsPanel.add(checkBox);
        }
        {   //CLEAN INPUT
            JCheckBox checkBox = new JCheckBox("clear input layer");
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.cleanInput = checkBox.isSelected();
                }
            });
            checkBox.setSelected(options.cleanInput);
            twoColumnsPanel.add(checkBox);
        }

        {   //HELP BUTTON
            JButton button3 = new JButton("Help");
            final Component main = this;
            // Add action listeners to handle button click events
            button3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        // The GitHub URL to open
                        String url = "https://github.com/IR0NSIGHT/ExpandLayerPlugin/blob/master/README.md";
                        // Open the URL in the default browser
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Component frame = SwingUtilities.getRoot(main);
                        JOptionPane.showMessageDialog(frame, "Failed to open online help URL: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            button3.setToolTipText("open online help");
            twoColumnsPanel.add(button3);
        }

        {   //EXECUTE BUTTON
            JButton button3 = new JButton("Run");
            // Add action listeners to handle button click events
            button3.addActionListener(e -> {
                if (runner != null)
                    runner.run();
            });
            button3.setToolTipText("execute the tool operation and place down the expanded output layer");
            twoColumnsPanel.add(button3);
        }

        for (Component p : twoColumnsPanel.getComponents()) {
            if (p instanceof JLabel) {
                ((JLabel) p).setHorizontalAlignment(SwingConstants.CENTER); // Horizontal center
                ((JLabel) p).setVerticalAlignment(SwingConstants.CENTER);   // Vertical center
            }
            if (p instanceof JComboBox) {
                ((JLabel) ((JComboBox) p).getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
            }
        }
        twoColumnsPanel.setMaximumSize(twoColumnsPanel.getPreferredSize());


        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        {
            JLabel label = new JLabel(NAME);
            label.setFont(new Font("Arial", Font.BOLD, 24));
            header.add(label);
        }

        JTextArea textArea = new JTextArea(DESCRIPTION +  "\n\nv"+VERSION, 4, 20);
        {
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setOpaque(false);   // Optional: remove background
            textArea.setBorder(null);    // Optional: remove border
            textArea.setMaximumSize(new Dimension(textArea.getPreferredSize().width,50));
        }

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT));
        content.add(header);
        content.add(textArea);
        content.add(twoColumnsPanel);
        gradientDisplay = new GradientDisplay(options.gradient);
        gradientDisplay.setPreferredSize(new Dimension(twoColumnsPanel.getPreferredSize().width,100));
        content.add(gradientDisplay);
        content.add(Box.createVerticalGlue()); // extra space at the bottom
        Arrays.stream(content.getComponents())
                .map(Component::getPreferredSize)
                .map(Dimension::getWidth)
                .max(Comparator.comparing(w -> w))
                .ifPresent(maxW -> content.setPreferredSize(new Dimension(maxW.intValue(), Integer.MAX_VALUE)));
        content.setMaximumSize(content.getPreferredSize());
        content.setMinimumSize(content.getPreferredSize());
        System.out.println(content.getMaximumSize());
        outer.add(content);
    }

    private GradientDisplay gradientDisplay;
    private Runnable runner;

    public void showArrayEditorDialog() {
        // Create the dialog
        JDialog dialog = new JDialog((Frame) null, "Edit Arrays", true); // Modal dialog
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create the PixelGrid instance with the gradient
        GradientDisplay pixelGrid = new GradientDisplay(options.gradient);
        JPanel gridPanel = new JPanel(new GridLayout(0, 2));
        gridPanel.add(pixelGrid);
        gridPanel.add(new GradientEditor(options.gradient, g -> {pixelGrid.setGradient(g); gradientDisplay.setGradient(g);}, grad -> {
            this.options.gradient = grad;
            dialog.dispose();
        }));

        // Add components to the dialog
        dialog.add(gridPanel, BorderLayout.NORTH);

        dialog.setMinimumSize(new Dimension(300, 200));
        dialog.pack();
        // Set dialog size and make it visible
        dialog.setLocationRelativeTo(null); // Center on screen
        dialog.setVisible(true);
    }
}
