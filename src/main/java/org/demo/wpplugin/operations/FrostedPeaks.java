package org.demo.wpplugin.operations;

import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.WorldPainterDialog;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.brushes.Brush;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.operations.*;
import org.pepsoft.worldpainter.painting.Paint;

import java.util.Random;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

/**
 * For any operation that is intended to be applied to the dimension in a particular location as indicated by the user
 * by clicking or dragging with a mouse or pressing down on a tablet, it makes sense to subclass
 * {@link MouseOrTabletOperation}, which automatically sets that up for you.
 *
 * <p>For more general kinds of operations you are free to subclass {@link AbstractOperation} instead, or even just
 * implement {@link Operation} directly.
 *
 * <p>There are also more specific base classes you can use:
 *
 * <ul>
 *     <li>{@link AbstractBrushOperation} - for operations that need access to the currently selected brush and
 *     intensity setting.
 *     <li>{@link RadiusOperation} - for operations that perform an action in the shape of the brush.
 *     <li>{@link AbstractPaintOperation} - for operations that apply the currently selected paint in the shape of the
 *     brush.
 * </ul>
 *
 * <p><strong>Note</strong> that for now WorldPainter only supports operations that
 */
public class FrostedPeaks extends MouseOrTabletOperation implements
        PaintOperation, // Implement this if you need access to the currently selected paint; note that some base classes already provide this
        BrushOperation // Implement this if you need access to the currently selected brush; note that some base classes already provide this
{
    public FrostedPeaks() {

        //super(NAME, DESCRIPTION, ID);
        super(NAME, DESCRIPTION, (WorldPainterView)null, ID, "frostedPeaksIcon");
        System.out.println("icon name of frosted peaks:"+ this.getIcon());
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

    /**
     * Perform the operation. For single shot operations this is invoked once per mouse-down. For continuous operations
     * this is invoked once per {@code delay} ms while the mouse button is down, with the first invocation having
     * {@code first} be {@code true} and subsequent invocations having it be {@code false}.
     *
     * @param centreX The x coordinate where the operation should be applied, in world coordinates.
     * @param centreY The y coordinate where the operation should be applied, in world coordinates.
     * @param inverse Whether to perform the "inverse" operation instead of the regular operation, if applicable. If the
     *                operation has no inverse it should just apply the normal operation.
     * @param first Whether this is the first tick of a continuous operation. For a one shot operation this will always
     *              be {@code true}.
     * @param dynamicLevel The dynamic level (from 0.0f to 1.0f inclusive) to apply in addition to the {@code level}
     *                     property, for instance due to a pressure sensitive stylus being used. In other words,
     *                     <strong>not</strong> the total level at which to apply the operation! Operations are free to
     *                     ignore this if it is not applicable. If the operation is being applied through a means which
     *                     doesn't provide a dynamic level (for instance the mouse), this will be <em>exactly</em>
     *                     {@code 1.0f}.
     */
    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        //  Perform the operation. In addition to the parameters you have the following methods available:
        // * getDimension() - obtain the dimension on which to perform the operation
        // * getLevel() - obtain the current brush intensity setting as a float between 0.0 and 1.0
        // * isAltDown() - whether the Alt key is currently pressed - NOTE: this is already in use to indicate whether
        //                 the operation should be inverted, so should probably not be overloaded
        // * isCtrlDown() - whether any of the Ctrl, Windows or Command keys are currently pressed
        // * isShiftDown() - whether the Shift key is currently pressed
        // In addition you have the following fields in this class:
        // * brush - the currently selected brush
        // * paint - the currently selected paint

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

    @Override
    public Brush getBrush() {
        return brush;
    }

    @Override
    public void setBrush(Brush brush) {
        this.brush = brush;
    }

    @Override
    public Paint getPaint() {
        return paint;
    }

    @Override
    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    private Brush brush;
    private Paint paint;

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