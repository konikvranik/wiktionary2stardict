package net.suteren.stardict.wiktionary2stardict.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service public class StardictExportService {

	private final WordDefinitionRepository repository;
	private final ObjectMapper mapper;

	public StardictExportService(WordDefinitionRepository repository) {
		this.repository = repository;
		this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Transactional(readOnly = true)
	public void export(String outputPrefix, String bookname, String langCodeFrom, String langCodeTo) throws Exception {
		if (langCodeFrom != null && langCodeTo != null) {
			exportInternal(outputPrefix, bookname, langCodeFrom, langCodeTo);
		} else {
			List<LanguageCombinationEntity> languageCombinations;
			if (langCodeFrom != null) {
				languageCombinations = repository.findLanguageCombinations(langCodeFrom);
			} else if (langCodeTo != null) {
				languageCombinations = repository.findLanguageCombinations(langCodeTo);
			} else {
				languageCombinations = repository.findLanguageCombinations();
			}
			log.info("Exporting {} language combinations: {}", languageCombinations.size(), languageCombinations.stream()
				.map(lc -> lc.from() + "->" + lc.to())
				.collect(Collectors.joining(";")));

			for (LanguageCombinationEntity lc : languageCombinations) {
				exportInternal(outputPrefix, bookname, lc.from(), lc.to());
			}
		}
	}

	private void exportInternal(String outputPrefix, String bookname, String langCodeFrom, String langCodeTo) throws Exception {
		String baseName = "%s%s-%s".formatted(outputPrefix, langCodeFrom, langCodeTo);
		// Build definitions map sorted by word
		List<WordDefinition> definitions = new ArrayList<>();

		List<TranslationEntity> all = repository.findAllTranslations(langCodeFrom, langCodeTo);
		log.info("Found {} translations from {} to {}.", all.size(), langCodeFrom, langCodeTo);
		for (TranslationEntity e : all) {
			log.info("Processing {}", e.source().getWord());
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
		SortedSet<IdxEntry> sortedIdx;
		try (DictFileWriter dictFileWriter = new DictFileWriter(new FileOutputStream(baseName + ".dict"))) {
			// Write dict -> idx entries
			for (WordDefinition wordDef : definitions) {
				for (DefinitionEntry definitionEntry : wordDef.getDefinitions()) {
					dictFileWriter.writeEntry(wordDef.getWord(), definitionEntry);
				}
			}
			sortedIdx = dictFileWriter.getIdxEntries();
		}

		try (IdxFileWriter idxFileWriter = new IdxFileWriter(new FileOutputStream(baseName + ".idx"))) {
			for (IdxEntry entry : sortedIdx) {
				idxFileWriter.writeEntry(entry);
			}
		}

		// Build .ifo info
		int wordcount = sortedIdx.size();
		int synwordcount = 0;
		int idxfilesize = sortedIdx.stream()
			.mapToInt(e -> e.word().getBytes().length + 1 + 8)
			.sum();

		String usedBookname = bookname != null && !bookname.isBlank() ? bookname : new File(baseName).getName();
		try (InfoFileWriter infoFileWriter = new InfoFileWriter(new FileWriter(baseName + ".ifo"))) {
			infoFileWriter.write(
				new InfoFile(usedBookname, wordcount, synwordcount, idxfilesize, 32, null, null, null, "Generated from Wiktionary JSONL", LocalDate.now(), null,
					DictType.WORDNET));
		}
	}

	private static WordDefinition constructWordDefinition(WiktionaryEntry entry) {
		WordDefinition wd = new WordDefinition();
		wd.setWord(entry.getWord());

		List<DefinitionEntry> wordDefinitions = wd.getDefinitions();
		Optional.of(entry)
			.map(WiktionaryEntry::getSenses)
			.stream()
			.flatMap(Collection::stream)
			.map(Sense::getSense)
			.filter(StringUtils::isNotBlank)
			.map(s -> new DefinitionEntry(EntryType.MEANING, s))
			.forEach(wordDefinitions::add);

		Optional.of(entry)
			.map(WiktionaryEntry::getSounds)
			.stream()
			.flatMap(Collection::stream)
			.map(Sound::getIpa)
			.map(s -> new DefinitionEntry(EntryType.PHONETIC, s))
			.forEach(wordDefinitions::add);
		log.info("Have {} definitions for {} word {}", wordDefinitions.size(), entry.getWord(),
			Optional.ofNullable(entry.getLang_code()).orElse(entry.getLang()));
		return wd;
	}
}
