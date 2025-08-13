package net.suteren.stardict.wiktionary2stardict.stardict.files;

public record IdxEntry(String word, int offset, int size) implements Comparable<IdxEntry> {
	@Override public int compareTo(IdxEntry o) {
		if (o == null) {
			return 1;
		}
		if (word == null) {
			return -1;
		}
		return word.compareTo(o.word);
	}
}
