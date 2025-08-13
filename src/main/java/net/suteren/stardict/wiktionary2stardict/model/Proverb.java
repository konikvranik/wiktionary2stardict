package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a proverb containing or related to the main entry.
 * Includes traditional sayings and wisdom.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Proverb {
    /** Romanized form of the proverb */
    private String roman;

    /** Specific meaning or context of this proverb */
    private String sense;

    /** Classification tags for this proverb */
    private List<String> tags;

    /** The proverb itself */
    private String word;
}
