package com.gtnewhorizons.decorativehorizons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = DecorativeHorizons.MODID,
    name = DecorativeHorizons.NAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]")
public class DecorativeHorizons {

    public static final String MODID = "decorativehorizons";
    public static final String NAME = "Decorative Horizons";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.gtnewhorizons.decorativehorizons.ClientProxy",
        serverSide = "com.gtnewhorizons.decorativehorizons.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
