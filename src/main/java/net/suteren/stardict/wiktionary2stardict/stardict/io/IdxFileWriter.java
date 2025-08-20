package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.files.SynonymumEntry;

@RequiredArgsConstructor
public class IdxFileWriter implements AutoCloseable {

	@Getter List<SynonymumEntry> indexEntries = new ArrayList<>();
	private final OutputStream outputStream;
	private final AtomicInteger currentOffset = new AtomicInteger(0);

	public static List<SynonymumEntry> writeIdxFile(String baseName, List<IdxEntry> sortedIdx) throws Exception {
		try (IdxFileWriter idxFileWriter = new IdxFileWriter(new FileOutputStream("%s.idx".formatted(baseName)))) {
			for (IdxEntry entry : sortedIdx) {
				idxFileWriter.writeEntry(entry);
			}
			return idxFileWriter.getIndexEntries();
		}
	}

	public void writeEntry(IdxEntry entry) throws IOException {
		byte[] wordBytes = entry.word().getBytes(StandardCharsets.UTF_8);
		ByteBuffer buffer = ByteBuffer.allocate(wordBytes.length + 1 + 8) // word + null terminator + 2x 32-bit int
			.order(ByteOrder.BIG_ENDIAN);
		buffer.put(wordBytes);        // UTF-8 encoded word
		buffer.put((byte) 0);         // null terminator
		buffer.putInt((int) entry.offset());        // offset in network byte order
		buffer.putInt((int) entry.size());          // size in network byte order
		outputStream.write(buffer.array());
		indexEntries.add(new SynonymumEntry(entry.word(), currentOffset.getAndAdd(1)));
	}

	@Override public void close() throws Exception {
		outputStream.close();
	}
}
