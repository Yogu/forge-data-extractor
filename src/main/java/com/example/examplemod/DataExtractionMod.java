package com.example.examplemod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = DataExtractionMod.MODID, version = DataExtractionMod.VERSION)
public class DataExtractionMod
{
    public static final String MODID = "data-extraction";
    public static final String VERSION = "0.1";

    private static final Logger LOGGER = LogManager.getLogger();
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        MinecraftForge.EVENT_BUS.register(new com.example.examplemod.EventHandler());
        LOGGER.info("Registered EventHandler");
    }
}
