package net.suteren.stardict.wiktionary2stardict.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.suteren.stardict.wiktionary2stardict.jpa.entity.TranslationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.repository.WordDefinitionRepository;
import net.suteren.stardict.wiktionary2stardict.model.Sense;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class StardictExportService {

    private final WordDefinitionRepository repository;
    private final ObjectMapper mapper;

    public StardictExportService(WordDefinitionRepository repository) {
        this.repository = repository;
        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void export(String outputPrefix, String sametypesequence, String bookname, String langCodeFrom, String langCodeTo) throws IOException {
        // Build definitions map sorted by word
        Map<String, WordDefinition> definitions = new TreeMap<>();

        List<TranslationEntity> all = repository.findAllTranslations(langCodeFrom, langCodeTo);
        for (WordDefinitionEntity e : all) {
            if (langCodeFrom != null && !langCodeFrom.isBlank()) {
                if (e.getLanguage() == null || !langCodeFrom.equals(e.getLanguage())) continue;
            }
            WiktionaryEntry entry;
            try {
                entry = mapper.readValue(e.getJson(), WiktionaryEntry.class);
            } catch (Exception ex) {
                continue; // skip malformed stored entries
            }
            if (entry.getWord() == null || entry.getWord().isBlank()) continue;

            List<String> glosses = new ArrayList<>();
            if (entry.getSenses() != null) {
                for (Sense s : entry.getSenses()) {
                    if (s.getGlosses() != null && !s.getGlosses().isEmpty()) {
                        glosses.addAll(s.getGlosses());
                    } else if (s.getRawGlosses() != null && !s.getRawGlosses().isEmpty()) {
                        glosses.addAll(s.getRawGlosses());
                    }
                }
            }
            if (glosses.isEmpty()) continue; // skip words without glosses

            String meaning = String.join("; ", glosses);
            WordDefinition wd = new WordDefinition();
            wd.setWord(entry.getWord());
            wd.getDefinitions().add(new DefinitionEntry(EntryType.MEANING, meaning));
            definitions.put(entry.getWord(), wd);
        }

        // Write dict -> idx entries
        List<IdxEntry> idxEntries = DictFileWriter.writeDictFile(outputPrefix + ".dict", definitions, sametypesequence);

        // Ensure idx order matches dict creation order (already insertion-order). For safety, sort both by word consistently
        List<IdxEntry> sortedIdx = idxEntries.stream()
            .sorted(Comparator.comparing(IdxEntry::word))
            .collect(Collectors.toList());
        // If sorted differs, we must rewrite dict accordingly. To keep minimal, rebuild dict if the original wasn't sorted
        boolean different = !sortedIdx.equals(idxEntries);
        if (different) {
            // Rebuild dict using sorted definitions
            Map<String, WordDefinition> sortedDefs = new TreeMap<>(definitions);
            idxEntries = DictFileWriter.writeDictFile(outputPrefix + ".dict", sortedDefs, sametypesequence);
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
            Set.of(EntryType.MEANING),
            DictType.WORDNET
        );
        InfoFileWriter.write(outputPrefix + ".ifo", info);
    }
}
