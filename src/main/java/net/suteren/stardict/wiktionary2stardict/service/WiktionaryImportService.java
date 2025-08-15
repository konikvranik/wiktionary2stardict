package net.suteren.stardict.wiktionary2stardict.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.suteren.stardict.wiktionary2stardict.jpa.entity.SenseEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.SynonymumEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.repository.WordDefinitionRepository;
import net.suteren.stardict.wiktionary2stardict.model.Sense;
import net.suteren.stardict.wiktionary2stardict.model.Synonym;
import net.suteren.stardict.wiktionary2stardict.model.WiktionaryEntry;

@Service
public class WiktionaryImportService {

	private final WordDefinitionRepository repository;
	private final ObjectMapper mapper;

	public WiktionaryImportService(WordDefinitionRepository repository) {
		this.repository = repository;
		this.mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public int importJsonlPath(String path) throws IOException {
		int count = 0;
		File f = new File(path);
		if (f.isDirectory()) {
			count += importJsonlPath(f.getPath());
		} else if (f.isFile()) {
			count += importJsonlFile(f);
		} else {
			throw new IOException("Path not found: " + path);
		}
		return count;
	}

	public int importJsonlFile(File file) throws IOException {
		int saved = 0;
		if (file.getName().endsWith(".jsonl")) {
			String sourceLabel = file.getName().replaceAll("^kaikki.org-dictionary-(.*)\\.jsonl$", "$1");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty())
						continue;
					if (processEntry(file, sourceLabel, line, parseEntry(line)))
						continue;
					saved++;
				}
			}
		}
		return saved;
	}

	private boolean processEntry(File file, String sourceLabel, String line, WiktionaryEntry entry) {
		if (entry == null)
			return true;
		if (StringUtils.isBlank(entry.getWord()))
			return true;
		WordDefinitionEntity e = new WordDefinitionEntity();
		e.setSource(sourceLabel != null ? sourceLabel : file.getName());
		String language = Optional.of(entry)
			.map(WiktionaryEntry::getLang_code)
			.orElse(entry.getLang());
		e.setLanguage(language);
		e.setWord(entry.getWord());
		e.setSenses(entry.getSenses().stream()
			.map(Sense::getSense)
			.map(SenseEntity::new)
			.toList()
		);
		e.setSynonymums(entry.getSynonyms().stream()
			.map(Synonym::getWord)
			.map(s -> new SynonymumEntity(s, language))
			.toList());
		e.setJson(line);
		repository.save(e);
		return false;
	}

	private WiktionaryEntry parseEntry(String line) {
		WiktionaryEntry entry;
		try {
			entry = mapper.readValue(line, WiktionaryEntry.class);
		} catch (Exception e) {
			// skip malformed lines but continue
			return null;
		}
		return entry;
	}
}
