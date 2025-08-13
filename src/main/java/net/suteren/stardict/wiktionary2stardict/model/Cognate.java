package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a cognate word - a word in another language that shares the same etymological origin.
 * Contains information about the related word and its descendants.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Cognate {
    /** Words that evolved from this cognate */
    private List<Descendant> descendants;

    /** The language of the cognate word */
    private String lang;

    /** ISO code of the cognate's language */
    private String lang_code;

    /** Unprocessed classification tags */
    private List<String> raw_tags;

    /** Romanized form of the cognate */
    private String roman;

    /** The specific meaning or context of this cognate */
    private String sense;

    /** Classification tags for this cognate */
    private List<String> tags;

    /** The cognate word itself */
    private String word;
}
