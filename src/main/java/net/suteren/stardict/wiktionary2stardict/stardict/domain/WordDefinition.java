package net.suteren.stardict.wiktionary2stardict.stardict.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reprezentuje definici slova ve StarDict slovníku.
 * Obsahuje seznam definičních záznamů různých typů.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDefinition implements Comparable<WordDefinition> {
	private String word;
	@Builder.Default
	private List<DefinitionEntry> definitions = new ArrayList<>();

	@Override public int compareTo(WordDefinition o) {
		if (getWord() == null) {
			return -1;
		}
		if (o == null) {
			return 1;
		}
		return getWord().compareTo(o.getWord());
	}
}
