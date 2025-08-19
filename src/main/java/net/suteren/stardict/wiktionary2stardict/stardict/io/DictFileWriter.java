package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
		Optional<byte[]> db = Optional.ofNullable(definitionEntry)
			.map(DefinitionEntry::getDefinition)
			.map(d -> d.getBytes(StandardCharsets.UTF_8));
		if (db.isPresent()) {
			byte[] data = db.get();
			writer.write(definitionEntry.getType().getType());
			writer.write(0);
			writer.write(data);
			long size = 2 + data.length;
			idxEntries.add(new IdxEntry(term, currentOffset.getAndAdd(size), size));
			return size;
		} else {
			return 0;
		}
	}

	@Override public void close() throws Exception {
		writer.close();
	}
}