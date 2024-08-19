package org.demo.wpplugin.operations;

import org.demo.wpplugin.Path;
import org.demo.wpplugin.PathManager;
import org.demo.wpplugin.layers.PathPreviewLayer;
import org.pepsoft.worldpainter.brushes.Brush;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.operations.*;
import org.pepsoft.worldpainter.painting.Paint;

import java.awt.*;
import java.util.Collections;

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
public class AddPointOperation extends MouseOrTabletOperation implements
        PaintOperation, // Implement this if you need access to the currently selected paint; note that some base classes already provide this
        BrushOperation // Implement this if you need access to the currently selected brush; note that some base classes already provide this
{
    /**
     * The globally unique ID of the operation. It's up to you what to use here. It is not visible to the user. It can
     * be a FQDN or package and class name, like here, or you could use a UUID. As long as it is globally unique.
     */
    static final String ID = "org.demo.wpplugin.BezierPathTool.v1";
    /**
     * Human-readable short name of the operation.
     */
    static final String NAME = "Bezier Path Tool";
    /**
     * Human-readable description of the operation. This is used e.g. in the tooltip of the operation selection button.
     */
    static final String DESCRIPTION = "Draw smooth, connected curves with C1 continuity.";
    final int COLOR_NONE = 0;
    final int COLOR_HANDLE = 1;
    final int COLOR_CURVE = 2;
    final int COLOR_SELECTED = 4;
    final int SIZE_SELECTED = 5;
    final int SIZE_DOT = 0;
    final int SIZE_MEDIUM_CROSS = 3;
    private Path path = new Path(Collections.emptyList());
    private Point selectedPoint;
    private Brush brush;
    private Paint paint;


    public AddPointOperation() {
        // Using this constructor will create a "single shot" operation. The tick() method below will only be invoked
        // once for every time the user clicks the mouse or presses on the tablet:
        super(NAME, DESCRIPTION, ID);
        // Using this constructor instead will create a continues operation. The tick() method will be invoked once
        // every "delay" ms while the user has the mouse button down or continues pressing on the tablet. The "first"
        // parameter will be true for the first invocation per mouse button press and false for every subsequent
        // invocation:
        // super(NAME, DESCRIPTION, delay, ID);
    }

    /**
     * Perform the operation. For single shot operations this is invoked once per mouse-down. For continuous operations
     * this is invoked once per {@code delay} ms while the mouse button is down, with the first invocation having
     * {@code first} be {@code true} and subsequent invocations having it be {@code false}.
     *
     * @param centreX      The x coordinate where the operation should be applied, in world coordinates.
     * @param centreY      The y coordinate where the operation should be applied, in world coordinates.
     * @param inverse      Whether to perform the "inverse" operation instead of the regular operation, if applicable. If the
     *                     operation has no inverse it should just apply the normal operation.
     * @param first        Whether this is the first tick of a continuous operation. For a one shot operation this will always
     *                     be {@code true}.
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
        Path previous = path;
        Point previousSelected = selectedPoint;
        Point userClickedCoord = new Point(centreX, centreY);
        if (isCtrlDown()) {
            //SELECT POINT
            try {
                if (path.amountHandles() != 0) {
                    Point closest = path.getClosestHandleTo(userClickedCoord);
                    //dont allow very far away clicks
                    if (closest.distance(userClickedCoord) < 50) {
                        selectedPoint = closest;
                    }
                }
                if (selectedPoint == previousSelected)  //user clicked selected again, unselect.
                    selectedPoint = null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (isShiftDown()) {
            if (selectedPoint != null)
                path = path.insertPointAfter(selectedPoint, userClickedCoord);
        } else {

            if (inverse) {
                try {
                    if (path.amountHandles() != 0) {
                        Point toBeRemoved = path.getClosestHandleTo(userClickedCoord);
                        path = path.removePoint(toBeRemoved);
                        if (selectedPoint == toBeRemoved)
                            selectedPoint = null;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            } else {
                Point next = new Point(centreX, centreY);
                if (selectedPoint != null) {
                    path = path.movePoint(selectedPoint, next);
                    selectedPoint = next;
                } else {
                    path = path.addPoint(next);
                }
            }
        }

        //update path
        final int PATH_ID = 1;

        this.getDimension().setEventsInhibited(true);
        //is selection was deleted or something.
        if (!path.isHandle(selectedPoint))
            selectedPoint = null;

        //erase old
        this.getDimension().clearLayerData(PathPreviewLayer.INSTANCE);

        //redraw new
        DrawPathLayer(path, false);
        if (selectedPoint != null)
            markPoint(selectedPoint, PathPreviewLayer.INSTANCE, COLOR_SELECTED, SIZE_SELECTED);

        PathManager.instance.setPathBy(PATH_ID, path);
        this.getDimension().setEventsInhibited(false);
    }

    /**
     * draws this path onto the map
     *
     * @param path
     */
    void DrawPathLayer(Path path, boolean erase) {
        PathPreviewLayer layer = PathPreviewLayer.INSTANCE;

        for (Point p : path.continousCurve()) {
            markPoint(p, layer, erase ? 0 : COLOR_CURVE, SIZE_DOT);
        }

        for (Point p : path) {
            markPoint(p, layer, erase ? 0 : COLOR_HANDLE, SIZE_MEDIUM_CROSS);
        }

        if (path.amountHandles() >= 2)
            markLine(path.handleByIndex(0), path.handleByIndex(1), layer, COLOR_HANDLE);
        if (path.amountHandles() >= 3)
            markLine(path.handleByIndex(path.amountHandles() - 1), path.handleByIndex(path.amountHandles() - 2), layer, COLOR_HANDLE);

    }

    /**
     * draws an X on the map in given color and size
     *
     * @param p
     * @param layer
     * @param color
     * @param size, 0 size = single dot on map
     */
    void markPoint(Point p, Layer layer, int color, int size) {
        for (int i = -size; i <= size; i++) {
            getDimension().setLayerValueAt(layer, p.x + i, p.y - i, color);
            getDimension().setLayerValueAt(layer, p.x + i, p.y + i, color);
        }
    }

    void markLine(Point p0, Point p1, Layer layer, int color) {
        double length = p0.distance(p1);
        for (double i = 0; i <= length; i++) {
            double factor = i / length;
            Point inter = new Point((int) (p0.x * factor + p1.x * (1 - factor)), (int) (p0.y * factor + p1.y * (1 - factor)));
            getDimension().setLayerValueAt(layer, inter.x, inter.y, color);
        }
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
}