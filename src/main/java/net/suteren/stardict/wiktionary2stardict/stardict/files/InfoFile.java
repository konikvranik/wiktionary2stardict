package net.suteren.stardict.wiktionary2stardict.stardict.files;

import java.time.LocalDate;
import java.util.Collection;

import net.suteren.stardict.wiktionary2stardict.stardict.DictType;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;

public record InfoFile(String bookname, int wordcount, int synwordcount, int idxfilesize, int idxoffsetbits, String author, String email, String website,
                       String description, LocalDate date, Collection<EntryType> sametypesequence, DictType dicttype) {
}
