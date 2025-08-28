package net.suteren.stardict.wiktionary2stardict.stardict.domain;

import java.time.LocalDate;
import java.util.Collection;

import net.suteren.stardict.wiktionary2stardict.stardict.DictType;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;

public record IfoFile(String version, String bookname, int wordcount, int synwordcount, int idxfilesize, int idxoffsetbits, String author, String email,
                      String website, String description, LocalDate date, Collection<EntryType> sametypesequence, DictType dicttype) {
	public IfoFile {
		if (version == null) {
			version = "2.4.2";
		}
		if (idxoffsetbits <= 0) {
			idxoffsetbits = 32;
		}
	}

	public IfoFile(String bookname, int wordcount, int synwordcount, int idxfilesize, int idxoffsetbits, String author, String email, String website,
		String description, LocalDate date, Collection<EntryType> sametypesequence, DictType dicttype) {
		this(null, bookname, wordcount, synwordcount, idxfilesize, idxoffsetbits, author, email, website, description, date, sametypesequence, dicttype);
	}
}
