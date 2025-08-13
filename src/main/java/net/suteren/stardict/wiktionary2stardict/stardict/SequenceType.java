package net.suteren.stardict.wiktionary2stardict.stardict;

import lombok.Getter;

public enum SequenceType {
	WAW('W'), PHONETIC('p'), MEANING('m');

	@Getter	private final char type;

	SequenceType(char type) {
		this.type = type;
	}
}
