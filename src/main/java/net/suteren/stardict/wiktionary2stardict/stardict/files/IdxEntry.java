package net.suteren.stardict.wiktionary2stardict.stardict.files;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record IdxEntry(String word, long offset, long size) implements Comparable<IdxEntry> {
	@Override public int compareTo(IdxEntry o) {
		if (o == null) {
			return 1;
		}
		return Objects.compare(word, o.word(), String::compareTo);
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
