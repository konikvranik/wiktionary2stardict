package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a semantic link between words in the dictionary.
 * Contains various forms and metadata about the linked word.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Linkage {
    /** The linked word itself */
    private String word;
    
    /** Alternative form of the word */
    private String alt;
    
    /** English translation or equivalent */
    private String english;
    
    /** Romanized form of the word */
    private String roman;
    
    /** Specific meaning or context of this linkage */
    private String sense;
    
    /** Taxonomic classification if applicable */
    private String taxonomic;
    
    /** Classification tags for this linkage */
    private List<String> tags;
    
    /** Topical domains this linkage belongs to */
    private List<String> topics;
    
    /** Unprocessed classification tags */
    private List<String> rawTags;

}
