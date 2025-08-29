package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.DefinitionEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.WordDefinition;

/**
 * Class for writing StarDict .dict files
 */
@RequiredArgsConstructor
public class DictFileWriter implements AutoCloseable {

	@Getter private final List<IdxEntry> idxEntries = new ArrayList<>();
	private final OutputStream outputStream;
	private final AtomicLong currentOffset = new AtomicLong(0);
	private final Mode mode;

	public List<IdxEntry> writeDefinitionFile(Stream<WordDefinition> definitions) throws Exception {
		// Nejprve seskupíme definice podle slova a sloučíme je do jednoho záznamu na slovo
		Map<String, Set<DefinitionEntry>> groupedDefinitionEntries = definitions.collect(Collectors.groupingBy(WordDefinition::getWord,
			Collectors.mapping(WordDefinition::getDefinitions,
				Collectors.flatMapping(Collection::stream,
					Collectors.toSet()))));

		// Zapišeme po jednom záznamu pro každé slovo
		for (Map.Entry<String, Set<DefinitionEntry>> entry : groupedDefinitionEntries.entrySet()) {
			writeWordDefinition(new WordDefinition(entry.getKey(), entry.getValue()));
		}
		return getIdxEntries();
	}

	public int writeWordDefinition(WordDefinition wordDef) throws IOException {
		int size = 0;
		for (DefinitionEntry definitionEntry : wordDef.getDefinitions()) {
			size += writeEntry(definitionEntry);
		}
		idxEntries.add(new IdxEntry(wordDef.getWord(), currentOffset.getAndAdd(size), size - 1));
		return size;
	}

	private int writeEntry(DefinitionEntry definitionEntry) throws IOException {
		Optional<byte[]> definitionBytes = Optional.ofNullable(definitionEntry)
			.map(DefinitionEntry::getDefinition);
		int size = 0;
		if (definitionBytes.isPresent()) {
			byte[] data = definitionBytes.get();
			size++;
			outputStream.write(definitionEntry.getType().getType());
			if (definitionEntry.getType().isString()) {
				outputStream.write(data);
				size += data.length;
				outputStream.write(0);
				size++;
				return size;
			} else {
				outputStream.write(StardictIoUtil.toBytes(data.length, 32, true));
				size += 32 / Byte.SIZE;
				outputStream.write(data);
				size += data.length;
				return size;
			}
		} else {
			return size;
		}
	}

	@Override public void close() throws Exception {
		outputStream.close();
	}

	public enum Mode {
		ALL, SPECIFIC, PANGO, XDXF, HTML
	}
}