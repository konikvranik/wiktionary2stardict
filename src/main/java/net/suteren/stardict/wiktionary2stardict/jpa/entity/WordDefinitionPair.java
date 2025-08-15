package net.suteren.stardict.wiktionary2stardict.jpa.entity;

import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionEntity;

public record WordDefinitionPair(WordDefinitionEntity left, WordDefinitionEntity right) {}