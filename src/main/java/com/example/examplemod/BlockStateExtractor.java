package com.example.examplemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
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

		BlockModelShapes blockModelShapes = modelManager.getBlockModelShapes();
		ObjectIntIdentityMap<IBlockState> blockStateIds = Block.BLOCK_STATE_IDS;

		for (IBlockState blockState : blockStateIds) {
			int id = blockStateIds.get(blockState);
			IBlockState primary = blockStateIds.getByValue(id);
			if (primary == null) {
				LOGGER.warn("No primary blockstate found for blockstate " + blockState);
			} else {
				blockStateIdsToBlockStates.put(id, primary.toString());
			}

			IBakedModel model = blockModelShapes.getModelForState(blockState);
			BlockStateInfo info = BlockStateInfo.fromBlockState(blockState, model);
			blockStates.put(blockState.toString(), info);
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(blockStates);
		try {
			FileUtils.write(new File("blockstates.json"), json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		json = gson.toJson(blockStateIdsToBlockStates);
		try {
			FileUtils.write(new File("blockstate-ids.json"), json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		LOGGER.info("Saved blockstates.json and blockstate-ids.json");
	}
}
