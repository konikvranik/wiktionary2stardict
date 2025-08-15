package net.suteren.stardict.wiktionary2stardict;

import net.suteren.stardict.wiktionary2stardict.cli.CliCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
public class Wiktionary2stardictApplication implements ApplicationRunner, ExitCodeGenerator {

	private int exitCode = 0;

	@Autowired
	private PicocliSpringFactory picocliSpringFactory;

	@Autowired
	private CliCommand cliCommand;

	public static void main(String[] args) {
		SpringApplication.run(Wiktionary2stardictApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		if (isTestEnvironment()) {
			return;
		}
		exitCode = new CommandLine(cliCommand, picocliSpringFactory).execute(args.getSourceArgs());
	}

	private boolean isTestEnvironment() {
		String cmd = System.getProperty("sun.java.command", "");
		String classpath = System.getProperty("java.class.path", "");
		return cmd.contains("org.junit") || classpath.contains("junit") || classpath.contains("surefire");
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}
}
