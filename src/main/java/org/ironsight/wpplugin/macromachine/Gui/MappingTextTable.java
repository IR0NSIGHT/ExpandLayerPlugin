package org.ironsight.wpplugin.macromachine.Gui;

import org.ironsight.wpplugin.macromachine.operations.MappingAction;
import org.ironsight.wpplugin.macromachine.operations.MappingPoint;
import org.ironsight.wpplugin.macromachine.operations.ValueProviders.IMappingValue;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.vecmath.Point2d;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class MappingTextTable extends LayerMappingPanel implements IMappingPointSelector {
    DefaultTableModel tableModel;
    TableModelListener listener;
    boolean[] inputSelection = new boolean[0];
    boolean blockSendingSelection = false;
    private Consumer<boolean[]> onSelect = f -> {
    };
    private JTable numberTable;
    private boolean groupValues = false;
    private JCheckBox groupValuesCheckBox;
    private boolean blockTableChanged;
    private Object beforeChange;
    private int eventRow;
    private int eventColumn;

    private void initTableModel() {
        if (mapping == null) return;
        MappingPointValue[][] data;
        Object[] columnNames;


        int amountRows = groupValues ? mapping.getMappingPoints().length : IMappingValue.range(mapping.input);
        data = new MappingPointValue[amountRows][];
        columnNames = new String[]{mapping.input.getName(), mapping.output.getName()};


        this.tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                eventColumn = column;
                eventRow = row;
                beforeChange = getValueAt(row, column);
                super.setValueAt(aValue, row, column);
            }
        };
        this.tableModel.addTableModelListener(this.listener);
        numberTable.setModel(tableModel);
    }

    @Override
    protected void updateComponents() {
        groupValues = groupValuesCheckBox.isSelected();
        blockTableChanged = true;
        if (groupValues) {
            initTableModel();   //rebuild
            int rowIndex = 0;
            for (MappingPoint p: mapping.getMappingPoints()) {
                //input
                tableModel.setValueAt(new MappingPointValue( mapping.input,p.input, true, rowIndex), rowIndex, 0);
                tableModel.setValueAt(new MappingPointValue(mapping.output,p.output, true, rowIndex ), rowIndex, 1);
                rowIndex++;
            }
        } else {
            int range = mapping.input.getMaxValue() - mapping.input.getMinValue() + 1;
            if (numberTable.getModel().getRowCount() != range || numberTable.getColumnCount() != 2) {
                initTableModel();   //rebuild
            }
            HashMap<Integer, Integer> mappingPointByInput = new HashMap<>();
            {
                int i = 0;
                for (MappingPoint p : mapping.getMappingPoints()) {
                    mappingPointByInput.put(p.input, i++);
                }
            }
            ArrayList<MappingPointValue[]> values =
                    new ArrayList<>(mapping.input.getMaxValue() - mapping.input.getMinValue() + 1);
            for (int i = 0; i < numberTable.getRowCount(); i++) {
                int numeric = i + mapping.input.getMinValue();
                boolean editable = mappingPointByInput.containsKey(numeric);
                int controlPointiD = mappingPointByInput.getOrDefault(numeric, -1);
                MappingPointValue inputV = new MappingPointValue(mapping.input,
                        numeric,
                        editable && !mapping.input.isDiscrete(),
                        controlPointiD);
                numberTable.getModel().setValueAt(inputV, i, 0);

                MappingPointValue outputV = new MappingPointValue(mapping.output,
                        mapping.map(inputV.numericValue),
                        editable,
                        controlPointiD);

                values.add(new MappingPointValue[]{inputV, outputV});
            }
            if (mapping.input.isDiscrete()) {
                values.sort(new Comparator<MappingPointValue[]>() {
                    @Override
                    public int compare(MappingPointValue[] o1, MappingPointValue[] o2) {    //compare input string names
                        return o1[0].mappingValue.valueToString(o1[0].numericValue)
                                .compareTo(o2[0].mappingValue.valueToString(o2[0].numericValue));
                    }
                });
            }
            int i = 0;
            for (MappingPointValue[] row : values) {
                numberTable.getModel().setValueAt(row[0], i, 0);
                numberTable.getModel().setValueAt(row[1], i++, 1);
            }

        }
        blockTableChanged = false;
    }

    @Override
    protected void initComponents() {
        this.setLayout(new BorderLayout());
        Border padding = new EmptyBorder(20, 20, 20, 20); // 20px padding on all sides
        Border whiteBorder = new EmptyBorder(5, 5, 5, 5); // 5px white border
        setBorder(BorderFactory.createCompoundBorder(whiteBorder, padding));

        // Add a TableModelListener to get a callback when a cell is edited
        numberTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                MappingPointValue value = (MappingPointValue) numberTable.getModel().getValueAt(row, column);
                return value.isEditable;
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (!numberTable.isRowSelected(row))    //user clicks into unselected column: clear selection and
                    // only selected the clicked one
                    numberTable.clearSelection();
                numberTable.addRowSelectionInterval(row, row);  //otherwise, just
                return super.getCellEditor(row, column);
            }
        };


        Font font = new Font("Arial", Font.PLAIN, 24);
        numberTable.setFont(font);
        MappingPointCellRenderer cellRenderer = new MappingPointCellRenderer();
        numberTable.setDefaultRenderer(Object.class, cellRenderer);
        numberTable.setRowHeight(cellRenderer.getPreferredHeight());
        numberTable.setDefaultEditor(Object.class, new MappingPointCellEditor());
        numberTable.setSelectionModel(new CustomListSelectionModel());
        JScrollPane scrollPane = new JScrollPane(numberTable);
        this.add(scrollPane, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        groupValuesCheckBox = new JCheckBox("Only Control Points");
        groupValuesCheckBox.addActionListener(f -> {
            if (groupValues != groupValuesCheckBox.isSelected()) {
                groupValues = groupValuesCheckBox.isSelected();
                updateComponents();
            }
        });
        buttons.add(groupValuesCheckBox);
        this.add(buttons, BorderLayout.SOUTH);
        listener = e -> {
            // Check if the event is due to a cell update
            if (e.getType() == TableModelEvent.UPDATE && !blockTableChanged) {
                int row = e.getFirstRow(); // Get the row of the edited cell
                int column = e.getColumn(); // Get the column of the edited cell
                Object newValue = tableModel.getValueAt(row, column); // Get the new value
                assert eventColumn == column;
                assert eventRow == row;
                MappingPointValue previous = beforeChange == null ? null : (MappingPointValue) beforeChange;
                int[] selectedRows = numberTable.getSelectedRows();
                parseAndSetValue(newValue, previous, selectedRows, column);
                if (column == 0) {  //input changed, rows moved, update selection
                    if (!groupValues) {
                        // row index is 1:1 mapped to input
                        int rowDiff = previous.numericValue - ((MappingPointValue) newValue).numericValue;
                        numberTable.clearSelection();
                        for (int rowIdx : selectedRows) {
                            if (rowIdx - rowDiff >= 0 && rowIdx - rowDiff < numberTable.getRowCount())
                                numberTable.addRowSelectionInterval(rowIdx - rowDiff, rowIdx - rowDiff);
                        }
                    } else {
                        // remove selection of old row
                        for (int rowIt = 0; rowIt < tableModel.getRowCount(); rowIt++) {
                            //add selection to new row
                            if (((MappingPointValue) tableModel.getValueAt(rowIt,column)).numericValue == ((MappingPointValue)newValue).numericValue ) {
                                numberTable.addRowSelectionInterval(rowIt, rowIt);
                            }
                            // delete selection of old row
                            if (((MappingPointValue) tableModel.getValueAt(rowIt,column)).numericValue == previous.numericValue ) {
                                numberTable.removeRowSelectionInterval(rowIt, rowIt);
                            }
                        }
                    }
                } else {
                    for (int rowIdx : selectedRows) {
                        numberTable.addRowSelectionInterval(rowIdx, rowIdx);
                    }
                }
            }
        };

        numberTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Add listener to scroll to the selected row
        numberTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int selectedRow = numberTable.getSelectedRow();
            if (selectedRow != -1) {
                numberTable.scrollRectToVisible(numberTable.getCellRect(selectedRow, 0, true));
            }

            boolean[] selection = new boolean[mapping.input.getMaxValue() - mapping.input.getMinValue() + 1];
            for (Integer row : numberTable.getSelectedRows()) {
                MappingPointValue mpv = (MappingPointValue) numberTable.getModel().getValueAt(row, 0);
                 // hightlight single selected input
                selection[mpv.numericValue - mapping.input.getMinValue()] = true;
            }
            this.inputSelection = selection;
            if (!blockSendingSelection)
                onSelect.accept(selection);
        });
    }

    protected boolean parseAndSetValue(Object changedValue, Object previousValue, int[] rows, int column) {
        assert changedValue instanceof MappingPointValue;
        MappingPointValue mpv = (MappingPointValue) changedValue;
        int diff = ((MappingPointValue) changedValue).numericValue;
        if (previousValue instanceof MappingPointValue) {
            diff = diff - ((MappingPointValue) previousValue).numericValue;
        }

        int targetValue = mpv.numericValue;
        MappingPoint[] points = mapping.getMappingPoints().clone();

        // set the same value for all selected rows as a bulk operation
        for (int row : rows) {
            MappingPointValue rowValue = (MappingPointValue) numberTable.getValueAt(row, column);
            if (!rowValue.isEditable || rowValue.mappingPointIndex == -1) continue;

            assert rowValue.isEditable : "can not update the value of a non-editable entry:" + rowValue;
            MappingPoint original = points[rowValue.mappingPointIndex];
            if (column == 0)    //INPUT UPDATED -> shfit all selected by diff value
                points[rowValue.mappingPointIndex] =
                        new MappingPoint(mapping.sanitizeInput(original.input + diff),
                                points[rowValue.mappingPointIndex].output);
            else    //OUTPUT UPDATED -> set target value as output for all selected
                points[rowValue.mappingPointIndex] =
                        new MappingPoint(points[rowValue.mappingPointIndex].input, mapping.sanitizeOutput(targetValue)
                        );

        }

        updateMapping(mapping.withNewPoints(points));
        return true;
    }

    @Override
    public void setOnSelect(Consumer<boolean[]> onSelect) {
        this.onSelect = onSelect;
    }

    @Override
    public void setSelectedInputs(boolean[] selectedPointIdx) {
        if (Arrays.equals(this.inputSelection, selectedPointIdx)) {
            return; //nothing to update here
        }
        blockSendingSelection = true;
        this.inputSelection = selectedPointIdx;

        numberTable.clearSelection();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            MappingPointValue value = (MappingPointValue) tableModel.getValueAt(i, 0);
            if (selectedPointIdx[value.numericValue - mapping.input.getMinValue()]) {
                numberTable.addRowSelectionInterval(i, i);
            }
        }


        repaint();
        blockSendingSelection = false;
    }

    // Custom selection model
    class CustomListSelectionModel extends DefaultListSelectionModel {
        @Override
        public void setSelectionInterval(int index0, int index1) {
            if (numberTable.isEditing()) return;
            super.setSelectionInterval(index0, index1);
        }
    }
}


