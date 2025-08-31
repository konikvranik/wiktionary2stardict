package net.suteren.stardict.wiktionary2stardict.cli;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.suteren.stardict.wiktionary2stardict.jpa.entity.TranslationEntity;
import net.suteren.stardict.wiktionary2stardict.service.ViewService;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@CommandLine.Command(name = "view", mixinStandardHelpOptions = true, description = "View definition.")
@Component public class ViewCommand implements Runnable {

	private final ViewService viewService;
	@CommandLine.Option(names = { "-f", "--lang-code-from" }, required = true, arity = "1", description = "Language code filter (e.g., 'cs', 'en')")
	String langCodeFrom;

	@CommandLine.Option(names = { "-t", "--lang-code-to" }, required = true, arity = "1", description = "Language code filter (e.g., 'cs', 'en')")
	String langCodeTo;

	@CommandLine.Option(names = { "-w", "--word" }, required = true, arity = "1", description = "Word to display")
	String word;

	@SneakyThrows
	@Override public void run() {
		List<TranslationEntity> translation = viewService.view(langCodeFrom, langCodeTo, word);
		translation.forEach(System.out::println);
	}
}
