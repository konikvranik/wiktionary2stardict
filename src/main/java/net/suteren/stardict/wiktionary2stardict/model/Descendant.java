package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a word that evolved from the main entry in another language.
 * Supports recursive nesting to show complete etymological evolution chains.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Descendant {
    /** Further evolved forms of this descendant */
    private List<Descendant> descendants;

    /** The language of this descendant */
    private String lang;

    /** ISO code of the descendant's language */
    private String lang_code;

    /** Romanized form of the descendant */
    private String roman;

    /** The specific meaning or context of this descendant */
    private String sense;

    /** Classification tags for this descendant */
    private List<String> tags;

    /** The descendant word itself */
    private String word;
}
