package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;

@RequiredArgsConstructor
public class IdxFileWriter implements AutoCloseable {
	private final OutputStream outputStream;

	public void writeEntry(IdxEntry entry) throws IOException {
		byte[] wordBytes = entry.word().getBytes(StandardCharsets.UTF_8);
		ByteBuffer buffer = ByteBuffer.allocate(wordBytes.length + 1 + 8) // word + null terminator + 2x 32-bit int
			.order(ByteOrder.BIG_ENDIAN);
		buffer.put(wordBytes);        // UTF-8 encoded word
		buffer.put((byte) 0);         // null terminator
		buffer.putInt((int) entry.offset());        // offset in network byte order
		buffer.putInt((int) entry.size());          // size in network byte order
		outputStream.write(buffer.array());
	}

	@Override public void close() throws Exception {
		outputStream.close();
	}
}
