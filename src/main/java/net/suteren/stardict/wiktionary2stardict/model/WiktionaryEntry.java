package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Root class representing a complete Wiktionary entry.
 * Contains all information about a word including its definitions,
 * etymology, related words, translations, and other linguistic metadata.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class WiktionaryEntry {
    /** List of abbreviations derived from or related to this word */
    private List<Abbreviation> abbreviations;

    /** List of words that can be formed by rearranging the letters of this word */
    private List<Anagram> anagrams;

    /** List of words with opposite meanings */
    private List<Antonym> antonyms;

    /** Categories this word belongs to */
    private List<String> categories;

    /** Words in other languages that share the same etymological origin */
    private List<Cognate> cognates;

    /** Common word combinations involving this word */
    private List<Collocation> collocations;

    /** Shortened forms of this word */
    private List<Contraction> contraction;

    /** Words at the same level in a semantic hierarchy */
    private List<CoordinateTerm> coordinate_terms;

    /** Words derived from this word */
    private List<Derived> derived;

    /** Words that evolved from this word in other languages */
    private List<Descendant> descendants;

    /** Etymology information explaining the word's origin */
    private List<String> etymology_texts;

    /** Different forms of the word (e.g., plural, past tense) */
    private List<Form> forms;

    /** Words representing wholes of which this word is a part */
    private List<Holonym> holonyms;

    /** More general terms that include this word's meaning */
    private List<Hypernym> hypernyms;

    /** More specific terms included in this word's meaning */
    private List<Hyponym> hyponyms;

    /** The language of this word entry */
    private String lang;

    /** ISO code of the language */
    private String lang_code;

    /** Parts that make up the whole denoted by this word */
    private List<Meronym> meronyms;

    /** Additional information about usage or meaning */
    private List<String> notes;

    /** Common phrases containing this word */
    private List<Phrase> phrases;

    /** Part of speech (e.g., noun, verb) */
    private String pos;

    /** Title for the part of speech section */
    private String pos_title;

    /** List of proverbs containing this word */
    private List<Proverb> proverbs;

    /** Unprocessed classification tags */
    private List<String> raw_tags;

    /** Words that are semantically related */
    private List<Related> related;

    /** Different meanings or definitions of the word */
    private List<Sense> senses;

    /** Pronunciation information including audio files */
    private List<Sound> sounds;

    /** Words with similar meanings */
    private List<Synonym> synonyms;

    /** Classification tags */
    private List<String> tags;

    /** Translations into other languages */
    private List<Translation> translations;

    /** The word itself */
    private String word;
}