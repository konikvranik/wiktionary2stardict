package net.suteren.stardict.wiktionary2stardict;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.suteren.stardict.wiktionary2stardict.cli.MainCommand;
import picocli.CommandLine;

@SpringBootApplication public class Wiktionary2stardictApplication implements ApplicationRunner, ExitCodeGenerator {

	private int exitCode = 0;

	@Autowired private MainCommand mainCommand;
	@Autowired private CommandLine.IFactory factory;

	public static void main(String[] args) {
		SpringApplication.run(Wiktionary2stardictApplication.class, args);
	}

	@Override public void run(ApplicationArguments args) {
		exitCode = new CommandLine(mainCommand, factory).execute(args.getSourceArgs());
	}

	@Override public int getExitCode() {
		return exitCode;
	}

	@RequiredArgsConstructor
	@Component private static class SpringBeanIFactory implements CommandLine.IFactory {

		private final ApplicationContext ctx;

		@Override public <K> K create(Class<K> cls) {
			K bean = ctx.getBeanProvider(cls).getIfAvailable();
			if (bean != null) {
				return bean;
			}
			return ctx.getAutowireCapableBeanFactory().createBean(cls);
		}
	}
}
