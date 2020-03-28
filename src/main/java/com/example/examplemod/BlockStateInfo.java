package com.example.examplemod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Static information about a {@link BlockState}
 */
public class BlockStateInfo implements  Cloneable {
	private static final Logger LOGGER = LogManager.getLogger();

	private String qualifiedName;
	private float ambientOcclusionLightValue;
	private BlockRenderType renderType;
	//private BlockRenderLayer renderLayer;
	private int lightOpacity;
	private int lightValue;
	private boolean isFullCube;
	private boolean isFullyOpaque;
	private boolean isFullBlock;
	private boolean isBlockNormalCube;
	private boolean isOpaqueCube;
	private boolean isTranslucent;
	private Map<Direction, Boolean> isSideSolid = new HashMap<Direction, Boolean>();
	private AxisAlignedBB collisionBoundingBox;
	private boolean isSolid;
	private boolean isLiquid;
	private boolean blocksMovement;
	private boolean useNeighborBrightness;

	private ModelInfo model;
	private ModelResourceLocation modelResourceLocation;

	public static BlockStateInfo fromBlockState(BlockState blockState) {
		BlockStateInfo info = new BlockStateInfo();
		info.qualifiedName = blockState.toString();
		info.renderType = blockState.getRenderType();
		//info.renderLayer = blockState.getBlock().getBlockLayer();
		//info.ambientOcclusionLightValue = blockState.getAmbientOcclusionLightValue();
		//info.lightOpacity = blockState.getLightOpacity();
		info.lightValue = blockState.getLightValue();
		//info.isFullCube = blockState.isFullCube();
		//info.isFullBlock = blockState.isFullBlock();
		//info.isFullyOpaque = blockState.isFullyOpaque();
		//info.isBlockNormalCube = blockState.isBlockNormalCube();
		//info.isOpaqueCube = blockState.isOpaqueCube();
		//info.isTranslucent = blockState.isTranslucent();
		info.isSolid = blockState.getMaterial().isSolid();
		info.isLiquid = blockState.getMaterial().isLiquid();
		info.blocksMovement = blockState.getMaterial().blocksMovement();
		//info.useNeighborBrightness = blockState.useNeighborBrightness();

		for (Direction side : Direction.values()) {
			try {
				info.isSideSolid.put(side, !Block.shouldSideBeRendered(Blocks.STONE.getDefaultState(), new FakeBlockAccess(blockState, new BlockPos(0, 0, 0).offset(side)), new BlockPos(0, 0, 0), side));
			} catch (NullPointerException e) {
				LOGGER.debug("Failed to retrieve isSideSolid." + side + " for " + blockState);
				// method tried to access world or pos, so it depends on more than just the block state
				info.isSideSolid.put(side, null);
			}
		}

		try {
			VoxelShape shape = blockState.getCollisionShape(null, null);
			if (!shape.isEmpty()) {
				info.collisionBoundingBox = shape.getBoundingBox();
			}
		} catch (NullPointerException e) {
			LOGGER.debug("Failed to retrieve collisionBoundingBox for " + blockState);
			// fail safe
			info.collisionBoundingBox = null;
		}

		return info;
	}

	public BlockStateInfo clone() {
		try {
			return (BlockStateInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public BlockStateInfo withModel(ModelInfo model) {
		BlockStateInfo clone = this.clone();
		clone.model = model;
		return clone;
	}

	public BlockStateInfo withRenderType(BlockRenderType renderType) {
		BlockStateInfo clone = this.clone();
		clone.renderType = renderType;
		return clone;
	}

	public BlockStateInfo withModelResourceLocation(ModelResourceLocation modelResourceLocation) {
		BlockStateInfo clone = this.clone();
		clone.modelResourceLocation = modelResourceLocation;
		return clone;
	}


	private static class FakeBlockAccess implements IBlockReader {
		protected BlockState blockState;
		protected BlockPos blockPos;

		public FakeBlockAccess(BlockState blockState, BlockPos blockPos) {
			this.blockState = blockState;
			this.blockPos = blockPos;
		}

		@Nullable
		@Override public TileEntity getTileEntity(BlockPos pos) {
			return null;
		}


		@Override public BlockState getBlockState(BlockPos pos) {
			if (pos.equals(this.blockPos)) {
				return this.blockState;
			}
			return Blocks.STONE.getDefaultState();
		}

		@Override public IFluidState getFluidState(BlockPos pos) {
			return Fluids.EMPTY.getDefaultState();
		}

	}

}
