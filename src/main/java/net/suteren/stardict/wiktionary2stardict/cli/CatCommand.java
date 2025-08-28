package net.suteren.stardict.wiktionary2stardict.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.service.StardictExportService;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.IfoFile;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.domain.WordDefinition;
import net.suteren.stardict.wiktionary2stardict.stardict.io.DictFileReader;
import net.suteren.stardict.wiktionary2stardict.stardict.io.IdxFileReader;
import net.suteren.stardict.wiktionary2stardict.stardict.io.IfoFileReader;
import net.suteren.stardict.wiktionary2stardict.stardict.io.SynFileReader;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@CommandLine.Command(name = "cat", mixinStandardHelpOptions = true, description = "List output.")
@Component public class CatCommand implements Runnable {

	private final StardictExportService exportService;

	@CommandLine.Parameters(index = "0", description = "Input file to print")
	Path inputFile;

	String exportPrefix;

	@SneakyThrows @Override
	public void run() {

		inputFile = inputFile.toAbsolutePath().normalize();

		Path ifoPath = getGetSiblingFile(inputFile, ".ifo");

		IfoFile ifo;
		try (IfoFileReader ifoReader = new IfoFileReader(new BufferedReader(new FileReader(ifoPath.toFile())))) {
			ifo = ifoReader.readIfoFile();
		}

		if (inputFile.getFileName().endsWith(".idx")) {
			try (IdxFileReader idxFileReader = new IdxFileReader(new FileInputStream(inputFile.toFile()), ifo.idxoffsetbits())) {
				List<IdxEntry> entries = idxFileReader.readIdxFile();
				entries.forEach(System.out::println);
				log.info("Displayed {} IDX entries.", entries.size());
			}
		} else if (inputFile.getFileName().endsWith(".syn")) {
			try (SynFileReader idxFileReader = new SynFileReader(new FileInputStream(inputFile.toFile()))) {
				List<SynonymumEntry> entries = idxFileReader.readSynFile();
				entries.forEach(System.out::println);
				log.info("Displayed {} SYN entries.", entries.size());
			}

		} else if (inputFile.getFileName().endsWith(".dict")) {
			Path idxPath = getGetSiblingFile(inputFile, ".idx");
			try (IdxFileReader idxFileReader = new IdxFileReader(new FileInputStream(idxPath.toFile()), ifo.idxoffsetbits());
				DictFileReader dictFileReader = new DictFileReader(FileChannel.open(inputFile), idxFileReader.readIdxFile(), ifo.sametypesequence())) {
				Map<String, WordDefinition> entries = dictFileReader.readDictFile();
			}
		}
	}

	private static Path getGetSiblingFile(Path originalFile, String ext) {
		int dot = originalFile.getFileName().toString().lastIndexOf('.');
		String base = (dot >= 0) ? originalFile.getFileName().toString().substring(0, dot) : originalFile.getFileName().toString();
		return originalFile.resolveSibling(base + ext);
	}
}
