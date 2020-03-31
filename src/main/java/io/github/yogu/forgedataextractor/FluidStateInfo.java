package io.github.yogu.forgedataextractor;

import net.minecraft.fluid.IFluidState;

public class FluidStateInfo {
    String qualifiedBlockStateName;

    public static FluidStateInfo fromFluidState(IFluidState fluidState) {
        FluidStateInfo info = new FluidStateInfo();
        info.qualifiedBlockStateName = BlockStateInfo.getQualifiedName(fluidState.getBlockState());
        return info;
    }
}
