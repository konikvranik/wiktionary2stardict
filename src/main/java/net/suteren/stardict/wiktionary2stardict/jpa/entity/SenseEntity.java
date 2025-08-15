package net.suteren.stardict.wiktionary2stardict.jpa.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Table(name = "sense")
@Getter @Setter
@Entity public class SenseEntity {

	public SenseEntity(String word) {
		this.word = word;
	}

	@GeneratedValue(strategy = GenerationType.UUID) @Column(nullable = false)
	@Id private UUID id;
	@Column String word;
}