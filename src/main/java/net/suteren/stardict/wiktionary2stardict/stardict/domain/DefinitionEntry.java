package net.suteren.stardict.wiktionary2stardict.stardict.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reprezentuje jeden záznam definice ve StarDict slovníku.
 * Každý záznam má svůj typ a obsah.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = { "type", "definition" })
public class DefinitionEntry {
	private EntryType type;
	private byte[] definition;

	@Override public String toString() {
		return "%s;%s".formatted(type.getType(), definition == null ? "<null>" : new String(definition, UTF_8));
	}

}
