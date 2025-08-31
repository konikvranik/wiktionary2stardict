package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.DictType;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IfoFile;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;

/**
 * Writes StarDict .ifo file.
 */
@RequiredArgsConstructor
public class IfoFileWriter implements AutoCloseable {

	private final Writer writer;

	public static void writeIfoFile(String bookname, String langCodeFrom, String langCodeTo, List<IdxEntry> sortedIdx, List<SynonymumEntry> sortedSyn,
		String baseName, int sizeInBits) throws Exception {
		int wordcount = sortedIdx.size();
		int synwordcount = sortedSyn.size();
		int idxfilesize = sortedIdx.stream()
			.mapToInt(e -> e.word().getBytes().length + 1 + 8)
			.sum();

		String usedBookname = bookname != null && !bookname.isBlank() ? bookname : "kaikki.org %s to %s dictionary".formatted(langCodeFrom, langCodeTo);
		try (IfoFileWriter ifoFileWriter = new IfoFileWriter(new FileWriter(baseName + ".ifo"))) {
			ifoFileWriter.write(
				new IfoFile(usedBookname, wordcount, synwordcount, idxfilesize, sizeInBits, null, null, null, "Generated from Wiktionary JSONL",
					LocalDate.now(), null, DictType.WORDNET));
		}
	}

	public void write(IfoFile info) throws IOException {
		// Required fields
		writer.write("StarDict's dict ifo file\n");
		writer.write("version=2.4.2\n");
		writer.write("bookname=" + sanitize(info.bookname()) + "\n");
		writer.write("wordcount=" + info.wordcount() + "\n");
		if (info.synwordcount() > 0) {
			writer.write("synwordcount=" + info.synwordcount() + "\n");
		}
		writer.write("idxfilesize=" + info.idxfilesize() + "\n");

		// Optional fields
		if (info.idxoffsetbits() > 0) {
			writer.write("idxoffsetbits=" + info.idxoffsetbits() + "\n");
		}
		if (info.description() != null && !info.description().isEmpty()) {
			writer.write("description=" + sanitize(info.description()) + "\n");
		}
		if (info.author() != null && !info.author().isEmpty()) {
			writer.write("author=" + sanitize(info.author()) + "\n");
		}
		if (info.email() != null && !info.email().isEmpty()) {
			writer.write("email=" + info.email() + "\n");
		}
		if (info.website() != null && !info.website().isEmpty()) {
			writer.write("website=" + info.website() + "\n");
		}
		if (info.date() != null) {
			writer.write("date=" + info.date().format(DateTimeFormatter.ofPattern("y.M.d")) + "\n");
		}
		if (info.sametypesequence() != null && !info.sametypesequence().isEmpty()) {
			writer.write("sametypesequence=" + joinSameTypeSequence(info.sametypesequence()) + "\n");
		}
		if (info.dicttype() != null) {
			writer.write("dicttype=" + info.dicttype().getType() + "\n");
		}
	}

	private static String joinSameTypeSequence(Collection<EntryType> sts) {
		return sts.stream().map(e -> String.valueOf(e.getType())).collect(Collectors.joining());
	}

	private static String sanitize(String value) {
		// Replace newlines to keep .ifo one-line values
		return value.replace('\n', ' ').replace('\r', ' ').trim();
	}

	@Override public void close() throws Exception {
		writer.close();
	}
}
