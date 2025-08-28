package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

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

	public static List<IdxEntry> writeDefinitionFile(String baseName, List<WordDefinition> definitions) throws Exception {
		try (DictFileWriter dictFileWriter = new DictFileWriter(new BufferedOutputStream(new FileOutputStream("%s.dict".formatted(baseName))))) {
			// Write dict -> idx entries
			for (WordDefinition wordDef : definitions) {
				dictFileWriter.writeWordDefinition(wordDef);
			}
			return dictFileWriter.getIdxEntries();
		}
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
			.map(DefinitionEntry::getDefinition)
			.map(d -> d.getBytes(StandardCharsets.UTF_8));
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
}