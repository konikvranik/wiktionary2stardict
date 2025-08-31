package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;

@RequiredArgsConstructor
public class IdxFileWriter implements AutoCloseable {

	@Getter List<SynonymumEntry> indexEntries = new ArrayList<>();
	private final OutputStream outputStream;
	private final int sizeInBits;
	private final AtomicInteger currentOffset = new AtomicInteger(0);

	public static List<SynonymumEntry> writeIdxFile(String baseName, List<IdxEntry> sortedIdx, int sizeInBits) throws Exception {
		try (IdxFileWriter idxFileWriter = new IdxFileWriter(new FileOutputStream("%s.idx".formatted(baseName)), sizeInBits)) {
			for (IdxEntry entry : sortedIdx) {
				idxFileWriter.writeEntry(entry);
			}
			return idxFileWriter.getIndexEntries();
		}
	}

	public void writeEntry(IdxEntry entry) throws IOException {
		byte[] wordBytes = entry.word().getBytes(StandardCharsets.UTF_8);
		outputStream.write(wordBytes);
		outputStream.write(0);
		outputStream.write(StardictIoUtil.toBytes(entry.offset(), sizeInBits, true));
		outputStream.write(StardictIoUtil.toBytes(entry.size(), 32, true));
		indexEntries.add(new SynonymumEntry(entry.word(), currentOffset.getAndAdd(1)));
	}

	@Override public void close() throws Exception {
		outputStream.close();
	}
}
