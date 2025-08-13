package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;

public class IdxFileReader {

	public static List<IdxEntry> readIdxFile(String filename) throws IOException {
		List<IdxEntry> entries = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(filename);
			FileChannel channel = fis.getChannel()) {

			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
			channel.read(buffer);
			buffer.flip();

			while (buffer.hasRemaining()) {
				IdxEntry entry = IdxEntry.fromBytes(buffer);
				entries.add(entry);
			}
		}

		return entries;
	}
}