package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a part of the whole denoted by the main entry.
 * Used to express part-whole relationships.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Meronym {
    /** Unprocessed classification tags */
    private List<String> raw_tags;
    
    /** Romanized form of the meronym */
    private String roman;
    
    /** Ruby text for pronunciation guidance */
    private List<List<String>> ruby;
    
    /** Specific meaning or context of this meronym relationship */
    private String sense;
    
    /** Classification tags for this meronym */
    private List<String> tags;
    
    /** The meronym word itself */
    private String word;
}
