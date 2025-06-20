package org.ironsight.wpplugin.macromachine.operations.ValueProviders;

import org.ironsight.wpplugin.macromachine.operations.LayerObjectContainer;
import org.ironsight.wpplugin.macromachine.operations.ProviderType;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.selection.SelectionBlock;

import java.awt.*;
import java.util.Objects;

public class BinaryLayerIO implements IPositionValueSetter, IPositionValueGetter, ILayerGetter {
    private final String layerName;
    private final String layerId;
    private Layer layer;

    public BinaryLayerIO(Layer layer) {
        this.layerId = layer.getId();
        this.layerName = layer.getName();
        this.layer = layer;
        assert layer.dataSize.equals(Layer.DataSize.BIT);
    }

    BinaryLayerIO(String name, String id) {
        this.layerId = id;
        this.layerName = name;
    }

    public void setValueAt(Dimension dim, int x, int y, int value) {
        assert layer != null;
        assert value == 0 || value == 1;
        dim.setBitLayerValueAt(layer, x, y, value == 1);
    }

    public int getMaxValue() {
        return 1;
    }

    public int getMinValue() {
        return 0;
    }

    @Override
    public void prepareForDimension(Dimension dim) {
        if (layer == null) {
            LayerObjectContainer.getInstance().setDimension(dim);
            layer = LayerObjectContainer.getInstance().queryLayer(layerId);
        }
        if (layer == null)
            throw new IllegalAccessError("Layer not found: " + layerName + "(" + layerId + ")");
    }

    public String valueToString(int value) {
        if (value == 0) {
            return layerName + " OFF (" + value + ")";
        } else {
            return layerName + " ON (" + value + ")";
        }
    }

    public boolean isDiscrete() {
        return true;
    }

    public void paint(Graphics g, int value, java.awt.Dimension dim) {
        if (value == 0) return;
        g.setColor(Color.red);
        g.fillRect(0, 0, dim.width, dim.height);
    }

    @Override
    public IMappingValue instantiateFrom(Object[] data) {
        return new BinaryLayerIO((String) data[0], (String) data[1]);
    }

    @Override
    public Object[] getSaveData() {
        return new Object[]{layerName, layerId};
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.BINARY_LAYER;
    }

    @Override
    public String toString() {
        return "BinaryLayerIO{" + "layerId='" + layerId + '\'' + ", layerName='" + layerName + '\'' + '}';
    }

    public int getValueAt(Dimension dim, int x, int y) {
        return dim.getBitLayerValueAt(layer, x, y) ? 1 : 0;
    }

    @Override
    public String getName() {
        return layerName;
    }

    @Override
    public String getDescription() {
        return "binary (ON or OFF) layer " + layerName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(layerId);
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryLayerIO that = (BinaryLayerIO) o;
        return Objects.equals(layerId, that.layerId);
    }

    @Override
    public String getLayerName() {
        return layerName;
    }
    @Override
    public String getToolTipText() {
        return getDescription();
    }
    @Override
    public String getLayerId() {
        return layerId;
    }
}
