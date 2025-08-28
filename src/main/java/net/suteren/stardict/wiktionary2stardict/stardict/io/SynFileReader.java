package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;

/**
 * Class for reading StarDict .syn files
 */
@RequiredArgsConstructor
public class SynFileReader implements AutoCloseable {

	private final FileInputStream fis;

	/**
	 * Loads synonyms from a .syn file
	 *
	 * @param filename Path to the .syn file
	 * @return List of synonym records
	 * @throws IOException When a file reading error occurs
	 */
	public List<SynonymumEntry> readSynFile() throws IOException {
		List<SynonymumEntry> entries = new ArrayList<>();

		ByteBuffer buffer = ByteBuffer.allocate(255);
		for (int b = fis.read(), dataSize = 1; b >= 0; b = fis.read(), dataSize++) {
			if (b == 0) {

				byte[] sizeBytes = fis.readNBytes(Integer.SIZE / Byte.SIZE);
				int size = (int) StardictIoUtil.toLong(sizeBytes, Integer.SIZE, true);

				entries.add(new SynonymumEntry(StandardCharsets.UTF_8.decode(buffer.slice(0, dataSize - 1)).toString(), size));

				dataSize = 0;
				buffer.clear();
			} else {
				buffer.put((byte) b);
			}
		}
		return entries;
	}

	@Override public void close() throws Exception {
		fis.close();
	}
}