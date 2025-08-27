package net.suteren.stardict.wiktionary2stardict.stardict;

import lombok.Getter;

public enum DictType {
	WORDNET("wordnet");

	@Getter private final String type;

	DictType(String type) {
		this.type = type;
	}

	public static DictType resolve(String s) {
		for (DictType dictType : DictType.values()) {
			if (dictType.getType().equals(s)) {
				return dictType;
			}
		}
		return null;
	}
}
