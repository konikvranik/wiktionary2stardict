package net.suteren.stardict.wiktionary2stardict.stardict;

import java.time.LocalDate;

public record DictInfo(String bookname, int wordcount, int synwordcount, int idxfilesize, int idxoffsetbits, String author, String email, String website,
                       String description, LocalDate date, SequenceType sametypesequence, DictType dicttype) {
}
