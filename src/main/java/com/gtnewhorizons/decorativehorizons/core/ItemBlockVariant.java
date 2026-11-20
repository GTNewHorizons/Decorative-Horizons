package com.gtnewhorizons.decorativehorizons.core;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.gtnewhorizons.decorativehorizons.DecorativeHorizons;

/** Item for an {@link IVariantBlock}, with one damage value and one name for each variant. */
public class ItemBlockVariant extends ItemBlock {

    public ItemBlockVariant(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return block().variantMeta(damage);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "tile." + DecorativeHorizons.MODID + "." + block().variantName(stack.getItemDamage());
    }

    protected IVariantBlock block() {
        return (IVariantBlock) field_150939_a;
    }
}
