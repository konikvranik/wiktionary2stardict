package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a word that is derived from the main entry through various word-formation processes.
 * Contains information about the derived word and its relationship to the main entry.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Derived {
    /** Unprocessed classification tags */
    private List<String> raw_tags;

    /** Romanized form of the derived word */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** The specific meaning or context of this derived word */
    private String sense;

    /** Classification tags for this derived word */
    private List<String> tags;

    /** The derived word itself */
    private String word;
}
