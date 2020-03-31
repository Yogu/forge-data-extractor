package io.github.yogu.forgedataextractor;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("forgedataextractor")
public class DataExtractionMod
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String OUTPUT_PATH = "extracted-data/";

    public DataExtractionMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModelBake);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onTextureStitch);
    }

    public void onModelBake(ModelBakeEvent event) {
        LOGGER.debug("onModelBake event fired");
        new BlockStateExtractor().run(event.getModelManager());
    }

    public void onTextureStitch(TextureStitchEvent event) {
        LOGGER.debug("onTextureStitch event fired");
        try {
            new TextureMapExtractor().run(event.getMap());
        } catch (Exception e) {
            LOGGER.warn("Error extracting texture", e);
        }
    }
}
