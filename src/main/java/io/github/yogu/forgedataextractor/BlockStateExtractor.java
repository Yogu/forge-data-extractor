package io.github.yogu.forgedataextractor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.*;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
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
		ObjectIntIdentityMap<BlockState> blockStateIds = Block.BLOCK_STATE_IDS;

		for (BlockState blockState : blockStateIds) {
			int id = blockStateIds.get(blockState);
			BlockState primary = blockStateIds.getByValue(id);
			if (primary == null) {
				LOGGER.warn("No primary blockstate found for blockstate " + blockState);
			} else {
				BlockStateInfo primaryInfo = getBlockStateInfo(modelProvider, primary);
				blockStateIdsToBlockStates.put(id, primaryInfo.getQualifiedName());
			}

			// put in all blockstates that have a numeric representation, even if they do not specify all properties
			BlockStateInfo blockStateInfo = getBlockStateInfo(modelProvider, blockState);
			blockStates.put(blockStateInfo.getQualifiedName(), getBlockStateInfo(modelProvider, blockState));

			// make sure we get all blockstates with all properties even without numeric representation
			// but these three generate huge amounts of blockstates, so leave them out until we really need them
			/*if (!(blockState.getBlock() instanceof RedstoneWireBlock) && !(blockState.getBlock() instanceof FireBlock) && !(blockState.getBlock() instanceof FlowerPotBlock)) {
				BlockStateInfo blockStateInfo = getBlockStateInfo(modelProvider, blockState);
				blockStates.put(blockStateInfo.getQualifiedName(), getBlockStateInfo(modelProvider, blockState));
			}*/
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
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstates.json"),
					gson.toJson(blockStates));
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstates-pretty.json"),
					prettyGson.toJson(blockStates));

			json = gson.toJson(blockStateIdsToBlockStates);
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstate-ids.json"),
					gson.toJson(blockStateIdsToBlockStates));
			FileUtils.write(new File(DataExtractionMod.OUTPUT_PATH + "blockstate-ids-pretty.json"),
					prettyGson.toJson(blockStateIdsToBlockStates));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		LOGGER.info("Saved blockstates.json and blockstate-ids.json");
	}

	private BlockStateInfo getBlockStateInfo(BlockModelShapes modelProvider, BlockState blockState) {
		BlockStateInfo info = BlockStateInfo.fromBlockState(blockState);

		BlockState blockStateForModel;
		String blockName = blockState.getBlock().getRegistryName().toString();
			/*if (blockName.equals("minecraft:water") || blockName.equals("minecraft:flowing_water")) {
				blockStateForModel = getLiquidBlockStateForModel(blockState, FluidBlocks.WATER);
				//info = info.withRenderType(EnumBlockRenderType.MODEL);
			} else if (blockName.equals("minecraft:lava") || blockName.equals("minecraft:flowing_lava")) {
				blockStateForModel = getLiquidBlockStateForModel(blockState, FluidBlocks.LAVA);
				//info = info.withRenderType(EnumBlockRenderType.MODEL);
			} else*/
		{
			blockStateForModel = blockState;
		}

		IBakedModel model = modelProvider.getModel(blockStateForModel);
		BlockState extendedBlockState = blockState.getBlock()
				.getExtendedState(blockStateForModel,
						new FakeBlockAccess(blockState, new BlockPos(0, 0, 0)),
						new BlockPos(0, 0, 0));
		info = info.withModel(ModelInfo.forBakedModel(model, extendedBlockState));

		ModelResourceLocation modelResourceLocation = BlockModelShapes.getModelLocation(blockState);
		if (modelResourceLocation != null) {
			info = info.withModelResourceLocation(modelResourceLocation);
		}

		return info;
	}

	/*private BlockState getLiquidBlockStateForModel(BlockState blockState, Block fluidBlock) {
		BlockState blockStateForModel = fluidBlock.getDefaultState();
		int liquidLevel = blockState.getValue(BlockLiquid.LEVEL);
		int fluidLevel;
		IBlockReader world = new FakeBlockAccess(blockState, new BlockPos(0, 0, 0));
		if (liquidLevel == 0) {
			fluidLevel = 15;
		} else if (liquidLevel > 8) {
			fluidLevel = 15;
			world = new FakeBlockAccessWithWaterOnTop(blockState, new BlockPos(0, 0, 0));
		} else {
			fluidLevel = (int)((double)liquidLevel / 8 * 15);
		}
		blockStateForModel.withProperty(BlockFluidBase.LEVEL, fluidLevel);
		return fluidBlock.getExtendedState(blockStateForModel, world, new BlockPos(0, 0, 0));
	}*/

	private class FakeBlockAccess implements IBlockReader {
		protected BlockState blockState;
		protected BlockPos blockPos;

		public FakeBlockAccess(BlockState blockState, BlockPos blockPos) {
			this.blockState = blockState;
			this.blockPos = blockPos;
		}

		@Nullable @Override public TileEntity getTileEntity(BlockPos pos) {
			return null;
		}


		@Override public BlockState getBlockState(BlockPos pos) {
			if (pos.equals(this.blockPos)) {
				return this.blockState;
			}
			return Blocks.AIR.getDefaultState();
		}

		@Override public IFluidState getFluidState(BlockPos pos) {
			return Fluids.EMPTY.getDefaultState();
		}

	}

	private class FakeBlockAccessWithWaterOnTop extends FakeBlockAccess {
		public FakeBlockAccessWithWaterOnTop(BlockState blockState,
				BlockPos blockPos) {
			super(blockState, blockPos);
		}

		@Override public BlockState getBlockState(BlockPos pos) {
			if (pos.equals(this.blockPos.up())) {
				return Blocks.WATER.getDefaultState();
			}
			return super.getBlockState(pos);
		}
	}
}
