package net.suteren.stardict.wiktionary2stardict.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Links an inflected or variant form back to its base word.
 * Used to connect different forms of the same word.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class FormOf {
    /** The base word that this is a form of */
    private String word;
}
