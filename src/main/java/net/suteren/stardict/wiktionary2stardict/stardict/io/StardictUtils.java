package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.nio.ByteOrder;

public class StardictUtils {
	/**
	 * Převede hodnotu (two's complement) do byte[] o dané velikosti.
	 *
	 * @param value hodnota k převodu
	 * @param sizeInBits počet bajtů v cílovém poli (1..8)
	 * @param networkByteOrder true = network order (big-endian), false = machine order (ByteOrder.nativeOrder())
	 * @return pole bajtů délky size
	 * @throws IllegalArgumentException pokud size není v rozsahu 1..8
	 */
	public static byte[] toBytes(long value, int sizeInBits, boolean networkByteOrder) {
		if (sizeInBits < 1 || sizeInBits > Long.SIZE) {
			throw new IllegalArgumentException("size must be between 1 and 8");
		}

		ByteOrder order = networkByteOrder ? ByteOrder.BIG_ENDIAN : ByteOrder.nativeOrder();
		int sizeInBytes = sizeInBits / Byte.SIZE;
		byte[] out = new byte[sizeInBytes];

		if (order == ByteOrder.BIG_ENDIAN) {
			for (int i = 0; i < sizeInBytes; i++) {
				int shift = (sizeInBytes - 1 - i) * 8;
				out[i] = (byte) (value >>> shift);
			}
		} else { // LITTLE_ENDIAN
			for (int i = 0; i < sizeInBytes; i++) {
				int shift = i * 8;
				out[i] = (byte) (value >>> shift);
			}
		}
		return out;
	}
}
