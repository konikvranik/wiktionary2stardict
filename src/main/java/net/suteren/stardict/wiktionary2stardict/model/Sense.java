package net.suteren.stardict.wiktionary2stardict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Represents a specific meaning or definition of a word.
 * Contains detailed semantic information and relationships to other words.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Sense {
    /** Processed definitions of this sense */
    private List<String> glosses;

    /** Unprocessed definitions */
    private List<String> rawGlosses;

    /** The specific meaning or context */
    private String sense;

    /** Classification tags for this sense */
    private List<String> tags;

    /** Topical domains this sense belongs to */
    private List<String> topics;

    /** Usage examples for this sense */
    private List<Example> examples;

    /** Words with similar meaning */
    private List<Linkage> synonyms;

    /** Words with opposite meaning */
    private List<Linkage> antonyms;

    /** More general terms that include this meaning */
    private List<Linkage> hypernyms;

    /** More specific terms included in this meaning */
    private List<Linkage> hyponyms;

    /** Terms representing wholes of which this is a part */
    private List<Linkage> holonyms;

    /** Terms representing parts of this whole */
    private List<Linkage> meronyms;

    /** Terms at the same hierarchical level */
    private List<Linkage> coordinateTerms;

    /** Alternative forms this word is a variant of */
    private List<Linkage> altOf;

    /** Base words this is a form of */
    private List<Linkage> formOf;

}
