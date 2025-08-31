package net.suteren.stardict.wiktionary2stardict.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.TranslationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.repository.WordDefinitionRepository;

@Slf4j
@RequiredArgsConstructor
@Component public class ViewService {

	private final WordDefinitionRepository repository;

	@Transactional
	public List<TranslationEntity> view(String langCodeFrom, String langCodeTo, String word) {
		List<TranslationEntity> translation = repository.findTranslation(langCodeFrom, langCodeTo, word);
		log.info("Found {} {} translations for {} word: {}", translation.size(), langCodeTo, langCodeFrom, word);
		return translation;
	}
}
