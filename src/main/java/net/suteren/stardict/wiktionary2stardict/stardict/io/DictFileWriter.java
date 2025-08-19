package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.files.DefinitionEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;

/**
 * Class for writing StarDict .dict files
 */
@RequiredArgsConstructor
public class DictFileWriter implements AutoCloseable {

	@Getter private final SortedSet<IdxEntry> idxEntries = new TreeSet<>();
	private final OutputStream writer;
	private final AtomicLong currentOffset = new AtomicLong(0);

	public long writeEntry(String term, DefinitionEntry definitionEntry) throws IOException {
		byte[] data = definitionEntry.getDefinition().getBytes(StandardCharsets.UTF_8);
		writer.write(definitionEntry.getType().getType());
		writer.write(0);
		writer.write(data);
		long size = 2 + data.length;
		idxEntries.add(new IdxEntry(term, currentOffset.getAndAdd(size), size));
		return size;
	}

	@Override public void close() throws Exception {
		writer.close();
	}
}