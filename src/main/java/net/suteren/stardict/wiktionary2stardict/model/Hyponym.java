package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a more specific term whose meaning is included in the main entry.
 * Used to express hierarchical relationships between words.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Hyponym {
    /** Unprocessed classification tags */
    private List<String> raw_tags;
    
    /** Romanized form of the hyponym */
    private String roman;
    
    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;
    
    /** Specific meaning or context of this hyponym relationship */
    private String sense;
    
    /** Classification tags for this hyponym */
    private List<String> tags;
    
    /** The hyponym word itself */
    private String word;
}
