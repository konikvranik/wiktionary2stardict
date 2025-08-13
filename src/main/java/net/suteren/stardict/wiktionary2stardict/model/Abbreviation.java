package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an abbreviation or acronym form of the word.
 * Contains the abbreviated form along with its romanization and ruby text
 * for languages that use non-Latin scripts.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Abbreviation {
    /** Romanized form of the abbreviation */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** The specific meaning or context of this abbreviation */
    private String sense;

    /** Classification tags for this abbreviation */
    private List<String> tags;

    /** The abbreviated form itself */
    private String word;
}
