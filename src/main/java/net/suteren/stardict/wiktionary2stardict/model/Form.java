package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a different form of the main entry word.
 * Includes inflected forms, spelling variants, and other morphological variations.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Form {
	/** The form itself */
	private String form;

	/** Literal meaning of this form */
	private String literal_meaning;

	/** Unprocessed classification tags */
	private List<String> raw_tags;

	/** Romanized form */
	private String roman;

	/** Classification tags for this form */
	private List<String> tags;
}
