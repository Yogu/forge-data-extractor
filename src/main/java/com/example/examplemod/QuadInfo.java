package com.example.examplemod;

import net.minecraft.client.renderer.block.model.BakedQuad;
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

		public Builder(BakedQuad quad) {
			this.quad = quad;
			this.vertexData = this.convertVertexDataToByteArray();
		}

		public QuadInfo build() {
			QuadInfo info = new QuadInfo();
			info.shouldApplyDiffuseLighting = quad.shouldApplyDiffuseLighting();
			info.positions = getPositions();
			info.normals = getNormals();
			info.textureCoordinates = getTextureCoordinates();
			info.colors = getColors();
			return info;
		}

		private byte[] convertVertexDataToByteArray() {
			byte[] vertexDataInBytes = new byte[quad.getVertexData().length * 4];
			ByteBuffer vertexBuffer = ByteBuffer.wrap(vertexDataInBytes);
			vertexBuffer.asIntBuffer().put(quad.getVertexData());
			return vertexDataInBytes;
		}

		private float[] getPositions() {
			return extractElementForAllVertices(VertexFormatElement.EnumUsage.POSITION, 3);
		}

		private float[] getNormals() {
			return extractElementForAllVertices(VertexFormatElement.EnumUsage.NORMAL, 3);
		}

		private float[] getTextureCoordinates() {
			return extractElementForAllVertices(VertexFormatElement.EnumUsage.UV, 2);
		}

		private float[] getColors() {
			return extractElementForAllVertices(VertexFormatElement.EnumUsage.COLOR, 4);
		}

		private float[] extractElementForAllVertices(VertexFormatElement.EnumUsage usage,
				int expectedElementCount, int elementIndex) {
			int index = this.findElement(quad.getFormat(), usage, elementIndex);
			if (index < 0) {
				return null;
			}
			VertexFormatElement element = quad.getFormat().getElement(index);
			assertElementCount(element, expectedElementCount);
			return extractElementForAllVertices(index);
		}

		private float[] extractElementForAllVertices(VertexFormatElement.EnumUsage usage,
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
			VertexFormatElement element = quad.getFormat().getElement(elementIndex);
			int elementOffset = quad.getFormat().getOffset(elementIndex);
			int elementSize = element.getSize();
			int elementCount = element.getElementCount();
			float[] result = new float[element.getElementCount() * VERTEX_COUNT];
			for (int i = 0; i < VERTEX_COUNT; i++) {
				int vertexElementOffset = elementOffset + i * quad.getFormat().getIntegerSize() * 4;
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
					break;
				case BYTE:
					byte[] bytes = new byte[elementCount];
					data.get(bytes);
					for (int i = 0; i < elementCount; i++) {
						floats[i] = bytes[i];
					}
					break;
				case SHORT:
					short[] shorts = new short[elementCount];
					data.asShortBuffer().get(shorts);
					for (int i = 0; i < elementCount; i++) {
						floats[i] = shorts[i];
					}
					break;
				case UBYTE:
					byte[] sbytes = new byte[elementCount];
					data.get(sbytes);
					for (int i = 0; i < elementCount; i++) {
						// copied from Byte.toUnsignedInt (since Java 1.8)
						floats[i] = ((int) sbytes[i]) & 0xFF;
					}
					break;
				case USHORT:
					short[] sshorts = new short[elementCount];
					data.asShortBuffer().get(sshorts);
					for (int i = 0; i < elementCount; i++) {
						// copied from Short.toUnsignedInt (since Java 1.8)
						floats[i] = ((int) sshorts[i]) & 0xFFFF;
					}
					break;
				case UINT:
					int[] sints = new int[elementCount];
					data.asIntBuffer().get(sints);
					for (int i = 0; i < elementCount; i++) {
						// copied from Integer.toUnsignedLong (since Java 1.8)
						floats[i] = ((long) sints[i]) & 0xFFFFFFFFL;
					}
				default:
					throw new RuntimeException("Unsupported type: " + formatElement.getType());
			}
			return floats;
		}

		private int findElement(VertexFormat format,
				VertexFormatElement.EnumUsage usage, int elementIndex) {
			int i = 0;
			for (VertexFormatElement element : format.getElements()) {
				if (element.getUsage() == usage && element.getIndex() == elementIndex) {
					return i;
				}
				i++;
			}
			return -1;
		}
	}
}
