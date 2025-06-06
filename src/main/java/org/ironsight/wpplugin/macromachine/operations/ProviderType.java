package org.ironsight.wpplugin.macromachine.operations;

import org.ironsight.wpplugin.macromachine.operations.ValueProviders.*;
import org.pepsoft.worldpainter.layers.Frost;
import org.pepsoft.worldpainter.layers.PineForest;

public enum ProviderType {
    HEIGHT,
    SLOPE,
    ANNOTATION,
    BINARY_LAYER,
    BINARY_SPRAYPAINT,
    BLOCK_DIRECTION,
    INTERMEDIATE,
    INTERMEDIATE_SELECTION,
    NIBBLE_LAYER,
    SELECTION,
    STONE_PALETTE,
    TERRAIN,
    TEST,
    VANILLA_BIOME,
    WATER_DEPTH,
    WATER_HEIGHT,
    ALWAYS,
    DISTANCE_TO_EDGE,
    ;

    public static IMappingValue fromType(Object[] data, ProviderType type) {
        return fromTypeDefault(type).instantiateFrom(data);
    }

    public static IMappingValue fromTypeDefault(ProviderType type) {
        switch (type) {
            case TEST:
                return new TestInputOutput();
            case SLOPE:
                return new SlopeProvider();
            case HEIGHT:
                return new HeightProvider();
            case TERRAIN:
                return new TerrainProvider();
            case WATER_DEPTH:
                return new WaterDepthProvider();
            case INTERMEDIATE:
                return new IntermediateValueIO();
            case STONE_PALETTE:
                return new StonePaletteApplicator();
            case VANILLA_BIOME:
                return new VanillaBiomeProvider();
            case BLOCK_DIRECTION:
                return new BlockFacingDirectionIO();
            case SELECTION:
                return new SelectionIO();
            case ANNOTATION:
                return new AnnotationSetter();
            case WATER_HEIGHT:
                return new WaterHeightAbsoluteIO();

            case BINARY_SPRAYPAINT:
                return new BitLayerBinarySpraypaintApplicator(Frost.INSTANCE);
            case BINARY_LAYER:
                return new BinaryLayerIO(Frost.INSTANCE);
            case NIBBLE_LAYER:
                return new NibbleLayerSetter(PineForest.INSTANCE);
            case INTERMEDIATE_SELECTION:
                return IntermediateSelectionIO.instance;
            case ALWAYS:
                return AlwaysIO.instance;
            case DISTANCE_TO_EDGE:
                return new DistanceToLayerEdgeGetter(PineForest.INSTANCE);
            default:
                throw new IllegalArgumentException(
                        "not implemented: can not instantiate providers that need extra " + "information");

        }
    }
}
