package net.suteren.stardict.wiktionary2stardict.stardict.domain;

import java.util.Objects;

import lombok.NonNull;

public record IdxEntry(String word, long offset, int size) implements Comparable<IdxEntry> {
	@Override public int compareTo(IdxEntry o) {
		if (o == null) {
			return 1;
		}
		return Objects.compare(word, o.word(), String::compareTo);
	}

	@Override public @NonNull String toString() {
		return "%s;%d;%d\n".formatted(word, offset, size);
	}
}
