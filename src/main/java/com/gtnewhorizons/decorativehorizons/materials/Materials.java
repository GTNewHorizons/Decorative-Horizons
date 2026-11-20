package com.gtnewhorizons.decorativehorizons.materials;

import net.minecraft.item.Item;

import com.gtnewhorizons.decorativehorizons.DecorativeHorizons;

public final class Materials {

    public static final Item IRON_GRATE = new Item();

    private Materials() {}

    public static void register() {
        DecorativeHorizons.register(IRON_GRATE, "iron_grate", "materials/iron_grate");
    }
}
