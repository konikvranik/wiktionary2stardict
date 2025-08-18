package net.suteren.stardict.wiktionary2stardict.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.suteren.stardict.wiktionary2stardict.jpa.entity.LanguageCombinationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.TranslationEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionEntity;

@Repository public interface WordDefinitionRepository extends JpaRepository<WordDefinitionEntity, UUID> {

	@Query("select e1, e2 from WordDefinitionEntity e1, WordDefinitionEntity e2 where e1.language = :fromLang and e2.language = :toLang and exists (select s1 from e1.senses s1 where s1.word in (select s2.word from e2.senses s2))")
	List<TranslationEntity> findAllTranslations(String fromLang, String toLAng);

	@Query("select e1, e2 from WordDefinitionEntity e1, WordDefinitionEntity e2 where e1.language = :fromLang and e2.language = :toLang and e1.word = :word and exists (select s1 from e1.senses s1 where s1.word in (select s2.word from e2.senses s2))")
	List<TranslationEntity> findTranslation(String fromLang, String toLAng, String word);

	@Query("select e1.language, e2.language, count(e1.language) from WordDefinitionEntity e1, WordDefinitionEntity e2 where e1.language != e2.language and exists (select s1 from e1.senses s1 where s1.word in (select s2.word from e2.senses s2)) group by e1.language, e2.language")
	List<LanguageCombinationEntity> findLanguageCombinations();

	void deleteBySource(String source);

	long countBySource(String source);
}
