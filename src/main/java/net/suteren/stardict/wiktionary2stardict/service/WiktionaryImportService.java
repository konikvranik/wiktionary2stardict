package net.suteren.stardict.wiktionary2stardict.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

	public WiktionaryImportService(WordDefinitionRepository repository) {
		this.repository = repository;
		this.mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

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
	 * This implementation avoids external HTML parsers and uses a conservative regex
	 * over anchor tags to capture immediate subdirectory links (languages).
	 */
	public List<String> listKaikkiLanguages() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(KAAKKI_DICTIONARY_ROOT)
			.header("User-Agent", "wiktionary2stardict/" + getClass().getPackage().getImplementationVersion())
			.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() != 200) {
			throw new IOException("Failed to load languages from " + KAAKKI_DICTIONARY_ROOT + ": HTTP " + response.statusCode());
		}
		String html = response.body();

		// Match <a href="something/">Label</a> and collect directory names without additional slashes.
		Pattern aTag = Pattern.compile("<a\\s+href=\\\"([^\\\"]+)\\\"[^>]*>([^<]*)</a>", Pattern.CASE_INSENSITIVE);
		Matcher m = aTag.matcher(html);
		Set<String> langs = new LinkedHashSet<>();
		while (m.find()) {
			String href = m.group(1).trim();
			String label = m.group(2).trim();
			if (href.isEmpty())
				continue;
			// Resolve relative to the dictionary root
			URI resolved;
			try {
				resolved = KAAKKI_DICTIONARY_ROOT.resolve(href);
			} catch (IllegalArgumentException e) {
				continue;
			}
			// We only want immediate children under /dictionary/<LANG>/
			String path = resolved.getPath();
			if (path == null)
				continue;
			// Expecting /dictionary/<LANG>/ or /dictionary/<LANG>
			String prefix = "/dictionary/";
			if (!path.startsWith(prefix))
				continue;
			String rest = path.substring(prefix.length());
			if (rest.isEmpty())
				continue; // root itself
			// Remove any leading slash
			if (rest.startsWith("/"))
				rest = rest.substring(1);
			// Accept single-segment optionally ending with slash
			if (rest.endsWith("/"))
				rest = rest.substring(0, rest.length() - 1);
			if (rest.contains("/"))
				continue; // deeper paths not languages list items
			String decoded;
			try {
				decoded = URLDecoder.decode(rest, StandardCharsets.UTF_8);
			} catch (IllegalArgumentException e) {
				decoded = rest; // fallback
			}
			// Prefer using visible label if it looks like a proper name matching the segment
			if (!label.isBlank() && (label.equalsIgnoreCase(decoded) || !label.contains("/"))) {
				langs.add(label);
			} else {
				langs.add(decoded);
			}
		}
		return List.copyOf(langs);
	}

	/**
	 * Downloads Kaikki JSONL files for the given language names and imports them into DB.
	 * Language handling follows the Python pattern: URL path uses the language verbatim, while
	 * the filename part strips spaces, hyphens and apostrophes.
	 */
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
		wordDefinitionEntity.setSenses(entry.getSenses().stream()
			.map(Sense::getSense)
			.map(SenseEntity::new)
			.toList()
		);
		wordDefinitionEntity.setSynonymums(
			Optional.of(entry)
				.map(WiktionaryEntry::getSynonyms)
				.stream()
				.flatMap(Collection::stream)
				.map(Synonym::getWord)
				.map(s -> new SynonymumEntity(s, language))
				.toList());
		wordDefinitionEntity.setJson(line);
		repository.save(wordDefinitionEntity);
		log.info("Imported {} from {}", entry.getWord(), file.getName());
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
