package com.gtnewhorizons.decorativehorizons.core.wrench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;

import com.gtnewhorizons.decorativehorizons.DecorativeHorizons;

/** A wrench for testing without GT. The wrench is registered only in dev env. */
public final class DebugWrench {

    public static Item item;

    private DebugWrench() {}

    public static void register() {
        if (!Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment"))) return;
        item = DecorativeHorizons.register(new Item() {

            @Override
            public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
                return true;
            }
        }.setMaxStackSize(1), "debug_wrench", "debug_wrench");
    }
}
