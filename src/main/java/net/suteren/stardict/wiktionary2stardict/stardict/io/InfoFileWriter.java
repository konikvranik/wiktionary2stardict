package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.files.InfoFile;

/**
 * Writes StarDict .ifo file.
 */
@RequiredArgsConstructor
public class InfoFileWriter implements AutoCloseable {

	private final Writer writer;

	public void write(InfoFile info) throws IOException {
		// Required fields
		writer.write("StarDict's dict ifo file\n");
		writer.write("version=2.4.2\n");
		writer.write("bookname=" + sanitize(info.bookname()) + "\n");
		writer.write("wordcount=" + info.wordcount() + "\n");
		writer.write("synwordcount=" + info.synwordcount() + "\n");
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
			writer.write("date=" + info.date().format(DateTimeFormatter.BASIC_ISO_DATE) + "\n");
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
