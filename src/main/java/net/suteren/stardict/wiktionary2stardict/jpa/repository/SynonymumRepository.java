package net.suteren.stardict.wiktionary2stardict.jpa.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.suteren.stardict.wiktionary2stardict.jpa.entity.SynonymumEntity;

@Repository public interface SynonymumRepository extends JpaRepository<SynonymumEntity, UUID> {
}
