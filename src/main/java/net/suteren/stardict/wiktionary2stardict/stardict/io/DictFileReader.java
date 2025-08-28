package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.DefinitionEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.WordDefinition;

/**
 * Class for reading StarDict .dict files
 */
@RequiredArgsConstructor
public class DictFileReader implements AutoCloseable {

	private final FileChannel is;
	private final List<IdxEntry> idxEntries;
	private final Collection<EntryType> sameTypeSequence;

	/**
	 * Loads word definitions from a .dict file according to records in the .idx file
	 *
	 * @param dictFilename Path to the .dict file
	 * @param idxEntries List of records from the .idx file
	 * @param sameTypeSequence The sameTypeSequence value from the .ifo file (can be null)
	 * @return Map of words and their definitions
	 * @throws IOException When a file reading error occurs
	 */
	public Map<String, WordDefinition> readDictFile() throws IOException {

		Map<String, WordDefinition> definitions = new HashMap<>();
		for (IdxEntry entry : idxEntries) {
			definitions.computeIfAbsent(entry.word(), x -> new WordDefinition(x, new ArrayList<>()))
				.getDefinitions()
				.addAll(readWordDefinition(entry));

		}
		return definitions;
	}

	public List<DefinitionEntry> readWordDefinition(IdxEntry entry) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(entry.size());
		is.read(buffer, entry.offset());

		List<EntryType> types;
		if (sameTypeSequence != null && !sameTypeSequence.isEmpty()) {
			types = List.copyOf(sameTypeSequence);
		} else {
			types = List.of(EntryType.resolve((char) buffer.get()));
		}

		EntryType mainType = types.getFirst();

		String def;
		if (mainType.isString()) {
			def = StardictIoUtil.readNullTerminatedUtf8String(buffer);
		} else {
			byte[] sizeBytes = new byte[4];
			buffer.get(sizeBytes);
			long dataSize = StardictIoUtil.toLong(sizeBytes, 32, true);
			byte[] dataBytes = new byte[(int) dataSize];
			buffer.get(dataBytes);
			def = new String(dataBytes, StandardCharsets.UTF_8);
		}

		return types.stream()
			.map(t -> new DefinitionEntry(t, def))
			.toList();
	}

	@Override public void close() throws Exception {
		is.close();
	}
}