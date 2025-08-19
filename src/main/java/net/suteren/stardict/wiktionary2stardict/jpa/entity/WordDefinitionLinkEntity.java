package net.suteren.stardict.wiktionary2stardict.jpa.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Table(name = "link", indexes = { @Index(columnList = "word", name = "idx_link_word"), @Index(columnList = "type", name = "idx_link_type"),
	@Index(columnList = "language", name = "idx_link_language") })
@Getter @Setter
@Entity public class WordDefinitionLinkEntity {

	public WordDefinitionLinkEntity(String word, String language, LinkType type) {
		this.word = word;
		this.language = language;
		this.type = type;
	}

	@GeneratedValue(strategy = GenerationType.UUID) @Column(nullable = false)
	@Id private UUID id;
	@Column(length = 1024) String word;
	@Column String language;
	@Enumerated(EnumType.ORDINAL)
	@Column LinkType type;
}
