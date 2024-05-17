package org.demo.wpplugin.operations;

import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.WorldPainterDialog;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.operations.Filter;
import org.pepsoft.worldpainter.operations.MouseOrTabletOperation;
import org.pepsoft.worldpainter.operations.PaintOperation;
import org.pepsoft.worldpainter.operations.StandardOptionsPanel;
import org.pepsoft.worldpainter.painting.Paint;
import org.pepsoft.worldpainter.panels.DefaultFilter;
import org.pepsoft.worldpainter.selection.SelectionBlock;
import org.pepsoft.worldpainter.selection.SelectionChunk;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Random;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

/**
 * Tool that applies frost to mountain peaks on left click
 * frost - no-frost will have a smooth, random and deterministic transition
 * Right click to configure the tool
 */
public class FrostedPeaks extends MouseOrTabletOperation implements PaintOperation {
        public FrostedPeaks() {
        super(NAME, DESCRIPTION, null, ID);

        System.out.println(NAME + " -> icon name of frosted peaks:" + this.getIcon());

    }

    private final StandardOptionsPanel optionsPanel = new StandardOptionsPanel(NAME,
            "<p>Left-Click to apply frost to all mountain peaks with a smooth transition.<br>" +
                    "0% frost at height 'at or above'.<br>" +
                    "100% frost at height 'at or below'.<br>" +
                    "Linear transition inbetween both heights.<br>" +
                    "Use 'inside selection' to restrict where tool applies.<br>" +
                    "Will ignore other filters.<br>" +
                    "by Ir0nsight<p>");

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    private static boolean isFrosted(int x, int y, int thisHeight, int minHeight, int transitionHeight, Random r) {
        if (thisHeight >= minHeight + transitionHeight)
            return true;
        if (thisHeight < minHeight)
            return false;
        float point = (thisHeight - minHeight) * 1f / transitionHeight;

        float rand = r.nextFloat();
        return (rand < point);
    }

    public static Filter getAppFilter(App appInstance) {
        try {
            // Get the Class object for the App class
            Class<?> appClass = appInstance.getClass();

            // Get the private field mapDragControl
            Field field = appClass.getDeclaredField("filter");

            // Make the private field accessible
            field.setAccessible(true);

            // Get the value of the field from the instance
            return (Filter) field.get(appInstance);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        if (inverse) {
            //ignored
        } else {

            Filter f = getAppFilter(App.getInstance());

            if (!(f instanceof DefaultFilter))
                return;

            final int minHeight = ((DefaultFilter) f).getAboveLevel();

            final int fullHeight = ((DefaultFilter) f).getBelowLevel();

            getDimension().visitTilesForEditing().andDo(tile -> {
                Random r = new Random((long) tile.getX()+tile.getY());

                int xTile = tile.getX() * TILE_SIZE, yTile = tile.getY() * TILE_SIZE;

                for (int x = 0; x < TILE_SIZE; x++) {
                    for (int y = 0; y < TILE_SIZE; y++) {
                        //filter out point if Filter requires to be inside selection
                        if (((DefaultFilter) f).isInSelection() && !absCoordInSelection(xTile + x, yTile + y))
                            continue;
                        if (isFrosted(x, y, tile.getIntHeight(x, y), minHeight, fullHeight - minHeight, r))
                            tile.setBitLayerValue(Frost.INSTANCE, x, y, true);
                    }
                }
            });
        }
    }

    private boolean absCoordInSelection(int x, int y) {
        return getDimension().getBitLayerValueAt(SelectionChunk.INSTANCE, x, y) || getDimension().getBitLayerValueAt(SelectionBlock.INSTANCE, x, y);
    }

    public void popup() {
        WorldPainterDialog dialog = new ConfigurationDialog(App.getInstance());
        dialog.setLocationRelativeTo(App.getInstance());
        dialog.setVisible(true);
    }


    /**
     * The globally unique ID of the operation. It's up to you what to use here. It is not visible to the user. It can
     * be a FQDN or package and class name, like here, or you could use a UUID. As long as it is globally unique.
     */
    static final String ID = "org.demo.wpplugin.FrostedPeaks.v1";

    /**
     * Human-readable short name of the operation.
     */
    static final String NAME = "Frosted Peaks";

    /**
     * Human-readable description of the operation. This is used e.g. in the tooltip of the operation selection button.
     */
    static final String DESCRIPTION = "Globally apply a layer of frost starting at level 120, with a smooth transition";

    @Override
    public Paint getPaint() {
        return null;
    }

    @Override
    public void setPaint(Paint paint) {

    }
}