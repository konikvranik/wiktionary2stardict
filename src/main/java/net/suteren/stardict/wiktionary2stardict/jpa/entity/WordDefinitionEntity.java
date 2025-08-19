package net.suteren.stardict.wiktionary2stardict.jpa.entity;

import java.util.Collection;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "definition", indexes = { @Index(columnList = "word", name = "idx_definition_word"), @Index(columnList = "language", name = "idx_definition_language"),
	@Index(columnList = "source", name = "idx_definition_source"), @Index(columnList = "type", name = "idx_definition_type") })
@Getter @Setter
@Entity public class WordDefinitionEntity {

	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false)
	@Id private UUID id;

	@Column private String source;
	@Column private String language;
	@Column private String word;
	@Column private String type;

	@Lob
	@Column private String json;

	@JoinColumn(name = "word_definition_id", nullable = false, foreignKey = @ForeignKey(name = "fk_link_word_definition"))
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) private Collection<WordDefinitionLinkEntity> links;
}
