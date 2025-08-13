package net.suteren.stardict.wiktionary2stardict.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains pronunciation information and audio files for the main entry.
 * Includes various audio formats and phonetic transcriptions.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Sound {
	/** Audio file name */
	private String audio;

	/** URL to FLAC audio file */
	private String flac_url;

	/** The specific form being pronounced */
	private String form;

	/** List of words with identical pronunciation */
	private List<String> homophones;

	/** International Phonetic Alphabet transcription */
	private String ipa;

	/** URL to MP3 audio file */
	private String mp3_url;

	/** URL to OGA audio file */
	private String oga_url;

	/** URL to OGG audio file */
	private String ogg_url;

	/** URL to OPUS audio file */
	private String opus_url;

	/** Unprocessed classification tags */
	private List<String> raw_tags;

	/** Romanized pronunciation */
	private String roman;

	/** Context or meaning for this pronunciation */
	private String sense;

	/** Classification tags for this pronunciation */
	private List<String> tags;

	/** URL to WAV audio file */
	private String wav_url;

	/** Chinese-specific pronunciation information */
	private String zh_pron;
}
