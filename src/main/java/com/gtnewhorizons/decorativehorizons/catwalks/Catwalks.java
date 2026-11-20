package com.gtnewhorizons.decorativehorizons.catwalks;

import java.util.Arrays;

import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.blockstate.properties.BooleanBlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.properties.DirectionBlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.properties.IntegerBlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.gtnewhorizons.decorativehorizons.DecorativeHorizons;
import com.gtnewhorizons.decorativehorizons.core.ItemBlockVariant;
import com.gtnewhorizons.decorativehorizons.core.WorldProperty;

/** The catwalk blocks and the blockstate properties of their models. */
public final class Catwalks {

    public static final BlockCatwalk CATWALK = new BlockCatwalk();
    public static final BlockCagedLadder CAGED_LADDER = new BlockCagedLadder();
    public static final BlockScaffold SCAFFOLD = new BlockScaffold();
    public static final BlockSupportColumn SUPPORT_COLUMN = new BlockSupportColumn();

    private Catwalks() {}

    public static void register() {
        DecorativeHorizons.register(CATWALK, "catwalk", ItemBlockVariant.class);
        DecorativeHorizons.register(CAGED_LADDER, "caged_ladder", ItemBlockVariant.class);
        DecorativeHorizons.register(SCAFFOLD, "scaffold", ItemBlockLine.class);
        DecorativeHorizons.register(SUPPORT_COLUMN, "support_column", ItemBlockLine.class);

        registerCatwalkProperties();
        registerLadderProperties();
        registerScaffoldProperties();
        registerColumnProperties();
    }

    /** The item model has the default values: a floor and a railing on each side. */
    private static void registerCatwalkProperties() {
        BlockPropertyRegistry.registerBlockItemProperty(CATWALK, createFloorProperty(BlockCatwalk.NO_FLOOR));
        BlockPropertyRegistry.registerBlockItemProperty(CATWALK, BooleanBlockProperty.flag("lit", BlockCatwalk.LIT));
        BlockPropertyRegistry
            .registerBlockItemProperty(CATWALK, BooleanBlockProperty.flag("caution", BlockCatwalk.CAUTION));
        for (ForgeDirection side : BlockCatwalk.SIDES) {
            BlockPropertyRegistry.registerBlockItemProperty(
                CATWALK,
                new WorldProperty(
                    formatName(side),
                    (world, x, y, z) -> BlockCatwalk.side(world, x, y, z, side),
                    (world, x, y, z, value) -> {
                        if (value.equals("railing")) BlockCatwalk.closeRailing(world, x, y, z, side);
                        else BlockCatwalk.openRailing(world, x, y, z, side);
                    }),
                "railing");
        }
        for (BlockCatwalk.Corner corner : BlockCatwalk.Corner.values()) {
            BlockPropertyRegistry.registerBlockItemProperty(
                CATWALK,
                new WorldProperty(
                    corner.name()
                        .toLowerCase(),
                    (world, x, y, z) -> BlockCatwalk.corner(world, x, y, z, corner)),
                "none");
        }
    }

    /** The item model has the default values: facing north, with a floor and four walls. */
    private static void registerLadderProperties() {
        BlockPropertyRegistry.registerBlockItemProperty(
            CAGED_LADDER,
            new DirectionBlockProperty.AbstractDirectionBlockProperty("facing") {

                @Override
                public int getMeta(ForgeDirection value, int existing) {
                    return BlockCagedLadder.withFacing(existing, value);
                }

                @Override
                public ForgeDirection getValue(int meta) {
                    return BlockCagedLadder.getBack(meta);
                }

                @Override
                public boolean isValidDirection(ForgeDirection value) {
                    return value.offsetY == 0 && value != ForgeDirection.UNKNOWN;
                }
            },
            ForgeDirection.NORTH);
        BlockPropertyRegistry.registerBlockItemProperty(CAGED_LADDER, createFloorProperty(BlockCagedLadder.FLOOR_OPEN));
        BlockPropertyRegistry
            .registerBlockItemProperty(CAGED_LADDER, BooleanBlockProperty.flag("caution", BlockCagedLadder.CAUTION));
        for (ForgeDirection side : BlockCatwalk.SIDES) {
            BlockPropertyRegistry.registerBlockItemProperty(
                CAGED_LADDER,
                new WorldProperty(
                    formatName(side),
                    (world, x, y, z) -> BlockCagedLadder.side(world, x, y, z, side),
                    (world, x, y, z, value) -> {
                        if (value.equals("open")) BlockCagedLadder.openWall(world, x, y, z, side);
                        else BlockCagedLadder.closeWall(world, x, y, z, side);
                    }),
                "wall");
        }
    }

    private static void registerScaffoldProperties() {
        BlockPropertyRegistry.registerBlockItemProperty(
            SCAFFOLD,
            IntegerBlockProperty.meta("style", BlockScaffold.STYLE, 0)
                .map(Arrays.asList(BlockScaffold.STYLES)));
        BlockPropertyRegistry
            .registerBlockItemProperty(SCAFFOLD, BooleanBlockProperty.flag("caution", BlockScaffold.CAUTION));
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            BlockPropertyRegistry.registerBlockItemProperty(
                SCAFFOLD,
                new WorldProperty(formatName(side), (world, x, y, z) -> BlockScaffold.side(world, x, y, z, side)),
                "none");
        }
    }

    private static void registerColumnProperties() {
        BlockPropertyRegistry.registerBlockItemProperty(
            SUPPORT_COLUMN,
            BooleanBlockProperty.flag("caution", BlockSupportColumn.CAUTION));
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            // the item renders as a vertical piece
            BlockPropertyRegistry.registerBlockItemProperty(
                SUPPORT_COLUMN,
                new WorldProperty(
                    formatName(side),
                    (world, x, y, z) -> BlockSupportColumn.side(world, x, y, z, side),
                    (world, x, y, z, value) -> {
                        if (value.equals("none")) BlockSupportColumn.disconnect(world, x, y, z, side);
                        else BlockSupportColumn.connect(world, x, y, z, side);
                    }),
                side.offsetY != 0 ? "end" : "none");
        }
    }

    // the block stores "no floor". Invert the flag, so an item, at damage 0, has a floor
    private static BooleanBlockProperty createFloorProperty(int noFloor) {
        return new BooleanBlockProperty.FlagBooleanBlockProperty("floor", noFloor) {

            @Override
            public int getMetaPrimitive(boolean value, int existing) {
                return super.getMetaPrimitive(!value, existing);
            }

            @Override
            public boolean getValuePrimitive(int meta) {
                return !super.getValuePrimitive(meta);
            }
        };
    }

    private static String formatName(ForgeDirection side) {
        return side.name()
            .toLowerCase();
    }
}
