package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a word that is semantically related to the main entry.
 * Includes words with related meanings not covered by other relationship types.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Related {
    /** Unprocessed classification tags */
    private List<String> raw_tags;

    /** Romanized form of the related word */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** Specific meaning or context of this relationship */
    private String sense;

    /** Classification tags for this related word */
    private List<String> tags;

    /** The related word itself */
    private String word;
}
