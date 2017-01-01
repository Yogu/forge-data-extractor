package com.example.examplemod;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Extracts a texture map into a file
 */
public class TextureMapExtractor {
	private static final Logger LOGGER = LogManager.getLogger();

	public void run(TextureMap map) {
		// Get pixel data from OpenGL
		int level = 0; // mip map level
		GlStateManager.bindTexture(map.getGlTextureId());
		int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_WIDTH);
		int height =
				GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_HEIGHT);
		int size = width * height * 4;
		ByteBuffer imageBuffer = BufferUtils.createByteBuffer(size);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, level, GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageBuffer);
		GlStateManager.bindTexture(0);

		// Convert pixel data to image
		byte[] imageData = new byte[size];
		imageBuffer.get(imageData);
		DataBuffer dataBuffer = new DataBufferByte(imageData, size);
		SampleModel sampleModel =
				new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 4, width * 4,
						new int[] { 0, 1, 2, 3 });
		Raster raster = Raster.createRaster(sampleModel, dataBuffer, new Point(0, 0));
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		image.setData(raster);

		// Write image to file
		try {
			ImageIO.write(image, "png", new File(DataExtractionMod.OUTPUT_PATH + "block-texture.png"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		LOGGER.info("created block-texture.png");
	}
}
