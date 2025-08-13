package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a word with opposite meaning to the main entry.
 * Contains the antonym word along with its metadata and pronunciation guides.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Antonym {
    /** Unprocessed classification tags */
    private List<String> raw_tags;

    /** Romanized form of the antonym */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** The specific meaning or context in which this word is an antonym */
    private String sense;

    /** Classification tags for this antonym */
    private List<String> tags;

    /** The antonym word itself */
    private String word;
}
