package com.gtnewhorizons.decorativehorizons;

import com.gtnewhorizons.decorativehorizons.catwalks.Catwalks;
import com.gtnewhorizons.decorativehorizons.core.wrench.DebugWrench;
import com.gtnewhorizons.decorativehorizons.materials.Materials;
import com.gtnewhorizons.decorativehorizons.rails.Rails;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Catwalks.register();
        Rails.register();
        Materials.register();
        DebugWrench.register();
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}

    public boolean wantsFullBlockOutline() {
        return false;
    }
}
