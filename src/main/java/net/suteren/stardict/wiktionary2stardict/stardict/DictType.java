package net.suteren.stardict.wiktionary2stardict.stardict;

import lombok.Getter;

public enum DictType {
	WORDNET("wordnet");

	@Getter private final String type;

	DictType(String type) {
		this.type = type;
	}
}
