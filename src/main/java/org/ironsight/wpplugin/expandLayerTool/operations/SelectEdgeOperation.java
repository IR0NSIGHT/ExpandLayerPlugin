package org.ironsight.wpplugin.expandLayerTool.operations;

import org.ironsight.wpplugin.expandLayerTool.Gui.OperationPanel;
import org.ironsight.wpplugin.expandLayerTool.pathing.RingFinder;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.layers.Annotations;
import org.pepsoft.worldpainter.operations.MouseOrTabletOperation;
import org.pepsoft.worldpainter.selection.SelectionBlock;
import org.pepsoft.worldpainter.selection.SelectionChunk;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.*;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE_BITS;

public class SelectEdgeOperation extends MouseOrTabletOperation {
    static final int CYAN = 9;
    public static final String NAME = "Select Edge Operation";
    public static final String DESCRIPTION = "1. Selects the edge of all blocks touching the input layer\n" +
            "2. expands the edge with a spraypaint gradient \n" +
            "3. paints it on the map as output layer";
    private static final String ID = "select_edge_operation";
    private final SelectEdgeOptions options = new SelectEdgeOptions();
    Random r = new Random();

    public SelectEdgeOperation() {
        super(NAME, DESCRIPTION, ID);
    }

    @Override
    public JPanel getOptionsPanel() {
        OperationPanel panel = new OperationPanel(options);
        panel.setRunner(this::run);
        return panel;
    }

    @Override
    protected void activate() throws PropertyVetoException {

    }

    private void run() {
        this.getDimension().setEventsInhibited(true);

        int annotationMatch = CYAN;

        LinkedList<Point> matches = new LinkedList<>();

        Iterator<? extends Tile> t = getDimension().getTiles().iterator();
        while (t.hasNext()) {
            Tile tile = t.next();
            if (options.cleanOutput) {
                if (options.outputAsSelection) {
                    tile.clearLayerData(SelectionBlock.INSTANCE);
                    tile.clearLayerData(SelectionChunk.INSTANCE);
                } else {
                    tile.clearLayerData(Annotations.INSTANCE);
                }
            }
            if ((options.inputFromSelection && tile.hasLayer(SelectionBlock.INSTANCE) || tile.hasLayer(SelectionChunk.INSTANCE)) || tile.hasLayer(Annotations.INSTANCE)) {
                for (int xInTile = 0; xInTile < TILE_SIZE; xInTile++) {
                    for (int yInTile = 0; yInTile < TILE_SIZE; yInTile++) {
                        final int x = xInTile + (tile.getX() << TILE_SIZE_BITS), y =
                                yInTile + (tile.getY() << TILE_SIZE_BITS);
                        if (options.inputFromSelection) {
                            if (tile.getBitLayerValue(SelectionBlock.INSTANCE, xInTile, yInTile) || getDimension().getBitLayerValueAt(SelectionChunk.INSTANCE, x, y))
                                matches.add(new Point(x, y));
                        } else {
                            int annotation = tile.getLayerValue(Annotations.INSTANCE, xInTile, yInTile);
                            if (annotation == annotationMatch) {
                                matches.add(new Point(x, y));
                            }
                        }

                    }
                }
                if (options.cleanInput) {
                    if (options.inputFromSelection) {
                        tile.clearLayerData(SelectionBlock.INSTANCE);
                        tile.clearLayerData(SelectionChunk.INSTANCE);
                    } else {
                        tile.clearLayerData(Annotations.INSTANCE);
                    }
                }
            }
        }

        HashMap<Point, Float> edge = new HashMap<>(matches.size());
        for (Point p : matches) {
            edge.put(p, 1f);
        }
        int amountRings = options.width;

        RingFinder start = new RingFinder(edge, 3);

        Set<Point> restrictions = new HashSet<>();
        switch (options.dir) {
            case BOTH:
                //no restrictions
                edge = start.ring(1);
                break;
            case OUTWARD:
                edge = start.ring(1);
                restrictions = start.ring(0).keySet();
                break;
            case INWARD:
                edge = start.ring(1);   //initial first outer layer
                restrictions = start.ring(2).keySet(); //initial second outwards layer

                //walk inwards once
                start = new RingFinder(edge, 1, restrictions);
                restrictions = edge.keySet();
                edge = start.ring(1);   //first inwards layer

                break;
            case OUT_AND_KEEP:
                //edge stays the same
                //no restrictions
                break;
        }
        start = new RingFinder(edge, amountRings, restrictions);

        int totalWidth = options.width;
        for (int w = 0; w < amountRings; w++) {
            float chance = options.gradient.getValue((float) w / totalWidth);
            applyWithStrength(start.ring(w).keySet(), chance);
        }

        this.getDimension().setEventsInhibited(false);
    }

    private void applyWithStrength(Collection<Point> points, float strength) {
        for (Point p : points) {
            if (strength > r.nextFloat()) {
                if (options.outputAsSelection)
                    getDimension().setBitLayerValueAt(SelectionBlock.INSTANCE, p.x, p.y, true);
                else getDimension().setLayerValueAt(Annotations.INSTANCE, p.x, p.y, CYAN);
            }
        }
    }

    @Override
    protected void deactivate() {

    }

    @Override
    protected void tick(int i, int i1, boolean b, boolean b1, float v) {


    }

    public static class SelectEdgeOptions {
        public int width = 3;
        public DIRECTION dir = DIRECTION.OUTWARD;
        public boolean cleanOutput = false;
        public boolean cleanInput = false;
        public boolean outputAsSelection = true;
        public boolean inputFromSelection = false;
        public Gradient gradient = new Gradient(new float[]{0.01f, 0.15f, 0.25f, 0.5f, 1f}, new float[]{1f, 0.4f, 0.2f, 0.1f
                , 0.03f});

        public enum DIRECTION {
            OUTWARD, INWARD, BOTH, OUT_AND_KEEP
        }
    }
}
