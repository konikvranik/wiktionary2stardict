package net.suteren.stardict.wiktionary2stardict.cli;

import net.suteren.stardict.wiktionary2stardict.Version;
import net.suteren.stardict.wiktionary2stardict.service.StardictExportService;
import net.suteren.stardict.wiktionary2stardict.service.WiktionaryImportService;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(mixinStandardHelpOptions = true, description = "Wiktionary to Stardict converter CLI", versionProvider = Version.VersionProvider.class)
public class CliCommand implements Runnable {

    private final WiktionaryImportService importService;
    private final StardictExportService exportService;

    public CliCommand(WiktionaryImportService importService, StardictExportService exportService) {
        this.importService = importService;
        this.exportService = exportService;
    }

    @CommandLine.Option(names = { "-v", "--version" }, description = "Print version info")
    boolean versionRequested;

    @CommandLine.Option(names = { "-i", "--import-jsonl" }, description = "Path to Wiktionary JSONL file or directory")
    String importPath;

    @CommandLine.Option(names = { "-s", "--source" }, description = "Source label stored with imported records")
    String sourceLabel;

    @CommandLine.Option(names = { "-l", "--lang-code" }, description = "Language code filter (e.g., 'cs', 'en')")
    String langCode;

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
                System.out.println("wiktionary2stardict version 0.0.1-SNAPSHOT");
                return;
            }

            int imported = 0;
            if (importPath != null && !importPath.isBlank()) {
                imported = importService.importJsonlPath(importPath, langCode, sourceLabel);
                System.out.println("Imported entries: " + imported);
            }

            if (exportPrefix != null && !exportPrefix.isBlank()) {
                exportService.export(exportPrefix, sameTypeSequence, bookname, langCode);
                System.out.println("Exported StarDict files with prefix: " + exportPrefix);
            }

            if ((importPath == null || importPath.isBlank()) && (exportPrefix == null || exportPrefix.isBlank()) && !versionRequested) {
                System.out.println("No action requested. Use --import-jsonl and/or --export-stardict.");
            }
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
