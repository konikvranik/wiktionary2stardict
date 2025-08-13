package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an example usage of a word in context.
 * Contains the example text, its translation, and formatting information.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Example {
    /** Offsets for bold text in romanized form */
    private List<List<String>> bold_roman_offsets;

    /** Offsets for bold text in original script */
    private List<List<String>> bold_text_offsets;

    /** Offsets for bold text in translation */
    private List<List<String>> bold_translation_offsets;

    /** Reference or source of the example */
    private String ref;

    /** Romanized form of the example */
    private String roman;

    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;

    /** The example text in original script */
    private String text;

    /** Translation of the example */
    private String translation;

}
