package net.suteren.stardict.wiktionary2stardict.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.repository.WordDefinitionRepository;
import net.suteren.stardict.wiktionary2stardict.model.WiktionaryEntry;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class WiktionaryImportService {

    private final WordDefinitionRepository repository;
    private final ObjectMapper mapper;

    public WiktionaryImportService(WordDefinitionRepository repository) {
        this.repository = repository;
        this.mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public int importJsonlPath(String path, String languageFilter, String sourceLabel) throws IOException {
        File f = new File(path);
        List<File> files = new ArrayList<>();
        if (f.isDirectory()) {
            File[] list = f.listFiles((dir, name) -> name.endsWith(".jsonl"));
            if (list != null) {
                for (File it : list) files.add(it);
            }
        } else if (f.isFile()) {
            files.add(f);
        } else {
            throw new IOException("Path not found: " + path);
        }

        int count = 0;
        for (File file : files) {
            count += importJsonlFile(file, languageFilter, sourceLabel);
        }
        return count;
    }

    public int importJsonlFile(File file, String languageFilter, String sourceLabel) throws IOException {
        int saved = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                WiktionaryEntry entry = null;
                try {
                    entry = mapper.readValue(line, WiktionaryEntry.class);
                } catch (Exception e) {
                    // skip malformed lines but continue
                    continue;
                }
                if (languageFilter != null && !languageFilter.isBlank()) {
                    if (!Objects.equals(languageFilter, entry.getLang_code()) && !Objects.equals(languageFilter, entry.getLang())) {
                        continue;
                    }
                }
                if (entry.getWord() == null || entry.getWord().isBlank()) continue;
                WordDefinitionEntity e = new WordDefinitionEntity();
                e.setSource(sourceLabel != null ? sourceLabel : file.getName());
                e.setLanguage(entry.getLang_code() != null ? entry.getLang_code() : entry.getLang());
                e.setWord(entry.getWord());
                e.setJson(line);
                repository.save(e);
                saved++;
            }
        }
        return saved;
    }
}
