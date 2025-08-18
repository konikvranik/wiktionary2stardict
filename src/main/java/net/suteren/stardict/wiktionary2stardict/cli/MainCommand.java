package net.suteren.stardict.wiktionary2stardict.cli;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
	ListKaikkiLangsCommand.class, ImportCommand.class,
	ExportCommand.class }, description = "Wiktionary to Stardict converter CLI", versionProvider = Version.VersionProvider.class)
@Component public class MainCommand implements Runnable {

	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}
}
