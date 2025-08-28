package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

		ByteBuffer buffer = ByteBuffer.allocate(255);
		for (int b = fis.read(), dataSize = 1; b >= 0; b = fis.read(), dataSize++) {
			if (b == 0) {
				byte[] offsetBytes = fis.readNBytes(sizeInBits / Byte.SIZE);
				long offset = StardictIoUtil.toLong(offsetBytes, sizeInBits, true);

				byte[] sizeBytes = fis.readNBytes(Integer.SIZE / Byte.SIZE);
				int size = (int) StardictIoUtil.toLong(sizeBytes, Integer.SIZE, true);

				entries.add(new IdxEntry(StandardCharsets.UTF_8.decode(buffer.slice(0, dataSize - 1)).toString(), offset, size));

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
