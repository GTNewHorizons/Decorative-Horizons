package com.gtnewhorizons.decorativehorizons.core;

/** A block with variants in the metadata. Each item damage value is one variant. */
public interface IVariantBlock {

    /** Damage values to list, in creative tab order. */
    int[] variants();

    /** Item name for a damage value, without the {@code tile.<modid>.} prefix. */
    String variantName(int damage);

    int variantMeta(int damage);
}
