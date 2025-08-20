package net.suteren.stardict.wiktionary2stardict.stardict.files;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;

/**
 * Reprezentuje definici slova ve StarDict slovníku.
 * Obsahuje seznam definičních záznamů různých typů.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDefinition implements Comparable<WordDefinition> {
	private String word;
	@Builder.Default
	private List<DefinitionEntry> definitions = new ArrayList<>();


	/**
	 * Vytvoří WordDefinition z bytového bufferu
	 *
	 * @param buffer ByteBuffer obsahující data
	 * @param size Velikost dat
	 * @param sameTypeSequence Pokud je nastaveno, všechny záznamy mají stejnou sekvenci typů
	 * @return WordDefinition objekt
	 */
	public static WordDefinition fromBytes(ByteBuffer buffer, int size, String sameTypeSequence) {
		WordDefinition wordDef = new WordDefinition();

		if (sameTypeSequence != null && !sameTypeSequence.isEmpty()) {
			return fromBytesWithSameTypeSequence(buffer, size, sameTypeSequence);
		} else {
			return fromBytesWithoutSameTypeSequence(buffer, size);
		}
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
			EntryType type = getEntryTypeFromChar(typeChar);

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
	private static WordDefinition fromBytesWithSameTypeSequence(ByteBuffer buffer, int size, String sameTypeSequence) {
		WordDefinition wordDef = new WordDefinition();
		int startPosition = buffer.position();

		for (int i = 0; i < sameTypeSequence.length(); i++) {
			char typeChar = sameTypeSequence.charAt(i);
			EntryType type = getEntryTypeFromChar(typeChar);
			boolean isLastEntry = (i == sameTypeSequence.length() - 1);

			// Načteme data podle typu
			String data;
			if (Character.isUpperCase(typeChar)) {
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
			} else {
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
			}

			DefinitionEntry entry = new DefinitionEntry(type, data);
			wordDef.getDefinitions().add(entry);
		}

		return wordDef;
	}

	/**
	 * Získá EntryType z char hodnoty
	 */
	private static EntryType getEntryTypeFromChar(char typeChar) {
		for (EntryType type : EntryType.values()) {
			if (type.getType() == typeChar) {
				return type;
			}
		}
		throw new IllegalArgumentException("Neznámý typ záznamu: " + typeChar);
	}

	@Override public int compareTo(WordDefinition o) {
		if (getWord() == null) {
			return -1;
		}
		if (o == null) {
			return 1;
		}
		return getWord().compareTo(o.getWord());
	}
}
