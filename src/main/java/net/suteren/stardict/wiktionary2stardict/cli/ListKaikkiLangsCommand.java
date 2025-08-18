package net.suteren.stardict.wiktionary2stardict.cli;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.service.WiktionaryImportService;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@CommandLine.Command(name = "listl", description = "List available languages from Kaikki and exit", mixinStandardHelpOptions = true)
@Component public class ListKaikkiLangsCommand implements Runnable {

	private final WiktionaryImportService importService;

	@Override public void run() {
		try {
			importService.listKaikkiLanguages()
				.forEach(l -> log.info("%s (%s)".formatted(l.getLeft(), l.getRight())));
		} catch (Exception ex) {
			log.error("Error listing Kaikki languages", ex);
			System.exit(1);
		}
	}
}
