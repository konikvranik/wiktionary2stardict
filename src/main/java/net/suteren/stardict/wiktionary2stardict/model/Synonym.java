package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a word with similar meaning to the main entry.
 * Contains words that can be used interchangeably in some contexts.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Synonym {
    /** Literal meaning of this synonym */
    private String literal_meaning;

    /** Unprocessed classification tags */
    private List<String> raw_tags;

    /** Romanized form of the synonym */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** Specific meaning or context where this word is synonymous */
    private String sense;

    /** Classification tags for this synonym */
    private List<String> tags;

    /** The synonym word itself */
    private String word;
}
