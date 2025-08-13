package net.suteren.stardict.wiktionary2stardict.stardict.files;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record IdxEntry(String word, int offset, int size) implements Comparable<IdxEntry> {
	@Override public int compareTo(IdxEntry o) {
		if (o == null) {
			return 1;
		}
		if (word == null) {
			return -1;
		}
		return word.compareTo(o.word);
	}

	/**
	 * Serializuje IdxEntry do binárního formátu pro StarDict .idx soubor
	 * Formát: UTF-8 string (null-terminated) + 32-bit offset (network order) + 32-bit size (network order)
	 */
	public byte[] toBytes() {
		byte[] wordBytes = word.getBytes(StandardCharsets.UTF_8);

		ByteBuffer buffer = ByteBuffer.allocate(wordBytes.length + 1 + 8) // word + null terminator + 2x 32-bit int
			.order(ByteOrder.BIG_ENDIAN);

		buffer.put(wordBytes);        // UTF-8 encoded word
		buffer.put((byte) 0);         // null terminator
		buffer.putInt(offset);        // offset in network byte order
		buffer.putInt(size);          // size in network byte order

		return buffer.array();
	}

	/**
	 * Vytvoří IdxEntry z binárních dat
	 */
	public static IdxEntry fromBytes(ByteBuffer buffer) {
		buffer.order(ByteOrder.BIG_ENDIAN);

		// Načtení UTF-8 string až do null terminátoru
		StringBuilder wordBuilder = new StringBuilder();
		byte b;
		while ((b = buffer.get()) != 0) {
			wordBuilder.append((char) (b & 0xFF));
		}
		String word = new String(wordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

		int offset = buffer.getInt();  // automaticky v network byte order
		int size = buffer.getInt();    // automaticky v network byte order

		return new IdxEntry(word, offset, size);
	}

}
