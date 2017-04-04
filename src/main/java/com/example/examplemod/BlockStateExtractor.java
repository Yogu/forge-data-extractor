package com.example.examplemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.BlockFluidBase;
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
		ObjectIntIdentityMap<IBlockState> blockStateIds = Block.BLOCK_STATE_IDS;

		for (IBlockState blockState : blockStateIds) {
			int id = blockStateIds.get(blockState);
			IBlockState primary = blockStateIds.getByValue(id);
			if (primary == null) {
				LOGGER.warn("No primary blockstate found for blockstate " + blockState);
			} else {
				blockStateIdsToBlockStates.put(id, primary.toString());
			}

			// put in all blockstates that have a numeric representation, even if they do not specify all properties
			blockStates.put(blockState.toString(), getBlockStateInfo(modelProvider, blockState));

			// make sure we get all blockstates with all properties even without numeric representation
			// but these three generate huge amounts of blockstates, so leave them out until we really need them
			if (!(blockState.getBlock() instanceof BlockRedstoneWire) && !(blockState.getBlock() instanceof BlockFire) && !(blockState.getBlock() instanceof BlockFlowerPot)) {
				for (IBlockState derived : blockState.getBlock().getBlockState().getValidStates()) {
					blockStates.put(derived.toString(), getBlockStateInfo(modelProvider, derived));
				}
			}
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

	private BlockStateInfo getBlockStateInfo(BlockModelShapes modelProvider, IBlockState blockState) {
		BlockStateInfo info = BlockStateInfo.fromBlockState(blockState);

		IBlockState blockStateForModel;
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

		IBakedModel model = modelProvider.getModelForState(blockStateForModel);
		IBlockState extendedBlockState = blockState.getBlock()
				.getExtendedState(blockStateForModel,
						new FakeBlockAccess(blockState, new BlockPos(0, 0, 0)),
						new BlockPos(0, 0, 0));
		info = info.withModel(ModelInfo.forBakedModel(model, extendedBlockState));

		ModelResourceLocation modelResourceLocation =
				modelProvider.getBlockStateMapper().getVariants(blockState.getBlock())
						.get(blockState);
		if (modelResourceLocation == null) {
			info = info.withModelResourceLocation(modelResourceLocation);
		}

		return info;
	}

	private IBlockState getLiquidBlockStateForModel(IBlockState blockState, Block fluidBlock) {
		IBlockState blockStateForModel = fluidBlock.getDefaultState();
		int liquidLevel = blockState.getValue(BlockLiquid.LEVEL);
		int fluidLevel;
		IBlockAccess world = new FakeBlockAccess(blockState, new BlockPos(0, 0, 0));
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
	}

	private class FakeBlockAccess implements IBlockAccess {
		protected IBlockState blockState;
		protected BlockPos blockPos;

		public FakeBlockAccess(IBlockState blockState, BlockPos blockPos) {
			this.blockState = blockState;
			this.blockPos = blockPos;
		}

		@Nullable @Override public TileEntity getTileEntity(BlockPos pos) {
			return null;
		}

		@Override public int getCombinedLight(BlockPos pos, int lightValue) {
			return 0;
		}

		@Override public IBlockState getBlockState(BlockPos pos) {
			if (pos.equals(this.blockPos)) {
				return this.blockState;
			}
			return Blocks.AIR.getDefaultState();
		}

		@Override public boolean isAirBlock(BlockPos pos) {
			return !pos.equals(this.blockPos);
		}

		@Override public Biome getBiome(BlockPos pos) {
			return Biome.getBiome(1);
		}

		@Override public int getStrongPower(BlockPos pos, EnumFacing direction) {
			return 0;
		}

		@Override public WorldType getWorldType() {
			return WorldType.DEFAULT;
		}

		@Override public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
			return false;
		}
	}

	private class FakeBlockAccessWithWaterOnTop extends FakeBlockAccess {
		public FakeBlockAccessWithWaterOnTop(IBlockState blockState,
				BlockPos blockPos) {
			super(blockState, blockPos);
		}

		@Override public IBlockState getBlockState(BlockPos pos) {
			if (pos.equals(this.blockPos.up())) {
				return Blocks.WATER.getDefaultState();
			}
			return super.getBlockState(pos);
		}
	}
}
