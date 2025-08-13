package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.util.Collection;

import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;

public class IdxFileWriter {

	public static void writeIdxFile(String filename, Collection<IdxEntry> entries) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(filename);
			FileChannel channel = fos.getChannel()) {

			for (IdxEntry entry : entries) {
				byte[] entryBytes = entry.toBytes();
				ByteBuffer buffer = ByteBuffer.wrap(entryBytes);
				channel.write(buffer);
			}
		}
	}
}
