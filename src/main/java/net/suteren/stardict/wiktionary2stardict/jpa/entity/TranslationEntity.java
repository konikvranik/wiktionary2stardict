package net.suteren.stardict.wiktionary2stardict.jpa.entity;

public record TranslationEntity(WordDefinitionEntity source, WordDefinitionEntity definition) {}