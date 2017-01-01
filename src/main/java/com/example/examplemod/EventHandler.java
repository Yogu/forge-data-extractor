package com.example.examplemod;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		LOGGER.debug("onModelBake event fired");
		new BlockStateExtractor().run(event.getModelManager());
	}

	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent event) {
		new TextureMapExtractor().run(event.getMap());
	}
}
