package com.example.examplemod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static information of a {@link IBakedModel}
 */
public class ModelInfo {
	private boolean isAmbientOcclusion;
	private Map<EnumFacing, List<BakedQuad>> sideQuads = new HashMap<EnumFacing, List<BakedQuad>>();
	private List<BakedQuad> generalQuads = new ArrayList<BakedQuad>();

	public static ModelInfo forBakedModel(IBakedModel model, IBlockState blockState) {
		ModelInfo info = new ModelInfo();
		info.isAmbientOcclusion = model.isAmbientOcclusion();
		for (EnumFacing side : EnumFacing.values()) {
			// We ignore both ExtendedStates and randomization for now
			info.sideQuads.put(side, model.getQuads(blockState, side, 0));
		}
		info.generalQuads = model.getQuads(blockState, null, 0);
		return info;
	}
}
