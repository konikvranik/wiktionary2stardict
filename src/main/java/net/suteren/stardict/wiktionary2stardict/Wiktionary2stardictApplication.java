package net.suteren.stardict.wiktionary2stardict;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.suteren.stardict.wiktionary2stardict.cli.CliCommand;
import picocli.CommandLine;

@SpringBootApplication
public class Wiktionary2stardictApplication implements ApplicationRunner, ExitCodeGenerator {

	private int exitCode = 0;

	public static void main(String[] args) {
		SpringApplication.run(Wiktionary2stardictApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		exitCode = new CommandLine(new CliCommand()).execute(args.getSourceArgs());
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}
}
