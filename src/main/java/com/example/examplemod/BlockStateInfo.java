package com.example.examplemod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Static information about a {@link IBlockState}
 */
public class BlockStateInfo implements  Cloneable {
	private static final Logger LOGGER = LogManager.getLogger();

	private String qualifiedName;
	private float ambientOcclusionLightValue;
	private EnumBlockRenderType renderType;
	private int lightOpacity;
	private int lightValue;
	private boolean isFullCube;
	private boolean isFullyOpaque;
	private boolean isFullBlock;
	private boolean isBlockNormalCube;
	private boolean isOpaqueCube;
	private boolean isTranslucent;
	private Map<EnumFacing, Boolean> isSideSolid = new HashMap<EnumFacing, Boolean>();
	private AxisAlignedBB collisionBoundingBox;

	private ModelInfo model;
	private ModelResourceLocation modelResourceLocation;

	public static BlockStateInfo fromBlockState(IBlockState blockState) {
		BlockStateInfo info = new BlockStateInfo();
		info.qualifiedName = blockState.toString();
		info.renderType = blockState.getRenderType();
		info.ambientOcclusionLightValue = blockState.getAmbientOcclusionLightValue();
		info.lightOpacity = blockState.getLightOpacity();
		info.lightValue = blockState.getLightValue();
		info.isFullCube = blockState.isFullCube();
		info.isFullBlock = blockState.isFullBlock();
		info.isFullyOpaque = blockState.isFullyOpaque();
		info.isBlockNormalCube = blockState.isBlockNormalCube();
		info.isOpaqueCube = blockState.isOpaqueCube();
		info.isTranslucent = blockState.isTranslucent();

		for (EnumFacing side : EnumFacing.values()) {
			try {
				info.isSideSolid.put(side, blockState.isSideSolid(null, null, side));
			} catch (NullPointerException e) {
				LOGGER.info("Failed to retrieve isSideSolid." + side + " for " + blockState);
				// method tried to access world or pos, so it depends on more than just the block state
				info.isSideSolid.put(side, null);
			}
		}

		try {
			info.collisionBoundingBox = blockState.getCollisionBoundingBox(null, null);
		} catch (NullPointerException e) {
			LOGGER.info("Failed to retrieve collisionBoundingBox for " + blockState);
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

	public BlockStateInfo withModelResourceLocation(ModelResourceLocation modelResourceLocation) {
		BlockStateInfo clone = this.clone();
		clone.modelResourceLocation = modelResourceLocation;
		return clone;
	}
}
