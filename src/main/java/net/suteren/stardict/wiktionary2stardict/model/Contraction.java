package net.suteren.stardict.wiktionary2stardict.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a shortened or contracted form of a word or phrase.
 * Contains information about the shortened version of the main entry.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Contraction {
    /** The contracted form of the word */
    private String word;
}
