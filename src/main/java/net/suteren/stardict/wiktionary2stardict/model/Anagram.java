package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an anagram of the word - a word formed by rearranging its letters.
 * Includes the anagram word and its romanization for non-Latin scripts.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Anagram {
    /** Romanized form of the anagram */
    private String roman;

    /** Classification tags for this anagram */
    private List<String> tags;

    /** The anagram word itself */
    private String word;
}
