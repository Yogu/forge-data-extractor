package com.example.examplemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ObjectIntIdentityMap;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts a map from block state ids to block states
 */
public class BlockStateExtractor {
	private static final Logger LOGGER = LogManager.getLogger();

	public void run(ModelManager modelManager) {
		Map<Integer, String> blockStateIdsToBlockStates = new HashMap<Integer, String>();
		Map<String, BlockStateInfo> blockStates = new HashMap<String, BlockStateInfo>();

		BlockModelShapes modelProvider = modelManager.getBlockModelShapes();
		// The onModelBake() event is called a bit too early - just before the modelProvider
		// is refreshed. Need to refresh it or we will get old sprites (missingno)
		modelProvider.reloadModels();
		ObjectIntIdentityMap<IBlockState> blockStateIds = Block.BLOCK_STATE_IDS;

		for (IBlockState blockState : blockStateIds) {
			int id = blockStateIds.get(blockState);
			IBlockState primary = blockStateIds.getByValue(id);
			if (primary == null) {
				LOGGER.warn("No primary blockstate found for blockstate " + blockState);
			} else {
				blockStateIdsToBlockStates.put(id, primary.toString());
			}

			BlockStateInfo info = BlockStateInfo.fromBlockState(blockState);

			// Either put the model directly in the
			IBakedModel model = modelProvider.getModelForState(blockState);
			info = info.withModel(ModelInfo.forBakedModel(model, blockState));

			ModelResourceLocation modelResourceLocation =
					modelProvider.getBlockStateMapper().getVariants(blockState.getBlock())
							.get(blockState);
			if (modelResourceLocation == null) {
				info = info.withModelResourceLocation(modelResourceLocation);
			}

			blockStates.put(blockState.toString(), info);
		}

		Gson gson = new GsonBuilder()
				.disableHtmlEscaping()
				.create();
		Gson prettyGson = new GsonBuilder()
				.setPrettyPrinting()
				.disableHtmlEscaping()
				.create();

		try {
			String json = gson.toJson(blockStates);
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstates.json"), gson.toJson(blockStates));
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstates-pretty.json"), prettyGson.toJson(blockStates));

			json = gson.toJson(blockStateIdsToBlockStates);
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstate-ids.json"), gson.toJson(blockStates));
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstate-ids-pretty.json"), prettyGson.toJson(blockStates));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		LOGGER.info("Saved blockstates.json and blockstate-ids.json");
	}
}
