package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
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

		CharBuffer cb = CharBuffer.allocate(255);
		for (int i = fis.read(); i >= 0; i = fis.read()) {
			char c = (char) i;
			if (c == 0) {
				entries.add(new SynonymumEntry(cb.toString(),
					(int) StardictIoUtil.toLong(fis.readNBytes(Integer.SIZE / Byte.SIZE), Integer.SIZE, true)));
				cb.clear();
			} else {
				cb.append(c);
			}
		}
		return entries;
	}

	@Override public void close() throws Exception {
		fis.close();
	}
}