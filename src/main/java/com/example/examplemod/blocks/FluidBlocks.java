package com.example.examplemod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * Created by jan on 4/1/17.
 */
public class FluidBlocks {
	public static final Block WATER = getWater();
	public static final Block LAVA = getLava();


	private static BlockFluidClassic getWater() {
		Fluid fluid = new Fluid("water", new ResourceLocation("water"),
				new ResourceLocation("flowing_water"));
		BlockFluidClassic block = new BlockFluidClassic(fluid, Material.WATER);
		fluid.setBlock(block);
		return block;
	}

	private static BlockFluidClassic getLava() {
		Fluid fluid = new Fluid("lava", new ResourceLocation("lava"),
				new ResourceLocation("flowing_lava"));
		BlockFluidClassic block = new BlockFluidClassic(fluid, Material.LAVA);
		fluid.setBlock(block);
		return block;
	}
}
