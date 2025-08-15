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
	 * Převede definici slova na pole bytů pro uložení do .dict souboru
	 *
	 * @param sameTypeSequence Pokud je nastaveno, všechny záznamy mají stejnou sekvenci typů
	 * @return Pole bytů reprezentující definici slova
	 */
	public byte[] toBytes(String sameTypeSequence) {
		if (sameTypeSequence != null && !sameTypeSequence.isEmpty()) {
			return toBytesWithSameTypeSequence(sameTypeSequence);
		} else {
			return toBytesWithoutSameTypeSequence();
		}
	}

	/**
	 * Převede definici slova na pole bytů bez použití sameTypeSequence
	 */
	private byte[] toBytesWithoutSameTypeSequence() {
		// Nejprve spočítáme celkovou velikost
		int totalSize = 0;
		for (DefinitionEntry entry : definitions) {
			totalSize += 1; // typ
			byte[] data = entry.getDefinition().getBytes(StandardCharsets.UTF_8);

			// Pro typy s velkými písmeny (W, P, X) přidáme 4 byty pro délku
			if (Character.isUpperCase(entry.getType().getType())) {
				totalSize += 4; // délka
				totalSize += data.length;
			} else {
				totalSize += data.length + 1; // data + null terminátor
			}
		}

		ByteBuffer buffer = ByteBuffer.allocate(totalSize);
		buffer.order(ByteOrder.BIG_ENDIAN);

		for (DefinitionEntry entry : definitions) {
			buffer.put((byte) entry.getType().getType()); // typ
			byte[] data = entry.getDefinition().getBytes(StandardCharsets.UTF_8);

			if (Character.isUpperCase(entry.getType().getType())) {
				buffer.putInt(data.length); // délka
				buffer.put(data); // data
			} else {
				buffer.put(data); // data
				buffer.put((byte) 0); // null terminátor
			}
		}

		return buffer.array();
	}

	/**
	 * Převede definici slova na pole bytů s použitím sameTypeSequence
	 */
	private byte[] toBytesWithSameTypeSequence(String sameTypeSequence) {
		// Ověříme, že počet definic odpovídá délce sameTypeSequence
		if (definitions.size() != sameTypeSequence.length()) {
			throw new IllegalArgumentException("Počet definic neodpovídá délce sameTypeSequence");
		}

		// Nejprve spočítáme celkovou velikost
		int totalSize = 0;
		for (int i = 0; i < definitions.size(); i++) {
			DefinitionEntry entry = definitions.get(i);
			byte[] data = entry.getDefinition().getBytes(StandardCharsets.UTF_8);

			char typeChar = sameTypeSequence.charAt(i);
			boolean isLastEntry = (i == definitions.size() - 1);

			if (Character.isUpperCase(typeChar)) {
				if (!isLastEntry) {
					totalSize += 4; // délka (kromě posledního záznamu)
				}
				totalSize += data.length;
			} else {
				totalSize += data.length;
				if (!isLastEntry) {
					totalSize += 1; // null terminátor (kromě posledního záznamu)
				}
			}
		}

		ByteBuffer buffer = ByteBuffer.allocate(totalSize);
		buffer.order(ByteOrder.BIG_ENDIAN);

		for (int i = 0; i < definitions.size(); i++) {
			DefinitionEntry entry = definitions.get(i);
			byte[] data = entry.getDefinition().getBytes(StandardCharsets.UTF_8);

			char typeChar = sameTypeSequence.charAt(i);
			boolean isLastEntry = (i == definitions.size() - 1);

			if (Character.isUpperCase(typeChar)) {
				if (!isLastEntry) {
					buffer.putInt(data.length); // délka (kromě posledního záznamu)
				}
				buffer.put(data); // data
			} else {
				buffer.put(data); // data
				if (!isLastEntry) {
					buffer.put((byte) 0); // null terminátor (kromě posledního záznamu)
				}
			}
		}

		return buffer.array();
	}

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
