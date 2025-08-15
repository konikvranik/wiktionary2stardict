package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a more general term that includes the meaning of the main entry.
 * Used to express hierarchical relationships between words.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Hypernym {
    /** Unprocessed classification tags */
    private List<String> raw_tags;
    
    /** Romanized form of the hypernym */
    private String roman;
    
    /** Specific meaning or context of this hypernym relationship */
    private String sense;
    
    /** Classification tags for this hypernym */
    private List<String> tags;
    
    /** The hypernym word itself */
    private String word;
}
