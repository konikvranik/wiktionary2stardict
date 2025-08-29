package net.suteren.stardict.wiktionary2stardict;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.DefinitionEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.WordDefinition;
import net.suteren.stardict.wiktionary2stardict.stardict.io.DictFileReader;
import net.suteren.stardict.wiktionary2stardict.stardict.io.DictFileWriter;
import net.suteren.stardict.wiktionary2stardict.stardict.io.IdxFileReader;
import net.suteren.stardict.wiktionary2stardict.stardict.io.IdxFileWriter;
import net.suteren.stardict.wiktionary2stardict.stardict.io.SynFileReader;
import net.suteren.stardict.wiktionary2stardict.stardict.io.SynFileWriter;

/**
 * Testovací třída pro ověření implementace čtení a zápisu StarDict souborů
 */
public class StardictFileTest {

	/**
	 * Hlavní metoda pro spuštění testu
	 *
	 * @param args Argumenty příkazové řádky
	 */
	public static void main(String[] args) throws Exception {
		try {
			// Vytvoříme testovací data
			List<WordDefinition> testData = createTestData();

			// Zapíšeme testovací data do souborů
			String dictFilename = "test.dict";
			String idxFilename = "test.idx";
			String synFilename = "test.syn";
			Collection<EntryType> sameTypeSequence = Set.of(EntryType.MEANING, EntryType.PANGO, EntryType.HTML); // meaning, pango, html

			System.out.println("Zapisuji testovací data do souborů...");

			List<IdxEntry> idxEntries;
			// Zapíšeme .dict soubor a získáme .idx záznamy
			try (DictFileWriter dictFileWriter = new DictFileWriter(new FileOutputStream(dictFilename), DictFileWriter.Mode.ALL)) {
				for (WordDefinition wordDef : testData) {
					dictFileWriter.writeWordDefinition(wordDef);
				}
				idxEntries = dictFileWriter.getIdxEntries();
			}

			try (IdxFileWriter idxFileWriter = new IdxFileWriter(new FileOutputStream(idxFilename))) {
				// Zapíšeme .idx soubor

				for (IdxEntry entry : idxEntries) {
					idxFileWriter.writeEntry(entry);
				}
			}
			// Vytvoříme a zapíšeme synonyma
			List<SynonymumEntry> synEntries = createTestSynonyms(idxEntries);
			try (SynFileWriter synFileWriter = new SynFileWriter(new FileOutputStream(synFilename))) {
				for (SynonymumEntry entry : synEntries) {
					synFileWriter.writeEntry(entry);
				}
			}

			System.out.println("Zápis dokončen.");

			// Načteme data ze souborů
			System.out.println("Načítám data ze souborů...");

			// Načteme .idx soubor
			List<IdxEntry> loadedIdxEntries = new IdxFileReader(new FileInputStream(idxFilename), 32).readIdxFile();
			System.out.println("Načteno " + loadedIdxEntries.size() + " idx záznamů.");

			// Načteme .dict soubor
			Map<String, WordDefinition> loadedDictData =
				new DictFileReader(FileChannel.open(Path.of(dictFilename)), loadedIdxEntries, sameTypeSequence).readDictFile();
			System.out.println("Načteno " + loadedDictData.size() + " definic slov.");

			// Načteme .syn soubor
			SynFileReader synFileReader = new SynFileReader(new FileInputStream(synFilename));
			List<SynonymumEntry> loadedSynEntries = synFileReader.readSynFile();
			System.out.println("Načteno " + loadedSynEntries.size() + " synonym.");

			// Ověříme, že načtená data odpovídají původním datům
			boolean success = verifyData(testData, loadedDictData, synEntries, loadedSynEntries);

			if (success) {
				System.out.println("Test proběhl úspěšně! Načtená data odpovídají původním datům.");
			} else {
				System.out.println("Test selhal! Načtená data neodpovídají původním datům.");
			}

		} catch (IOException e) {
			System.err.println("Chyba při práci se soubory: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Vytvoří testovací data pro slovník
	 *
	 * @return Mapa slov a jejich definic
	 */
	private static List<WordDefinition> createTestData() {
		List<WordDefinition> data = new ArrayList<>();

		// Slovo 1
		WordDefinition word1 = new WordDefinition();
		word1.setWord("apple");

		List<DefinitionEntry> definitions1 = new ArrayList<>();
		definitions1.add(
			new DefinitionEntry(EntryType.MEANING, "A common, round fruit produced by the tree Malus domestica, cultivated in temperate climates.".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		definitions1.add(new DefinitionEntry(EntryType.PANGO,
			"A <b>common</b>, round fruit produced by the tree <i>Malus domestica</i>, cultivated in temperate climates.".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		definitions1.add(
			new DefinitionEntry(EntryType.HTML, "<div>A common, round fruit produced by the tree Malus domestica, cultivated in temperate climates.</div>".getBytes(java.nio.charset.StandardCharsets.UTF_8)));

		word1.setDefinitions(definitions1);
		data.add(word1);

		// Slovo 2
		WordDefinition word2 = new WordDefinition();
		word2.setWord("banana");

		List<DefinitionEntry> definitions2 = new ArrayList<>();
		definitions2.add(
			new DefinitionEntry(EntryType.MEANING, "An elongated curved tropical fruit that grows in bunches and has a creamy flesh and a smooth skin.".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		definitions2.add(
			new DefinitionEntry(EntryType.PANGO, "An <b>elongated curved</b> tropical fruit that grows in bunches and has a creamy flesh and a smooth skin.".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		definitions2.add(new DefinitionEntry(EntryType.HTML,
			"<div>An elongated curved tropical fruit that grows in bunches and has a creamy flesh and a smooth skin.</div>".getBytes(java.nio.charset.StandardCharsets.UTF_8)));

		word2.setDefinitions(definitions2);
		data.add(word2);

		// Slovo 3
		WordDefinition word3 = new WordDefinition();
		word3.setWord("orange");

		List<DefinitionEntry> definitions3 = new ArrayList<>();
		definitions3.add(new DefinitionEntry(EntryType.MEANING, "A round, reddish-yellow, acidic fruit of the citrus family.".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		definitions3.add(new DefinitionEntry(EntryType.PANGO, "A <b>round</b>, reddish-yellow, acidic fruit of the <i>citrus</i> family.".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		definitions3.add(new DefinitionEntry(EntryType.HTML, "<div>A round, reddish-yellow, acidic fruit of the citrus family.</div>".getBytes(java.nio.charset.StandardCharsets.UTF_8)));

		word3.setDefinitions(definitions3);
		data.add(word3);

		return data;
	}

	/**
	 * Vytvoří testovací synonyma
	 *
	 * @param idxEntries Seznam idx záznamů
	 * @return Seznam synonym
	 */
	private static List<SynonymumEntry> createTestSynonyms(List<IdxEntry> idxEntries) {
		List<SynonymumEntry> synonyms = new ArrayList<>();

		// Najdeme indexy slov v idx záznamech
		int appleIndex = -1;
		int bananaIndex = -1;
		int orangeIndex = -1;

		int i = 0;
		for (IdxEntry entry : idxEntries) {
			if ("apple".equals(entry.word())) {
				appleIndex = i;
			} else if ("banana".equals(entry.word())) {
				bananaIndex = i;
			} else if ("orange".equals(entry.word())) {
				orangeIndex = i;
			}
			i++;
		}

		// Přidáme synonyma
		if (appleIndex >= 0) {
			synonyms.add(new SynonymumEntry("fruit", appleIndex));
			synonyms.add(new SynonymumEntry("malus", appleIndex));
		}

		if (bananaIndex >= 0) {
			synonyms.add(new SynonymumEntry("fruit", bananaIndex));
			synonyms.add(new SynonymumEntry("plantain", bananaIndex));
		}

		if (orangeIndex >= 0) {
			synonyms.add(new SynonymumEntry("fruit", orangeIndex));
			synonyms.add(new SynonymumEntry("citrus", orangeIndex));
		}

		return synonyms;
	}

	/**
	 * Ověří, že načtená data odpovídají původním datům
	 *
	 * @param originalData Původní data
	 * @param loadedData Načtená data
	 * @param originalSynonyms Původní synonyma
	 * @param loadedSynonyms Načtená synonyma
	 * @return true pokud data odpovídají, jinak false
	 */
	private static boolean verifyData(List<WordDefinition> originalData, Map<String, WordDefinition> loadedData,
		List<SynonymumEntry> originalSynonyms, List<SynonymumEntry> loadedSynonyms) {

		// Ověříme počet záznamů
		if (originalData.size() != loadedData.size()) {
			System.out.println("Nesouhlasí počet definic slov: původní=" + originalData.size() + ", načtené=" + loadedData.size());
			return false;
		}

		if (originalSynonyms.size() != loadedSynonyms.size()) {
			System.out.println("Nesouhlasí počet synonym: původní=" + originalSynonyms.size() + ", načtené=" + loadedSynonyms.size());
			return false;
		}

		// Ověříme definice slov
		for (WordDefinition entry : originalData) {
			String word = entry.getWord();
			WordDefinition originalDef = entry;
			WordDefinition loadedDef = loadedData.get(word);

			if (loadedDef == null) {
				System.out.println("Chybí definice pro slovo: " + word);
				return false;
			}

			if (originalDef.getDefinitions().size() != loadedDef.getDefinitions().size()) {
				System.out.println("Nesouhlasí počet definic pro slovo " + word + ": původní=" +
					originalDef.getDefinitions().size() + ", načtené=" + loadedDef.getDefinitions().size());
				return false;
			}

			// Ověříme jednotlivé definice (zachováme pořadí pomocí iterátorů)
			java.util.Iterator<DefinitionEntry> itOrig = originalDef.getDefinitions().iterator();
			java.util.Iterator<DefinitionEntry> itLoad = loadedDef.getDefinitions().iterator();
			while (itOrig.hasNext() && itLoad.hasNext()) {
				DefinitionEntry originalEntry = itOrig.next();
				DefinitionEntry loadedEntry = itLoad.next();

				if (originalEntry.getType() != loadedEntry.getType()) {
					System.out.println("Nesouhlasí typ definice pro slovo " + word + ": původní=" +
						originalEntry.getType() + ", načtený=" + loadedEntry.getType());
					return false;
				}

				if (!java.util.Arrays.equals(originalEntry.getDefinition(), loadedEntry.getDefinition())) {
					System.out.println("Nesouhlasí obsah definice pro slovo " + word + ":");
					System.out.println("Původní: " + new String(originalEntry.getDefinition(), java.nio.charset.StandardCharsets.UTF_8));
					System.out.println("Načtený: " + new String(loadedEntry.getDefinition(), java.nio.charset.StandardCharsets.UTF_8));
					return false;
				}
			}
			if (itOrig.hasNext() || itLoad.hasNext()) {
				System.out.println("Nesouhlasí počet porovnaných definic u slova: " + word);
				return false;
			}
		}

		// Ověříme synonyma
		for (int i = 0; i < originalSynonyms.size(); i++) {
			SynonymumEntry originalSyn = originalSynonyms.get(i);

			// Najdeme odpovídající synonymum v načtených datech
			boolean found = false;
			for (SynonymumEntry loadedSyn : loadedSynonyms) {
				if (originalSyn.word().equals(loadedSyn.word()) && originalSyn.indexPosition() == loadedSyn.indexPosition()) {
					found = true;
					break;
				}
			}

			if (!found) {
				System.out.println("Chybí synonymum: " + originalSyn.word() + " -> " + originalSyn.indexPosition());
				return false;
			}
		}

		return true;
	}
}