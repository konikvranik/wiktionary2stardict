package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for I/O operations with StarDict files
 */
public class StardictIoUtil {

	/**
	 * Converts a 32-bit integer to network byte order (big-endian)
	 * Equivalent to g_htonl() from C
	 */
	public static byte[] intToNetworkBytes(int value) {
		return ByteBuffer.allocate(4)
			.order(ByteOrder.BIG_ENDIAN)
			.putInt(value)
			.array();
	}

	/**
	 * Reads a 32-bit integer from network byte order (big-endian)
	 * Equivalent to g_ntohl() from C
	 */
	public static int networkBytesToInt(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException("Expected 4 bytes for int, got " + bytes.length);
		}
		return ByteBuffer.wrap(bytes)
			.order(ByteOrder.BIG_ENDIAN)
			.getInt();
	}

	/**
	 * Converts a string to UTF-8 bytes
	 */
	public static byte[] stringToUtf8Bytes(String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Reads a UTF-8 string from bytes
	 */
	public static String utf8BytesToString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Reads a null-terminated UTF-8 string from ByteBuffer
	 *
	 * @param buffer ByteBuffer containing data
	 * @return The read string
	 */
	public static String readNullTerminatedUtf8String(ByteBuffer buffer) {
		StringBuilder sb = new StringBuilder();
		byte b;
		while (buffer.hasRemaining() && (b = buffer.get()) != 0) {
			sb.append((char) (b & 0xFF));
		}
		return new String(sb.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
	}

	/**
	 * Writes a null-terminated UTF-8 string to ByteBuffer
	 *
	 * @param buffer ByteBuffer for writing
	 * @param str String to write
	 */
	public static void writeNullTerminatedUtf8String(ByteBuffer buffer, String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		buffer.put(bytes);
		buffer.put((byte) 0); // null terminator
	}

	/**
	 * Creates a ByteBuffer with a null-terminated UTF-8 string
	 *
	 * @param str String to write
	 * @return ByteBuffer containing the string and null terminator
	 */
	public static ByteBuffer createNullTerminatedUtf8StringBuffer(String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1);
		buffer.put(bytes);
		buffer.put((byte) 0); // null terminator
		buffer.flip();
		return buffer;
	}

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
		if (sizeInBits != Long.SIZE && sizeInBits != Integer.SIZE) {
			throw new IllegalArgumentException("size must be either 32 or 64, got %d.".formatted(sizeInBits));
		}
		ByteBuffer buffer = ByteBuffer.allocate(sizeInBits / Byte.SIZE)
			.order(networkByteOrder ? ByteOrder.BIG_ENDIAN : ByteOrder.nativeOrder());
		if (sizeInBits == Long.SIZE) {
			buffer.putLong(value);
		} else {
			buffer.putInt((int) value);
		}
		return buffer.array();
	}

	public static long toLong(byte[] bytes, int sizeInBits, boolean networkByteOrder) {
		if (sizeInBits != Long.SIZE && sizeInBits != Integer.SIZE) {
			throw new IllegalArgumentException("size must be either 32 or 64, got %d.".formatted(sizeInBits));
		}
		return ByteBuffer.wrap(bytes)
			.order(networkByteOrder ? ByteOrder.BIG_ENDIAN : ByteOrder.nativeOrder())
			.getLong();

	}
}
