package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.files.InfoFile;

/**
 * Writes StarDict .ifo file.
 */
public class InfoFileWriter {

    public static void write(String filename, InfoFile info) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8))) {
            // Required fields
            bw.write("StarDict's dict ifo file\n");
            bw.write("version=2.4.2\n");
            bw.write("bookname=" + sanitize(info.bookname()) + "\n");
            bw.write("wordcount=" + info.wordcount() + "\n");
            bw.write("synwordcount=" + info.synwordcount() + "\n");
            bw.write("idxfilesize=" + info.idxfilesize() + "\n");

            // Optional fields
            if (info.idxoffsetbits() == 64) {
                bw.write("idxoffsetbits=64\n");
            }
            if (info.description() != null && !info.description().isEmpty()) {
                bw.write("description=" + sanitize(info.description()) + "\n");
            }
            if (info.author() != null && !info.author().isEmpty()) {
                bw.write("author=" + sanitize(info.author()) + "\n");
            }
            if (info.email() != null && !info.email().isEmpty()) {
                bw.write("email=" + info.email() + "\n");
            }
            if (info.website() != null && !info.website().isEmpty()) {
                bw.write("website=" + info.website() + "\n");
            }
            if (info.date() != null) {
                bw.write("date=" + info.date().format(DateTimeFormatter.BASIC_ISO_DATE) + "\n");
            }
            if (info.sametypesequence() != null && !info.sametypesequence().isEmpty()) {
                bw.write("sametypesequence=" + joinSameTypeSequence(info.sametypesequence()) + "\n");
            }
            if (info.dicttype() != null) {
                bw.write("dicttype=" + info.dicttype().getType() + "\n");
            }
        }
    }

    private static String joinSameTypeSequence(Collection<EntryType> sts) {
        return sts.stream().map(e -> String.valueOf(e.getType())).collect(Collectors.joining());
    }

    private static String sanitize(String value) {
        // Replace newlines to keep .ifo one-line values
        return value.replace('\n', ' ').replace('\r', ' ').trim();
    }
}
