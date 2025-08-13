package net.suteren.stardict.wiktionary2stardict.stardict;

import lombok.Getter;

public enum EntryType {
	MEANING('m'), LOCALIZED_MEANING('l'), PANGO('g'), ENGLISH_PHONETIC('t'), XDXF('x'), YIN_BIAO('y'), KINGSOFT('k'),
	MEDIAWIKI('w'), HTML('h'), WORDNET('n'), RESOURCES('r'), WAW('W'), PICTURE('P'), EXPERIMENTAL('X'), PHONETIC('p');

	@Getter private final char type;

	EntryType(char type) {
		this.type = type;
	}
}
