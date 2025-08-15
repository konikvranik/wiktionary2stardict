package net.suteren.stardict.wiktionary2stardict.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionEntity;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.WordDefinitionPair;

@Repository public interface WordDefinitionRepository extends JpaRepository<WordDefinitionEntity, UUID> {

	@Query("select e1, e2 from WordDefinitionEntity e1, WordDefinitionEntity e2 where e1.language = :fromLang and e2.language == :toLang and exists (select s1 from e1.senses s1 where s1.word in (select s2.word from e2.senses s2))")
	List<WordDefinitionPair> findAllTranslations(String fromLang, String toLAng);

	@Query("select e1, e2 from WordDefinitionEntity e1, WordDefinitionEntity e2 where e1.language = :fromLang and e2.language == :toLang and e1.word = :word and exists (select s1 from e1.senses s1 where s1.word in (select s2.word from e2.senses s2))")
	List<WordDefinitionPair> findTranslation(String fromLang, String toLAng, String word);

}
