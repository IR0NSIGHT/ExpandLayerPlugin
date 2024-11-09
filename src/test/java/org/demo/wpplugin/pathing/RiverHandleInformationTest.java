package org.demo.wpplugin.pathing;

import org.demo.wpplugin.geometry.PaintDimension;
import org.demo.wpplugin.operations.ContinuousCurve;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.demo.wpplugin.geometry.HeightDimension.getImmutableDimension62;
import static org.demo.wpplugin.operations.River.RiverHandleInformation.*;
import static org.demo.wpplugin.operations.River.RiverHandleInformation.RiverInformation.RIVER_RADIUS;
import static org.demo.wpplugin.pathing.PointInterpreter.PointType.RIVER_2D;
import static org.demo.wpplugin.pathing.PointUtils.setPosition2D;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RiverHandleInformationTest {
    @Test
    public void drawRiverPath() {
        ArrayList<float[]> handles = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            float[] handle = new float[RIVER_2D.size];
            handle = setPosition2D(handle, new float[]{i, 2 * i});
            handle = setValue(handle, RIVER_RADIUS, i == 0 ? 3 : INHERIT_VALUE);
            handles.add(handle);
        }
        Path p = new Path(handles, RIVER_2D);

        p = p.setHandleByIdx(setValue(p.handleByIndex(0), RIVER_RADIUS, 15), 0);
        p = p.setHandleByIdx(setValue(p.handleByIndex(p.amountHandles() - 1), RIVER_RADIUS, 15), p.amountHandles() - 1);


        PaintDimension dim = new PaintDimension() {
            @Override
            public int getValue(int x, int y) {
                return 0;
            }

            @Override
            public void setValue(int x, int y, int v) {

            }
        };
        Path clone = p.clone();
        try {
            DrawRiverPath(p, ContinuousCurve.fromPath(p, getImmutableDimension62()), dim);
        } catch (IllegalAccessException ignored) {
        }
        assertEquals(clone, p, "path was mutated by drawing it");
    }
}