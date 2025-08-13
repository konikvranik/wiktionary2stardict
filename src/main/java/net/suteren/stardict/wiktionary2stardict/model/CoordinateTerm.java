package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents words that share the same hypernym as the main entry.
 * These are terms at the same hierarchical level in a semantic taxonomy.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class CoordinateTerm {
    /** Unprocessed classification tags */
    private List<String> raw_tags;

    /** Romanized form of the coordinate term */
    private String roman;

    /** Specific meaning or context of this coordinate relationship */
    private String sense;

    /** Classification tags for this coordinate term */
    private List<String> tags;

    /** The coordinate term itself */
    private String word;
}
