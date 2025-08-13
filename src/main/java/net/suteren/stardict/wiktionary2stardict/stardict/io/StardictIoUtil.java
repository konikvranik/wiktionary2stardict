package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Utility třída pro I/O operace se StarDict soubory
 */
public class StardictIoUtil {

	/**
	 * Převede 32-bit integer do network byte order (big-endian)
	 * Ekvivalent g_htonl() z C
	 */
	public static byte[] intToNetworkBytes(int value) {
		return ByteBuffer.allocate(4)
			.order(ByteOrder.BIG_ENDIAN)
			.putInt(value)
			.array();
	}

	/**
	 * Načte 32-bit integer z network byte order (big-endian)
	 * Ekvivalent g_ntohl() z C
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
	 * Převede string do UTF-8 bytů
	 */
	public static byte[] stringToUtf8Bytes(String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Načte UTF-8 string z bytů
	 */
	public static String utf8BytesToString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	/**
	 * Načte null-terminated UTF-8 string z ByteBuffer
	 * @param buffer ByteBuffer obsahující data
	 * @return Načtený string
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
	 * Zapíše null-terminated UTF-8 string do ByteBuffer
	 * @param buffer ByteBuffer pro zápis
	 * @param str String k zapsání
	 */
	public static void writeNullTerminatedUtf8String(ByteBuffer buffer, String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		buffer.put(bytes);
		buffer.put((byte) 0); // null terminator
	}
	
	/**
	 * Vytvoří ByteBuffer s null-terminated UTF-8 stringem
	 * @param str String k zapsání
	 * @return ByteBuffer obsahující string a null terminator
	 */
	public static ByteBuffer createNullTerminatedUtf8StringBuffer(String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1);
		buffer.put(bytes);
		buffer.put((byte) 0); // null terminator
		buffer.flip();
		return buffer;
	}
}
