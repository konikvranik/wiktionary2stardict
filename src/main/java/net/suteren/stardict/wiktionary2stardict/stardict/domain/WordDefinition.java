package net.suteren.stardict.wiktionary2stardict.stardict.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reprezentuje definici slova ve StarDict slovníku.
 * Obsahuje seznam definičních záznamů různých typů.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WordDefinition implements Comparable<WordDefinition> {
	private String word;
	@Builder.Default
	private Collection<DefinitionEntry> definitions = new ArrayList<>();

	@Override public String toString() {
		return definitions.stream()
			.map(DefinitionEntry::toString)
			.map(s -> "%s: %s".formatted(word, s))
			.collect(Collectors.joining("\n"));
	}

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
