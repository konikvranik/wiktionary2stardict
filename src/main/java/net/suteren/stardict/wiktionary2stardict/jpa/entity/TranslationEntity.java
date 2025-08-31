package net.suteren.stardict.wiktionary2stardict.jpa.entity;

import lombok.NonNull;

public record TranslationEntity(WordDefinitionEntity source, WordDefinitionEntity definition) {

	@Override public @NonNull String toString() {
		return """
			{
				"word": "%s",
				"language": "%s",
				"type": "%s",
				"definition": %s}
			"""
			.formatted(source.getWord(), source.getLanguage(), source.getType(), definition.getJson());
	}
}