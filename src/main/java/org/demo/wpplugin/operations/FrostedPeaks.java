package org.demo.wpplugin.operations;

import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.WorldPainterDialog;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.operations.MouseOrTabletOperation;
import org.pepsoft.worldpainter.operations.StandardOptionsPanel;

import javax.swing.*;
import java.util.Random;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

/**
 * Tool that applies frost to mountain peaks on left click
 * frost - no-frost will have a smooth, random and deterministic transition
 * Right click to configure the tool
 */
public class FrostedPeaks extends MouseOrTabletOperation
{
    public FrostedPeaks() {
        super(NAME, DESCRIPTION, null, ID, "frostedPeaksIcon");
        System.out.println("icon name of frosted peaks:"+ this.getIcon());
    }

    private final StandardOptionsPanel optionsPanel = new StandardOptionsPanel(NAME, "<p>Left-Click to apply frost to all mountain peaks with a smooth transition.<br>Right-Click for configuration.<br>by Ir0nsight<p>");

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

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        if (inverse) {
            System.out.println("right click => configure");
            popup();
        } else {
            getDimension().visitTilesForEditing().andDo(tile -> {
                Random r = new Random((long) tile.getX()+tile.getY());

                for (int x = 0; x < TILE_SIZE; x++) {
                    for (int y = 0; y < TILE_SIZE; y++) {
                        if (isFrosted(x, y, tile.getIntHeight(x, y), 120, 100,r))
                            tile.setBitLayerValue(Frost.INSTANCE, x, y, true);
                    }
                }
            });
        }
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


}