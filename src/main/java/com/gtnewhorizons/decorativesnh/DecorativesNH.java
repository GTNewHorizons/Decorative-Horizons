package com.gtnewhorizons.decorativesnh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = DecorativesNH.MODID,
    name = DecorativesNH.NAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]")
public class DecorativesNH {

    public static final String MODID = "decorativesnh";
    public static final String NAME = "Decoratives NH";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.gtnewhorizons.decorativesnh.ClientProxy",
        serverSide = "com.gtnewhorizons.decorativesnh.CommonProxy")
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
