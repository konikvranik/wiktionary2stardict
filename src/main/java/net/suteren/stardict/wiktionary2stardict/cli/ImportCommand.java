package net.suteren.stardict.wiktionary2stardict.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.service.StardictExportService;
import net.suteren.stardict.wiktionary2stardict.service.WiktionaryImportService;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@CommandLine.Command(name = "import", mixinStandardHelpOptions = true, description = "Import vocabulaties from Kaikki")
@Component public class ImportCommand implements Runnable {

	private final WiktionaryImportService importService;
	private final StardictExportService exportService;

	@CommandLine.Option(names = { "-i", "--import-jsonl" }, split = ",", description = "Path to Wiktionary JSONL file or directory")
	Set<String> importPath;

	@CommandLine.Option(names = { "-d",
		"--download-langs" }, split = ",", description = "Comma-separated Kaikki language names to download and import (e.g., 'Czech,Italian,Serbo-Croatian')")
	String[] downloadLangs;

	@CommandLine.Option(names = { "-c", "--cleanup" }, arity = "*", split = ",", description = "Comma-separated sources to delete")
	Set<String> cleanupSources;

	@Override
	public void run() {
		try {
			int imported = 0;

			if (downloadLangs != null) {
				importService.cleanupEntries(cleanupSources);
			}

			if (downloadLangs != null && downloadLangs.length > 0) {
				List<String> langs = Arrays.stream(downloadLangs).toList();
				int dlImported = importService.downloadAndImportLanguages(langs);
				imported += dlImported;
				log.info("Downloaded and imported entries: {} (total imported: {})", dlImported, imported);
			}

			if (importPath != null && !importPath.isEmpty()) {
				for (String path : importPath) {
					imported += importService.importJsonlPath(path);
					log.info("Imported entries: {}", imported);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred during CLI execution", ex);
			System.exit(1);
		}
	}
}
