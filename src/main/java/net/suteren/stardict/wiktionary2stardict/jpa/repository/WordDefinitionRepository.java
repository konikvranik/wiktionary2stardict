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

	@Query("""
		select distinct d1, d2
		from WordDefinitionEntity d1
		join d1.links l1
		join WordDefinitionEntity d2
		where d1.language = :fromLang
		  and d2.language = :toLang
		  and d1.type = d2.type
		  and d1.type != 'name'
		  and l1.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
		  and exists (
		      select 1
		      from WordDefinitionLinkEntity l2
		      where l2.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
		        and l2.word = l1.word
		        and l2 member of d2.links
		  )
		""")
	List<TranslationEntity> findAllTranslations(String fromLang, String toLang);

	@Query("""
		select distinct e1, e2
		from WordDefinitionEntity e1
		join e1.links l1, WordDefinitionEntity e2
		join e2.links l2
		where e1.language = :fromLang
		  and e2.language = :toLang
		  and e1.type = e2.type
		  and e1.type != 'name'
		  and e1.word = :word
		  and l1.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
		  and l2.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
		  and l1.word = l2.word
		""")
	List<TranslationEntity> findTranslation(String fromLang, String toLang, String word);

	@Query("""
			select l1.language, l2.language
			from WordDefinitionLinkEntity l1
			join WordDefinitionLinkEntity l2
				on l1.word = l2.word
			where
				l1.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
				and l2.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
				and l1.language != l2.language
			group by l1.language, l2.language
		""")
	List<LanguageCombinationEntity> findLanguageCombinations();

	@Query("""
			select l1.language, l2.language
			from WordDefinitionLinkEntity l1
			join WordDefinitionLinkEntity l2
				on l1.word = l2.word
			where
				l1.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
				and l2.type = net.suteren.stardict.wiktionary2stardict.jpa.entity.LinkType.MEANING
				and l1.language = :language
				and l1.language != l2.language
			group by l1.language, l2.language
		""")
	List<LanguageCombinationEntity> findLanguageCombinations(String language);

	void deleteBySource(String source);

	long countBySource(String source);
}
