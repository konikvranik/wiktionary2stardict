package net.suteren.stardict.wiktionary2stardict.cli;

import java.util.Collection;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.service.StardictExportService;
import net.suteren.stardict.wiktionary2stardict.stardict.EntryType;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@CommandLine.Command(name = "export", mixinStandardHelpOptions = true, description = "EXport to StarDict format.")
@Component public class ExportCommand implements Runnable {

	private final StardictExportService exportService;

	@CommandLine.Option(names = { "-lf", "--lang-code-from" }, description = "Language code filter (e.g., 'cs', 'en')")
	String langCodeFrom;

	@CommandLine.Option(names = { "-lt", "--lang-code-to" }, description = "Language code filter (e.g., 'cs', 'en')")
	String langCodeTo;

	@CommandLine.Option(names = { "-e", "--export-stardict" }, defaultValue = "dict_", description = "Output prefix for StarDict files (without extension)")
	String exportPrefix;

	@CommandLine.Option(names = { "-b", "--bookname" }, description = "Bookname for .ifo file")
	String bookname;

	@CommandLine.Option(names = { "-t", "--sametypesequence" }, description = "sameTypeSequence for StarDict (default: m)")
	String sameTypeSequence = "m";

	@CommandLine.Option(names = { "-f", "--definition-formats" }, description = "Definition formats")
	Collection<Character> definitionFormats;

	@SneakyThrows @Override
	public void run() {
		exportService.export(exportPrefix, bookname, langCodeFrom, langCodeTo, definitionFormats);
		log.info("Exported StarDict files with prefix: {}", exportPrefix);
	}
}
