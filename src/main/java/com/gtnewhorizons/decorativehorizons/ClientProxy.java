package com.gtnewhorizons.decorativehorizons;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;
import com.gtnewhorizons.decorativehorizons.catwalks.BlockFrame;
import com.gtnewhorizons.decorativehorizons.core.wrench.Wrench;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchGridRenderer;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // GTNHLib only loads JSON models and their textures for mods registered here
        ModelRegistry.registerModid(DecorativeHorizons.MODID);
        MinecraftForge.EVENT_BUS.register(new WrenchGridRenderer());
    }

    @Override
    public boolean wantsFullBlockOutline() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return false;
        ItemStack held = player.getHeldItem();
        return Wrench.isWrench(held)
            || (held != null && held.getItem() instanceof ItemBlock item && item.field_150939_a instanceof BlockFrame);
    }
}
