package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a common phrase or expression containing the main entry.
 * Includes idiomatic expressions and common combinations.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Phrase {
    /** Romanized form of the phrase */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** Specific meaning or context of this phrase */
    private String sense;

    /** Classification tags for this phrase */
    private List<String> tags;

    /** The phrase itself */
    private String word;
}
