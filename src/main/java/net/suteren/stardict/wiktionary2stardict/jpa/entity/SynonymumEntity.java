package net.suteren.stardict.wiktionary2stardict.jpa.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "synonymum", indexes = { @Index(columnList = "word", name = "idx_word"), @Index(columnList = "language", name = "idx_language") })
@Getter @Setter @NoArgsConstructor
@Entity public class SynonymumEntity {

	@GeneratedValue(strategy = GenerationType.UUID) @Column(nullable = false)
	@Id private UUID id;

	@Column String language;
	@Column String word;

	public SynonymumEntity(String s, String lang) {
		word = s;
		language = lang;
	}
}