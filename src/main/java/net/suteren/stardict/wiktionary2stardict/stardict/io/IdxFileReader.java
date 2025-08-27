package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;

@RequiredArgsConstructor
public class IdxFileReader implements AutoCloseable {

	private final InputStream fis;
	private final int sizeInBits;

	public List<IdxEntry> readIdxFile() throws IOException {
		List<IdxEntry> entries = new ArrayList<>();

		CharBuffer cb = CharBuffer.allocate(255);
		for (int i = fis.read(); i >= 0; i = fis.read()) {
			char c = (char) i;
			if (c == 0) {
				entries.add(new IdxEntry(cb.toString(),
					StardictIoUtil.toLong(fis.readNBytes(sizeInBits / Byte.SIZE), sizeInBits, true),
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
