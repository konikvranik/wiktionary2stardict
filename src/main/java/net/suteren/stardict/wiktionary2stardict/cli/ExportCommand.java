package net.suteren.stardict.wiktionary2stardict.cli;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.service.StardictExportService;
import net.suteren.stardict.wiktionary2stardict.service.WiktionaryImportService;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@CommandLine.Command(name = "export", mixinStandardHelpOptions = true, description = "EXport to StarDict format.")
@Component public class ExportCommand implements Runnable {

	private final WiktionaryImportService importService;
	private final StardictExportService exportService;

	@CommandLine.Option(names = { "-v", "--version" }, description = "Print version info")
	boolean versionRequested;

	@CommandLine.Option(names = { "-i", "--import-jsonl" }, description = "Path to Wiktionary JSONL file or directory")
	String importPath;

	@CommandLine.Option(names = { "-d",
		"--download-langs" }, split = ",", description = "Comma-separated Kaikki language names to download and import (e.g., 'Czech,Italian,Serbo-Croatian')")
	String[] downloadLangs;

	@CommandLine.Option(names = { "-lf", "--lang-code-from" }, description = "Language code filter (e.g., 'cs', 'en')")
	String langCodeFrom;

	@CommandLine.Option(names = { "-lt", "--lang-code-to" }, description = "Language code filter (e.g., 'cs', 'en')")
	String langCodeTo;

	@CommandLine.Option(names = { "-e", "--export-stardict" }, description = "Output prefix for StarDict files (without extension)")
	String exportPrefix;

	@CommandLine.Option(names = { "-b", "--bookname" }, description = "Bookname for .ifo file")
	String bookname;

	@CommandLine.Option(names = { "-t", "--sametypesequence" }, description = "sameTypeSequence for StarDict (default: m)")
	String sameTypeSequence = "m";

	@Override
	public void run() {
		try {
			if (versionRequested) {
				log.info("wiktionary2stardict version 0.0.1-SNAPSHOT");
				return;
			}

			if (exportPrefix != null && !exportPrefix.isBlank()) {
				exportService.export(exportPrefix, sameTypeSequence, bookname, langCodeFrom, langCodeTo);
				log.info("Exported StarDict files with prefix: {}", exportPrefix);
			}

			if ((downloadLangs == null || downloadLangs.length == 0) && (importPath == null || importPath.isBlank()) && (exportPrefix == null
				|| exportPrefix.isBlank()) && !versionRequested) {
				log.info("No action requested. Use 'list-kaikki-langs' subcommand and/or --download-langs and/or --import-jsonl and/or --export-stardict.");
			}
		} catch (Exception ex) {
			log.error("Error occurred during CLI execution", ex);
			System.exit(1);
		}
	}
}
