package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a group of words that commonly appear together with the main entry.
 * Contains the collocation phrase and its usage information.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Collocation {
    /** Romanized form of the collocation */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** The specific meaning or context of this collocation */
    private String sense;

    /** Classification tags for this collocation */
    private List<String> tags;

    /** The collocation phrase itself */
    private String word;
}
