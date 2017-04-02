package com.example.examplemod;

import com.example.examplemod.blocks.FluidBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = DataExtractionMod.MODID, version = DataExtractionMod.VERSION)
public class DataExtractionMod
{
    public static final String MODID = "cubeview";
    public static final String VERSION = "0.1";

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String OUTPUT_PATH = "extracted-data/";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        GameRegistry.register(FluidBlocks.LAVA, new ResourceLocation("cubeview:lava"));
        GameRegistry.register(FluidBlocks.WATER, new ResourceLocation("cubeview:water"));

        // some example code
        MinecraftForge.EVENT_BUS.register(new com.example.examplemod.EventHandler());
        LOGGER.info("Registered EventHandler");
    }
}
