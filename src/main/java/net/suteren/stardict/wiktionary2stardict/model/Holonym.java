package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a whole of which the main entry is a part.
 * Used to express part-whole relationships.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Holonym {
    /** Unprocessed classification tags */
    private List<String> raw_tags;
    
    /** Romanized form of the holonym */
    private String roman;
    
    /** Classification tags for this holonym */
    private List<String> tags;
    
    /** The holonym word itself */
    private String word;
}
