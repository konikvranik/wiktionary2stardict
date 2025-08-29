package net.suteren.stardict.wiktionary2stardict.service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.converter.WiktionaryEntryRenderers;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.LanguageCombinationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.TranslationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.repository.WordDefinitionRepository;
import net.suteren.stardict.wiktionary2stardict.model.Sense;
import net.suteren.stardict.wiktionary2stardict.model.Sound;
import net.suteren.stardict.wiktionary2stardict.model.WiktionaryEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.DefinitionEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.WordDefinition;
import net.suteren.stardict.wiktionary2stardict.stardict.io.DictFileWriter;
import net.suteren.stardict.wiktionary2stardict.stardict.io.IdxFileWriter;
import net.suteren.stardict.wiktionary2stardict.stardict.io.IfoFileWriter;
import net.suteren.stardict.wiktionary2stardict.stardict.io.SynFileWriter;

@Slf4j
@Service public class StardictExportService {

	private final WordDefinitionRepository repository;
	private final ObjectMapper mapper;

	public StardictExportService(WordDefinitionRepository repository) {
		this.repository = repository;
		this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Transactional(readOnly = true)
	public void export(String outputPrefix, String bookname, String langCodeFrom, String langCodeTo, Collection<Character> definitionFormats) throws Exception {
		if (langCodeFrom != null && langCodeTo != null) {
			exportInternal(outputPrefix, bookname, langCodeFrom, langCodeTo, definitionFormats);
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
				exportInternal(outputPrefix, bookname, lc.from(), lc.to(), definitionFormats);
			}
		}
	}

	private void exportInternal(String outputPrefix, String bookname, String langCodeFrom, String langCodeTo, Collection<Character> definitionFormats)
		throws Exception {
		String baseName = "%s%s-%s".formatted(outputPrefix, langCodeFrom, langCodeTo);

		List<TranslationEntity> allTranslations = repository.findAllTranslations(langCodeFrom, langCodeTo);
		log.info("Found {} translations from {} to {}.", allTranslations.size(), langCodeFrom, langCodeTo);
		List<IdxEntry> sortedIdx;

		try (DictFileWriter dictFileWriter = new DictFileWriter(new BufferedOutputStream(new FileOutputStream("%s.dict".formatted(baseName))),
			DictFileWriter.Mode.ALL)) {

			sortedIdx = dictFileWriter.writeDefinitionFile(allTranslations.stream()
				.map(e -> constructWordDefinition(e, definitionFormats))
				.filter(Objects::nonNull));
		}
		Collections.sort(sortedIdx);
		List<SynonymumEntry> sortedSyn = IdxFileWriter.writeIdxFile(baseName, sortedIdx);
		Collections.sort(sortedSyn);
		SynFileWriter.writeSynFile(baseName, sortedSyn);
		IfoFileWriter.writeIfoFile(bookname, langCodeFrom, langCodeTo, sortedIdx, sortedSyn, baseName);
	}

	private WordDefinition constructWordDefinition(TranslationEntity e, Collection<Character> definitionFormats) {

		log.debug("Processing {}", e.source().getWord());
		WiktionaryEntry wiktionaryEntry = parseWiktionaryEntry(e);
		if (wiktionaryEntry == null) {return null;}
		WordDefinition wd = new WordDefinition();
		wd.setWord(e.source().getWord());

		wd.getDefinitions().add(new DefinitionEntry(EntryType.MEANING, e.definition().getWord().getBytes(StandardCharsets.UTF_8)));
		if (CollectionUtils.isEmpty(definitionFormats) || definitionFormats.contains('m')) {

			Optional.of(wiktionaryEntry)
				.map(WiktionaryEntry::getSenses)
				.stream()
				.flatMap(Collection::stream)
				.map(Sense::getSense)
				.filter(StringUtils::isNotBlank)
				.map(s -> new DefinitionEntry(EntryType.MEANING, s.getBytes(StandardCharsets.UTF_8)))
				.forEach(wd.getDefinitions()::add);

			Optional.of(wiktionaryEntry)
				.map(WiktionaryEntry::getSounds)
				.stream()
				.flatMap(Collection::stream)
				.map(Sound::getIpa)
				.filter(StringUtils::isNotBlank)
				.map(s -> new DefinitionEntry(EntryType.PHONETIC, s.strip()
					.replaceAll("^\\s*/?(.*[^/])/?$", "//$1//")
					.getBytes(StandardCharsets.UTF_8)))
				.forEach(wd.getDefinitions()::add);
		}

		if (CollectionUtils.isEmpty(definitionFormats) || definitionFormats.contains('h')) {
			// Render also HTML and XDXF representations for richer consumers
			try {
				String html = WiktionaryEntryRenderers.toHtml(wiktionaryEntry);
				if (StringUtils.isNotBlank(html)) {
					wd.getDefinitions().add(new DefinitionEntry(EntryType.HTML, html.getBytes(StandardCharsets.UTF_8)));
				}
			} catch (Exception ignore) {
				log.warn("Failed to render entry {}: {}", wiktionaryEntry.getWord(), ignore.getMessage());
				log.debug(ignore.getMessage(), ignore);
			}
		}

		if (CollectionUtils.isEmpty(definitionFormats) || definitionFormats.contains('x')) {
			try {
				String xdxf = WiktionaryEntryRenderers.toXdxf(wiktionaryEntry);
				if (StringUtils.isNotBlank(xdxf)) {
					wd.getDefinitions().add(new DefinitionEntry(EntryType.XDXF, xdxf.getBytes(StandardCharsets.UTF_8)));
				}
			} catch (Exception ignore) {
				log.warn("Failed to render entry {}: {}", wiktionaryEntry.getWord(), ignore.getMessage());
				log.debug(ignore.getMessage(), ignore);
			}
		}

		if (CollectionUtils.isEmpty(definitionFormats) || definitionFormats.contains('g')) {
			try {
				String pango = WiktionaryEntryRenderers.toPango(wiktionaryEntry);
				if (StringUtils.isNotBlank(pango)) {
					wd.getDefinitions().add(new DefinitionEntry(EntryType.PANGO, pango.getBytes(StandardCharsets.UTF_8)));
				}
			} catch (Exception ignore) {
				log.warn("Failed to render entry {}: {}", wiktionaryEntry.getWord(), ignore.getMessage());
				log.debug(ignore.getMessage(), ignore);
			}
		}

		Set<DefinitionEntry> uniqueDefinitions = new HashSet<>(wd.getDefinitions());
		wd.getDefinitions().clear();
		wd.getDefinitions().addAll(uniqueDefinitions);

		log.debug("Have {} definitions for {} word {}", wd.getDefinitions().size(), wiktionaryEntry.getWord(),
			Optional.ofNullable(wiktionaryEntry.getLang_code()).orElse(wiktionaryEntry.getLang()));

		return wd;
	}

	private WiktionaryEntry parseWiktionaryEntry(TranslationEntity e) {
		WiktionaryEntry entry;
		try {
			entry = mapper.readValue(e.definition().getJson(), WiktionaryEntry.class);
		} catch (Exception ex) {
			return null;
		}
		if (StringUtils.isBlank(e.source().getWord())) {
			return null;
		}
		return entry;
	}
}
