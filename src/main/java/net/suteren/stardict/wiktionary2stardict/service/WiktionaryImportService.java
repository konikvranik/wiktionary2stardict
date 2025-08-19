package net.suteren.stardict.wiktionary2stardict.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.SenseEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.SynonymumEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.repository.WordDefinitionRepository;
import net.suteren.stardict.wiktionary2stardict.model.Sense;
import net.suteren.stardict.wiktionary2stardict.model.Synonym;
import net.suteren.stardict.wiktionary2stardict.model.WiktionaryEntry;

@Slf4j
@Service public class WiktionaryImportService {

	private final WordDefinitionRepository repository;
	private final ObjectMapper mapper;
	private final HttpClient httpClient = HttpClient.newHttpClient();

	private static final URI KAAKKI_DICTIONARY_ROOT = URI.create("https://kaikki.org/dictionary/");
	private static final Pattern LANG_PATTERN = Pattern.compile("(.*\\S)\\s*\\(\\s*\\d+\\s*(?:senses\\s*)?\\)\\s*$");
	private static final Pattern LANG_URL_PATTERN = Pattern.compile("^/dictionary(/[^/]+(/(?:index.html)?)?)$");

	public WiktionaryImportService(WordDefinitionRepository repository) {
		this.repository = repository;
		this.mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Transactional
	public int importJsonlPath(String path) throws IOException {
		int count = 0;
		File f = new File(path);
		if (f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null) {
				for (File file : list) {
					if (file.isDirectory()) {
						count += importJsonlPath(file.getPath());
					} else if (file.isFile()) {
						count += importJsonlFile(file);
					}
				}
			}
		} else if (f.isFile()) {
			count += importJsonlFile(f);
		} else {
			throw new IOException("Path not found: " + path);
		}
		return count;
	}

	@Transactional
	public long cleanupEntries(Collection<String> source) {
		long count;
		if (source == null || source.isEmpty()) {
			count = repository.count();
			repository.deleteAll();
		} else {
			count = source.stream()
				.mapToLong(s -> {
					long c = repository.countBySource(s);
					repository.deleteBySource(s);
					return c;
				})
				.sum();
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

	/**
	 * Fetches available languages from https://kaikki.org/dictionary/ and returns a list.
	 * Reimplemented to use Jsoup for robust HTML parsing instead of regex.
	 */
	public Set<Pair<String, String>> listKaikkiLanguages() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(KAAKKI_DICTIONARY_ROOT)
			.header("User-Agent", "wiktionary2stardict/" + getClass().getPackage().getImplementationVersion())
			.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() != 200) {
			throw new IOException("Failed to load languages from " + KAAKKI_DICTIONARY_ROOT + ": HTTP " + response.statusCode());
		}
		String html = response.body();

		// Parse with Jsoup using the dictionary root as the base URL
		Document doc = Jsoup.parse(html, KAAKKI_DICTIONARY_ROOT.toString());
		Elements links = doc.select("li a[href]");
		Set<org.apache.commons.lang3.tuple.Pair<String, String>> langs = new LinkedHashSet<>();

		for (Element a : links) {
			String absHref = a.attr("abs:href");
			Matcher m = LANG_PATTERN.matcher(a.text());
			String lang = null;
			if (m.matches()) {
				lang = m.group(1);
			}
			String rest = getHref(absHref);
			if (rest == null)
				continue;

			langs.add(Pair.of(rest, lang));
		}
		return langs;
	}

	private static String getHref(String absHref) {
		if (absHref.isBlank())
			return null;
		try {
			return Optional.ofNullable(URI.create(absHref).getPath())
				.map(LANG_URL_PATTERN::matcher)
				.filter(Matcher::matches)
				.map(m -> m.group(1))
				.orElse(null);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	/**
	 * Downloads Kaikki JSONL files for the given language names and imports them into DB.
	 * Language handling follows the Python pattern: URL path uses the language verbatim, while
	 * the filename part strips spaces, hyphens and apostrophes.
	 */
	@Transactional
	public int downloadAndImportLanguages(List<String> languages) throws IOException, InterruptedException {
		if (languages == null || languages.isEmpty())
			return 0;
		int totalImported = 0;
		File tempDir = Files.createTempDirectory("kaikki-jsonl-").toFile();
		tempDir.deleteOnExit();
		for (String language : languages) {
			if (StringUtils.isBlank(language))
				continue;
			String langTrim = language.trim();
			String langNoSpaces = langTrim.replace(" ", "").replace("-", "").replace("'", "");
			String fileName = "kaikki.org-dictionary-" + langNoSpaces + ".jsonl";
			URI uri;
			log.info("Downloading {} to {}", language, tempDir.getAbsolutePath());
			try {
				//"https://kaikki.org/dictionary/{self.source_language}/kaikki.org-dictionary-{lang_nospaces}.jsonl"
				uri = new URI(
					"https",
					"kaikki.org",
					"/dictionary/" + langTrim + "/" + fileName,
					null
				);
			} catch (Exception e) {
				throw new IOException("Invalid language for URL: " + langTrim, e);
			}
			HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.header("User-Agent", "wiktionary2stardict/" + getClass().getPackage().getImplementationVersion())
				.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (response.statusCode() != 200) {
				throw new IOException("Failed to download from " + uri + ": HTTP " + response.statusCode());
			}
			File out = new File(tempDir, fileName);
			try (FileWriter fw = new FileWriter(out, StandardCharsets.UTF_8)) {
				fw.write(response.body());
			}
			log.info("Downloaded {} from {}", language, uri);
			int imported = importJsonlFile(out);
			totalImported += imported;
		}
		return totalImported;
	}

	private boolean processEntry(File file, String sourceLabel, String line, WiktionaryEntry entry) {
		if (entry == null)
			return true;
		if (StringUtils.isBlank(entry.getWord()))
			return true;
		WordDefinitionEntity wordDefinitionEntity = new WordDefinitionEntity();
		wordDefinitionEntity.setSource(sourceLabel != null ? sourceLabel : file.getName());
		String language = Optional.of(entry)
			.map(WiktionaryEntry::getLang_code)
			.orElse(entry.getLang());
		wordDefinitionEntity.setLanguage(language);
		wordDefinitionEntity.setWord(entry.getWord());
		wordDefinitionEntity.setType(entry.getPos());
		wordDefinitionEntity.setSenses(extractSenses(entry)
		);
		wordDefinitionEntity.setSynonymums(
			Optional.of(entry)
				.map(WiktionaryEntry::getSynonyms)
				.stream()
				.flatMap(Collection::stream)
				.map(Synonym::getWord)
				.filter(Objects::nonNull)
				.map(s -> new SynonymumEntity(s, language))
				.collect(Collectors.toSet()));
		wordDefinitionEntity.setJson(line);
		if (wordDefinitionEntity.getWord() != null && (!CollectionUtils.isEmpty(wordDefinitionEntity.getSenses()) || !CollectionUtils.isEmpty(
			wordDefinitionEntity.getSynonymums())))
			repository.save(wordDefinitionEntity);
		log.info("Imported {} from {}", entry.getWord(), file.getName());
		return false;
	}

	private static Set<SenseEntity> extractSenses(WiktionaryEntry entry) {
		try {
			return entry.getSenses().stream()
				.map(Sense::getGlosses)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(SenseEntity::new)
				.collect(Collectors.toSet());
		} catch (NullPointerException e) {
			throw e;
		}
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
