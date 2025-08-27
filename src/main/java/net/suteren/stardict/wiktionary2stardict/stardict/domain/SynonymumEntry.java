package net.suteren.stardict.wiktionary2stardict.stardict.domain;

import java.util.Objects;

import lombok.NonNull;

public record SynonymumEntry(String word, int indexPosition) implements Comparable<SynonymumEntry> {
	@Override public int compareTo(SynonymumEntry o) {
		if (o == null) {
			return 1;
		}
		return Objects.compare(word, o.word, String::compareTo);
	}

	@Override public @NonNull String toString() {
		return "%s;%d\n".formatted(word, indexPosition);
	}
}
