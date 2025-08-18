package net.suteren.stardict.wiktionary2stardict.jpa.entity;

import java.util.Collection;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "definition")
@Getter @Setter
@Entity public class WordDefinitionEntity {

	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false)
	@Id private UUID id;

	@Column private String source;
	@Column private String language;
	@Column private String word;
	@Lob
	@Column(columnDefinition = "CLOB") private String json;
	@ManyToMany(cascade = CascadeType.ALL) private Collection<SynonymumEntity> synonymums;
	@ManyToMany(cascade = CascadeType.ALL) private Collection<SenseEntity> senses;
}
