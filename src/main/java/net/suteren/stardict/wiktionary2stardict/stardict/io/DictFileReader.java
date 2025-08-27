package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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
			WordDefinition wordDef = readWordDefinition(entry);

			definitions.put(entry.word(), wordDef);

		}

		return definitions;
	}

	public WordDefinition readWordDefinition(IdxEntry entry) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(entry.size());
		is.read(buffer, entry.offset());
		WordDefinition wordDef;
		int size = entry.size();
		WordDefinition wordDef1 = new WordDefinition();

		if (sameTypeSequence != null && !sameTypeSequence.isEmpty()) {
			wordDef = fromBytesWithSameTypeSequence(buffer, size, sameTypeSequence);
		} else {
			wordDef = fromBytesWithoutSameTypeSequence(buffer, size);
		}
		wordDef.setWord(entry.word());
		return wordDef;
	}

	/**
	 * Vytvoří WordDefinition z bytového bufferu bez použití sameTypeSequence
	 */
	private static WordDefinition fromBytesWithoutSameTypeSequence(ByteBuffer buffer, int size) {
		WordDefinition wordDef = new WordDefinition();
		int startPosition = buffer.position();

		while (buffer.position() - startPosition < size) {
			// Načteme typ
			char typeChar = (char) buffer.get();
			EntryType type = EntryType.resolve(typeChar);

			// Načteme data podle typu
			String data;
			if (Character.isUpperCase(typeChar)) {
				// Typy s velkými písmeny mají délku jako 4-bytové číslo
				int dataLength = buffer.getInt();
				byte[] dataBytes = new byte[dataLength];
				buffer.get(dataBytes);
				data = new String(dataBytes, StandardCharsets.UTF_8);
			} else {
				// Typy s malými písmeny končí null terminátorem
				StringBuilder sb = new StringBuilder();
				byte b;
				while ((b = buffer.get()) != 0) {
					sb.append((char) (b & 0xFF));
				}
				data = new String(sb.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
			}

			DefinitionEntry entry = new DefinitionEntry(type, data);
			wordDef.getDefinitions().add(entry);
		}

		return wordDef;
	}

	/**
	 * Vytvoří WordDefinition z bytového bufferu s použitím sameTypeSequence
	 */
	private static WordDefinition fromBytesWithSameTypeSequence(ByteBuffer buffer, int size, Collection<EntryType> sameTypeSequence) {

		WordDefinition wordDef = new WordDefinition();
		int startPosition = buffer.position();

		for (int i = 0; i < sameTypeSequence.length(); i++) {
			char typeChar = sameTypeSequence.charAt(i);
			EntryType type = EntryType.resolve(typeChar);
			boolean isLastEntry = (i == sameTypeSequence.length() - 1);

			// Načteme data podle typu
			String data;
			if (type.isString()) {
				if (!isLastEntry) {
					// Typy s malými písmeny končí null terminátorem (kromě posledního záznamu)
					StringBuilder sb = new StringBuilder();
					byte b;
					while ((b = buffer.get()) != 0) {
						sb.append((char) (b & 0xFF));
					}
					data = new String(sb.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
				} else {
					// Poslední záznam - přečteme všechna zbývající data
					int remainingBytes = size - (buffer.position() - startPosition);
					byte[] dataBytes = new byte[remainingBytes];
					buffer.get(dataBytes);
					data = new String(dataBytes, StandardCharsets.UTF_8);
				}
			} else {
				if (!isLastEntry) {
					// Typy s velkými písmeny mají délku jako 4-bytové číslo (kromě posledního záznamu)
					int dataLength = buffer.getInt();
					byte[] dataBytes = new byte[dataLength];
					buffer.get(dataBytes);
					data = new String(dataBytes, StandardCharsets.UTF_8);
				} else {
					// Poslední záznam - přečteme všechna zbývající data
					int remainingBytes = size - (buffer.position() - startPosition);
					byte[] dataBytes = new byte[remainingBytes];
					buffer.get(dataBytes);
					data = new String(dataBytes, StandardCharsets.UTF_8);
				}
			}

			DefinitionEntry entry = new DefinitionEntry(type, data);
			wordDef.getDefinitions().add(entry);
		}

		return wordDef;
	}

	@Override public void close() throws Exception {
		is.close();
	}
}