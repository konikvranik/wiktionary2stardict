package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;

/**
 * Class for writing StarDict .syn files
 */
@RequiredArgsConstructor
public class SynFileWriter implements AutoCloseable {

	private final OutputStream outputStream;

	public static void writeSynFile(String baseName, List<SynonymumEntry> idxEntries) throws Exception {
		try (SynFileWriter synFileWriter = new SynFileWriter(new FileOutputStream("%s.syn".formatted(baseName)))) {
			for (SynonymumEntry entry : idxEntries) {
				synFileWriter.writeEntry(entry);
			}
		}
	}

	/**
	 * Converts a synonym record to a byte array
	 *
	 * @param entry Synonym record
	 * @return Byte array representing the record
	 */
	public int writeEntry(SynonymumEntry entry) throws IOException {
		byte[] wordBytes = entry.word().getBytes(StandardCharsets.UTF_8);
		int size = wordBytes.length + 1 + 4;
		outputStream.write(wordBytes);
		outputStream.write(0);
		outputStream.write(StardictIoUtil.toBytes(entry.indexPosition(), 32, true));
		return size;
	}

	@Override public void close() throws Exception {
		outputStream.close();
	}
}