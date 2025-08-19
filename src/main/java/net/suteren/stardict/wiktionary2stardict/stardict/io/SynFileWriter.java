package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.files.SynonymumEntry;

/**
 * Class for writing StarDict .syn files
 */
@RequiredArgsConstructor
public class SynFileWriter implements AutoCloseable {

	private final OutputStream outputStream;

	/**
	 * Converts a synonym record to a byte array
	 *
	 * @param entry Synonym record
	 * @return Byte array representing the record
	 */
	public int writeEntry(SynonymumEntry entry) throws IOException {
		byte[] wordBytes = entry.word().getBytes(StandardCharsets.UTF_8);
		int size = wordBytes.length + 1 + 4;
		ByteBuffer buffer = ByteBuffer.allocate(size); // word + null terminator + 32-bit int
		buffer.put(wordBytes);        // UTF-8 encoded word
		buffer.put((byte) 0);         // null terminator
		buffer.putInt(entry.indexPosition()); // index position in network byte order
		buffer.order(ByteOrder.BIG_ENDIAN);
		outputStream.write(buffer.array());
		return size;
	}

	@Override public void close() throws Exception {
		outputStream.close();
	}
}