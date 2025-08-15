package net.suteren.stardict.wiktionary2stardict.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.suteren.stardict.wiktionary2stardict.jpa.entity.LanguageCombinationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.TranslationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.repository.WordDefinitionRepository;
import net.suteren.stardict.wiktionary2stardict.model.Sense;
import net.suteren.stardict.wiktionary2stardict.model.Sound;
import net.suteren.stardict.wiktionary2stardict.model.WiktionaryEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.DictType;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.files.DefinitionEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.files.InfoFile;
import net.suteren.stardict.wiktionary2stardict.stardict.files.WordDefinition;
import net.suteren.stardict.wiktionary2stardict.stardict.io.DictFileWriter;
import net.suteren.stardict.wiktionary2stardict.stardict.io.IdxFileWriter;
import net.suteren.stardict.wiktionary2stardict.stardict.io.InfoFileWriter;

@Service
public class StardictExportService {

	private final WordDefinitionRepository repository;
	private final ObjectMapper mapper;

	public StardictExportService(WordDefinitionRepository repository) {
		this.repository = repository;
		this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public void export(String outputPrefix, String sametypesequence, String bookname) throws IOException {
		for (LanguageCombinationEntity lc : repository.findLanguageCombinations()) {
			export(outputPrefix, sametypesequence, bookname, lc.from(), lc.to());
		}
	}

	public void export(String outputPrefix, String sametypesequence, String bookname, String langCodeFrom, String langCodeTo) throws IOException {
		// Build definitions map sorted by word
		List<WordDefinition> definitions = new ArrayList<>();

		List<TranslationEntity> all = repository.findAllTranslations(langCodeFrom, langCodeTo);
		for (TranslationEntity e : all) {
			WiktionaryEntry entry;
			try {
				entry = mapper.readValue(e.definition().getJson(), WiktionaryEntry.class);
			} catch (Exception ex) {
				continue; // skip malformed stored entries
			}
			if (StringUtils.isBlank(e.source().getWord()))
				continue;
			definitions.add(constructWordDefinition(entry));
		}

		// Write dict -> idx entries
		List<IdxEntry> idxEntries = DictFileWriter.writeDictFile(outputPrefix + ".dict", definitions, sametypesequence);

		// Ensure idx order matches dict creation order (already insertion-order). For safety, sort both by word consistently
		List<IdxEntry> sortedIdx = idxEntries.stream()
			.sorted(Comparator.comparing(IdxEntry::word))
			.toList();
		// If sorted differs, we must rewrite dict accordingly. To keep minimal, rebuild dict if the original wasn't sorted
		boolean different = !sortedIdx.equals(idxEntries);
		if (different) {
			// Rebuild dict using sorted definitions
			definitions.sort(Comparator.naturalOrder());
			idxEntries = DictFileWriter.writeDictFile(outputPrefix + ".dict", definitions, sametypesequence);
		}

		// Write idx
		IdxFileWriter.writeIdxFile(outputPrefix + ".idx", idxEntries);

		// Build .ifo info
		int wordcount = idxEntries.size();
		int synwordcount = 0;
		int idxfilesize = idxEntries.stream()
			.mapToInt(e -> e.word().getBytes().length + 1 + 8)
			.sum();

		String usedBookname = bookname != null && !bookname.isBlank() ? bookname : new java.io.File(outputPrefix).getName();
		InfoFile info = new InfoFile(
			usedBookname,
			wordcount,
			synwordcount,
			idxfilesize,
			32,
			null,
			null,
			null,
			"Generated from Wiktionary JSONL",
			LocalDate.now(),
			null,
			DictType.WORDNET
		);
		InfoFileWriter.write(outputPrefix + ".ifo", info);
	}

	private static WordDefinition constructWordDefinition(WiktionaryEntry entry) {
		WordDefinition wd = new WordDefinition();
		wd.setWord(entry.getWord());

		List<DefinitionEntry> wordDefinitions = wd.getDefinitions();
		entry.getSenses().stream()
			.map(Sense::getSense)
			.filter(StringUtils::isNotBlank)
			.map(s -> new DefinitionEntry(EntryType.MEANING, s))
			.forEach(wordDefinitions::add);

		entry.getSounds().stream()
			.map(Sound::getIpa)
			.map(s -> new DefinitionEntry(EntryType.PHONETIC, s))
			.forEach(wordDefinitions::add);
		return wd;
	}
}
