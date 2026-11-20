package com.gtnewhorizons.decorativehorizons.core;

import net.minecraft.block.Block;

/** A block that forms straight lines for {@link LineBuilder}. */
public interface ILineBlock {

    default boolean continuesLine(Block block, int meta, int lineMeta) {
        return block == this;
    }
}
