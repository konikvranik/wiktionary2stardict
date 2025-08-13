package net.suteren.stardict.wiktionary2stardict.cli;

import picocli.CommandLine;

@CommandLine.Command(mixinStandardHelpOptions = true, description = "Wiktionary to Stardict converter CLI") public class CliCommand
	implements Runnable {

	@CommandLine.Option(names = { "-v", "--version" }, description = "Print version info")
	boolean versionRequested;

	@Override public void run() {
		if (versionRequested) {
			System.out.println("wiktionary2stardict version 0.0.1-SNAPSHOT");
		} else {
			System.out.println("Run your conversion logic here.");
		}
	}
}
