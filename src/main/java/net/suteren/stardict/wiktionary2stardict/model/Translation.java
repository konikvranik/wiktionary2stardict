package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a translation of the main entry into another language.
 * Contains the translated word and associated metadata.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Translation {
    /** The language name of this translation */
    private String lang;

    /** ISO code of the translation's language */
    private String lang_code;

    /** Romanized form of the translation */
    private String roman;

    /** Specific meaning or context for this translation */
    private String sense;

    /** Classification tags for this translation */
    private List<String> tags;

    /** The translated word itself */
    private String word;
}
