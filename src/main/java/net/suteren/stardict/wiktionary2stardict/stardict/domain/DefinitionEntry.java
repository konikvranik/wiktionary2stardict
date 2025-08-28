package net.suteren.stardict.wiktionary2stardict.stardict.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;

/**
 * Reprezentuje jeden záznam definice ve StarDict slovníku.
 * Každý záznam má svůj typ a obsah.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = { "type", "definition" })
public class DefinitionEntry {
	private EntryType type;
	private String definition;

	@Override public String toString() {
		return "%s;%s".formatted(type.getType(), definition);
	}

}
