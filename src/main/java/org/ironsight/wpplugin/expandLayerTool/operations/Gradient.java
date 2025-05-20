package org.ironsight.wpplugin.expandLayerTool.operations;

import java.util.Arrays;

public class Gradient {
    public final float[] positions;
    public final float[] values;

    public Gradient(float[] positions, float[] values) {
        assert positions.length == values.length;
        this.positions = positions;
        this.values = values;
    }

    public float getValue(float x) {
        if (values.length == 0) {
            return 0;
        }
        int idx = Arrays.binarySearch(positions, x);
        if (idx < 0) {
            idx = -idx - 1;
        }
        if (idx == 0) {
            return values[idx];
        }
        if (idx >= positions.length - 1) {
            return values[positions.length - 1];
        }
        return values[idx];
    }

}
