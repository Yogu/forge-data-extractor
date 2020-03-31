package io.github.yogu.forgedataextractor;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;

import java.util.*;

/**
 * Static information of a {@link IBakedModel}
 */
public class ModelInfo {
	private boolean isAmbientOcclusion;
	private Map<Direction, List<QuadInfo>> sideQuads = new HashMap<Direction, List<QuadInfo>>();
	private List<QuadInfo> generalQuads = new ArrayList<QuadInfo>();

	public static ModelInfo forBakedModel(IBakedModel model, BlockState blockState) {
		ModelInfo info = new ModelInfo();
		info.isAmbientOcclusion = model.isAmbientOcclusion();
		for (Direction side : Direction.values()) {
			// We ignore both ExtendedStates and randomization for now
			info.sideQuads.put(side, convertQuads(model.getQuads(blockState, side, new Random())));
		}
		info.generalQuads = convertQuads(model.getQuads(blockState, null, new Random()));
		return info;
	}

	private static List<QuadInfo> convertQuads(Iterable<BakedQuad> quads) {
		List<QuadInfo> list = new ArrayList<QuadInfo>();
		for (BakedQuad quad : quads) {
			list.add(QuadInfo.forBakedQuad(quad));
		}
		return list;
	}
}
