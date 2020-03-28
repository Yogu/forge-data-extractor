package com.example.examplemod;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Static information about a {@link BakedQuad}
 */
public class QuadInfo {
	private static final int VERTEX_COUNT = 4;

	private boolean shouldApplyDiffuseLighting;
	private float[] positions;
	private float[] textureCoordinates;
	private float[] normals;
	private float[] colors;
	private int tintIndex;

	public static QuadInfo forBakedQuad(BakedQuad quad) {
		return new Builder(quad).build();
	}

	private static class Builder {
		private BakedQuad quad;
		private byte[] vertexData;
		protected float[] positions;
		private float[] textureCoordinates;
		private float[] normals;
		private float[] colors;
		private int tintIndex;

		public Builder(BakedQuad quad) {
			this.quad = quad;
			this.vertexData = this.convertVertexDataToByteArray();
			this.tintIndex = quad.getTintIndex();
		}

		public QuadInfo build() {
			QuadInfo info = new QuadInfo();
			info.shouldApplyDiffuseLighting = quad.shouldApplyDiffuseLighting();
			info.positions = getPositions();
			info.normals = getNormals();
			info.textureCoordinates = getTextureCoordinates();
			info.colors = getColors();
			info.tintIndex = tintIndex;
			return info;
		}

		private byte[] convertVertexDataToByteArray() {
			byte[] vertexDataInBytes = new byte[quad.getVertexData().length * 4];
			ByteBuffer vertexBuffer = ByteBuffer.wrap(vertexDataInBytes);
			vertexBuffer.asIntBuffer().put(quad.getVertexData());
			return vertexDataInBytes;
		}

		private float[] getPositions() {
			return extractElementForAllVertices(VertexFormatElement.Usage.POSITION, 3);
		}

		private float[] getNormals() {
			return extractElementForAllVertices(VertexFormatElement.Usage.NORMAL, 3);
		}

		private float[] getTextureCoordinates() {
			return extractElementForAllVertices(VertexFormatElement.Usage.UV, 2);
		}

		/**
		 * Takes an array of sprite-local UV coordinates and converts it to texture-global coords
		 * @param coordsWithinSprite the sprite-local UV coordinates
		 * @return the global UV coordinates
		 */
		private float[] applySpriteTextureCoordinates(float[] coordsWithinSprite) {
			float minU = quad.getSprite().getMinU();
			float maxU = quad.getSprite().getMinU();
			float minV = quad.getSprite().getMinV();
			float maxV = quad.getSprite().getMinV();
			float[] result = new float[coordsWithinSprite.length];
			for (int i = 0; i < coordsWithinSprite.length / 2; i++) {
				float localU = coordsWithinSprite[i * 2];
				float localV = coordsWithinSprite[i * 2 + 1];
				float globalU = minU + localU * (maxU - minU);
				float globalV = minV + localV * (maxV - minV);
				result[i * 2] = globalU;
				result[i * 2 + 1] = globalV;
			}
			return result;
		}

		private float[] getColors() {
			return extractElementForAllVertices(VertexFormatElement.Usage.COLOR, 4);
		}

		private float[] extractElementForAllVertices(VertexFormatElement.Usage usage,
				int expectedElementCount, int elementIndex) {
			int index = this.findElement(DefaultVertexFormats.BLOCK, usage, elementIndex);
			if (index < 0) {
				return null;
			}
			VertexFormatElement element = DefaultVertexFormats.BLOCK.func_227894_c_().get(index); // func_227894_c_ is getElements()
			assertElementCount(element, expectedElementCount);
			return extractElementForAllVertices(index);
		}

		private float[] extractElementForAllVertices(VertexFormatElement.Usage usage,
				int expectedElementCount) {
			return extractElementForAllVertices(usage, expectedElementCount, 0);
		}

		private void assertElementCount(VertexFormatElement element, int count) {
			if (element.getElementCount() != count) {
				throw new RuntimeException(
						"Expected " + count + " elements, got " + element.getElementCount());
			}
		}

		/**
		 * Extracts the given element of all vertices in the quad and interprets the data as numbers
		 *
		 * @param elementIndex the index of the element to extract
		 * @return the numbers of this element in all vertices
		 */
		private float[] extractElementForAllVertices(int elementIndex) {
			VertexFormatElement element = DefaultVertexFormats.BLOCK.func_227894_c_().get(elementIndex);
			int elementOffset = DefaultVertexFormats.BLOCK.getOffset(elementIndex);
			int elementSize = element.getSize();
			int elementCount = element.getElementCount();
			float[] result = new float[element.getElementCount() * VERTEX_COUNT];
			for (int i = 0; i < VERTEX_COUNT; i++) {
				int vertexElementOffset = elementOffset + i * DefaultVertexFormats.BLOCK.getIntegerSize() * 4;
				byte[] elementData =
						Arrays.copyOfRange(vertexData, vertexElementOffset,
								vertexElementOffset + elementSize);
				ByteBuffer elementBuffer = ByteBuffer.wrap(elementData).asReadOnlyBuffer();
				float[] elementNumbers = interpretElementDataAsNumbers(element, elementBuffer);
				System.arraycopy(elementNumbers, 0, result, i * elementCount, elementCount);

			}
			return result;
		}

		/**
		 * Interprets {@code data} as an array of numbers like specified in {@code formatElement}
		 *
		 * @param formatElement the specification for type and count
		 * @param data          the raw data
		 * @return the numbers
		 */
		private float[] interpretElementDataAsNumbers(VertexFormatElement formatElement,
				ByteBuffer data) {
			int elementCount = formatElement.getElementCount();
			float[] floats = new float[elementCount];
			float normalization = 1;
			switch (formatElement.getType()) {
				case FLOAT:
					data.asFloatBuffer().get(floats);
					break;
				case INT:
					int[] ints = new int[elementCount];
					data.asIntBuffer().get(ints);
					for (int i = 0; i < elementCount; i++) {
						floats[i] = ints[i];
					}
					normalization = Integer.MAX_VALUE;
					break;
				case BYTE:
					byte[] bytes = new byte[elementCount];
					data.get(bytes);
					for (int i = 0; i < elementCount; i++) {
						floats[i] = bytes[i];
					}
					normalization = Byte.MAX_VALUE;
					break;
				case SHORT:
					short[] shorts = new short[elementCount];
					data.asShortBuffer().get(shorts);
					for (int i = 0; i < elementCount; i++) {
						floats[i] = shorts[i];
					}
					normalization = Short.MAX_VALUE;
					break;
				case UBYTE:
					byte[] sbytes = new byte[elementCount];
					data.get(sbytes);
					for (int i = 0; i < elementCount; i++) {
						// copied from Byte.toUnsignedInt (since Java 1.8)
						floats[i] = ((int) sbytes[i]) & 0xFF;
					}
					normalization = 0xFF;
					break;
				case USHORT:
					short[] sshorts = new short[elementCount];
					data.asShortBuffer().get(sshorts);
					for (int i = 0; i < elementCount; i++) {
						// copied from Short.toUnsignedInt (since Java 1.8)
						floats[i] = ((int) sshorts[i]) & 0xFFFF;
					}
					normalization = 0xFFFF;
					break;
				case UINT:
					int[] sints = new int[elementCount];
					data.asIntBuffer().get(sints);
					for (int i = 0; i < elementCount; i++) {
						// copied from Integer.toUnsignedLong (since Java 1.8)
						floats[i] = ((long) sints[i]) & 0xFFFFFFFFL;
					}
					normalization = 0xFFFFFFFFL;
				default:
					throw new RuntimeException("Unsupported type: " + formatElement.getType());
			}
			if (normalization != 1) {
				for (int i = 0; i < elementCount; i++) {
					floats[i] = floats[i] / normalization;
				}
			}
			return floats;
		}

		private int findElement(VertexFormat format,
				VertexFormatElement.Usage usage, int elementIndex) {
			int i = 0;
			for (VertexFormatElement element : DefaultVertexFormats.BLOCK.func_227894_c_()) {
				if (element.getUsage() == usage && element.getIndex() == elementIndex) {
					return i;
				}
				i++;
			}
			return -1;
		}
	}
}
