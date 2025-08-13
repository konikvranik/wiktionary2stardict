package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

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
}
