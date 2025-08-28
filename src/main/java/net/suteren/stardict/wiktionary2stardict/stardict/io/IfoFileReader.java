package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.stardict.DictType;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IfoFile;

@RequiredArgsConstructor
public class IfoFileReader implements AutoCloseable {
	private final BufferedReader reader;

	public IfoFile readIfoFile() throws IOException {

		String line = reader.readLine();
		if ("StarDict's dict ifo file\n".equals(line)) {
			throw new IllegalStateException("Malformed IFO file.");
		}

		final AtomicReference<String> version = new AtomicReference<>();
		final AtomicReference<String> bookname = new AtomicReference<>();
		final AtomicInteger wordcount = new AtomicInteger();
		final AtomicInteger synwordcount = new AtomicInteger();
		final AtomicInteger idxfilesize = new AtomicInteger();
		final AtomicInteger idxoffsetbits = new AtomicInteger();
		final AtomicReference<String> author = new AtomicReference<>();
		final AtomicReference<String> email = new AtomicReference<>();
		final AtomicReference<String> website = new AtomicReference<>();
		final AtomicReference<String> description = new AtomicReference<>();
		final AtomicReference<LocalDate> date = new AtomicReference<>();
		final AtomicReference<Collection<EntryType>> sametypesequence = new AtomicReference<>();
		final AtomicReference<DictType> dicttype = new AtomicReference<>();

		while ((line = reader.readLine()) != null) {

			getValue(line, "version=").ifPresent(version::set);
			getValue(line, "bookname=").ifPresent(bookname::set);
			getValue(line, "wordcount=").map(Integer::parseInt).ifPresent(wordcount::set);
			getValue(line, "synwordcount=").map(Integer::parseInt).ifPresent(synwordcount::set);
			getValue(line, "idxfilesize=").map(Integer::parseInt).ifPresent(idxfilesize::set);
			getValue(line, "idxoffsetbits=").map(Integer::parseInt).ifPresent(idxoffsetbits::set);
			getValue(line, "author=").ifPresent(author::set);
			getValue(line, "email=").ifPresent(email::set);
			getValue(line, "website=").ifPresent(website::set);
			getValue(line, "description=").ifPresent(description::set);
			getValue(line, "date=").map(text -> tryToParseDate(text)).ifPresent(date::set);
			getValue(line, "sametypesequence=").map(String::toCharArray).map(EntryType::resolve).ifPresent(sametypesequence::set);
			getValue(line, "dicttype=").map(DictType::resolve).ifPresent(dicttype::set);

		}

		return new IfoFile(version.get(), bookname.get(), wordcount.get(), synwordcount.get(), idxfilesize.get(), idxoffsetbits.get(), author.get(),
			email.get(), website.get(), description.get(), date.get(),
			sametypesequence.get(), dicttype.get());

	}

	private static LocalDate tryToParseDate(String text) {
		try {
			return LocalDate.parse(text, DateTimeFormatter.BASIC_ISO_DATE);
		} catch (DateTimeParseException ignored) {
			try {
				return LocalDate.parse(text, DateTimeFormatter.ofPattern("y.M.d"));
			} catch (DateTimeParseException e) {
				return null;
			}
		}
	}

	private static Optional<String> getValue(String line, String prefix) {
		if (line.startsWith(prefix)) {
			return Optional.of(line.substring(prefix.length()));
		} else {
			return Optional.empty();
		}
	}

	@Override public void close() throws Exception {
		reader.close();
	}
}
