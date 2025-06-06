package org.ironsight.wpplugin.macromachine.operations.ValueProviders;

import org.ironsight.wpplugin.macromachine.operations.ProviderType;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.selection.SelectionBlock;
import org.pepsoft.worldpainter.selection.SelectionChunk;

public class SelectionIO extends BinaryLayerIO {
    public SelectionIO() {
        super(SelectionBlock.INSTANCE);
    }

    @Override
    public void setValueAt(Dimension dim, int x, int y, int value) {
        dim.setBitLayerValueAt(SelectionBlock.INSTANCE, x, y, value != 0);
        if (value == 0)
            dim.setBitLayerValueAt(SelectionChunk.INSTANCE, x, y, false);
    }

    @Override
    public int getValueAt(Dimension dim, int x, int y) {
        return dim.getBitLayerValueAt(SelectionBlock.INSTANCE, x, y) ||
                dim.getBitLayerValueAt(SelectionChunk.INSTANCE, x, y) ? 1 : 0;
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public void prepareForDimension(Dimension dim) {
        //not required
    }

    @Override
    public IMappingValue instantiateFrom(Object[] data) {
        return new SelectionIO();
    }

    @Override
    public String toString() {
        return "SelectionIO{}";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.SELECTION;
    }
}
