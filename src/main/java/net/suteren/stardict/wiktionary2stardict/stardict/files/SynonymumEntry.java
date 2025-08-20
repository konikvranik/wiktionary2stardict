package net.suteren.stardict.wiktionary2stardict.stardict.files;

import java.util.Objects;

public record SynonymumEntry(String word, int indexPosition) implements Comparable<SynonymumEntry> {
	@Override public int compareTo(SynonymumEntry o) {
		if (o == null) {
			return 1;
		}
		return Objects.compare(word, o.word, String::compareTo);
	}
}
