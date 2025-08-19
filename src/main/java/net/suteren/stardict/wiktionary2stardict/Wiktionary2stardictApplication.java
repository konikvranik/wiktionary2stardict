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
// ... existing code ...
import java.util.Arrays;
import java.util.stream.IntStream;
// ... existing code ...

@SpringBootApplication public class Wiktionary2stardictApplication implements ApplicationRunner, ExitCodeGenerator {

	private int exitCode = 0;

	@Autowired private MainCommand mainCommand;
	@Autowired private CommandLine.IFactory factory;

	public static void main(String[] args) {
		SpringApplication.run(Wiktionary2stardictApplication.class, args);
	}

	@Override public void run(ApplicationArguments args) {
		String[] src = args.getSourceArgs();

		int sepIdx = IntStream.range(0, src.length)
			.filter(i -> "--".equals(src[i]))
			.findFirst()
			.orElse(-1);

		String[] cliArgs;
		if (sepIdx >= 0) {
			cliArgs = Arrays.copyOfRange(src, sepIdx + 1, src.length);
		} else {
			cliArgs = Arrays.stream(src)
				.filter(a -> {
					String s = a.startsWith("-D") ? a.substring(2) : a;
					return !(s.startsWith("--spring.")
						|| s.startsWith("--management.")
						|| s.startsWith("--server.")
						|| s.equals("--"));
				})
				.toArray(String[]::new);
		}

		exitCode = new CommandLine(mainCommand, factory).execute(cliArgs);
	}

	@Override public int getExitCode() {
		return exitCode;
	}

	@RequiredArgsConstructor
	@Component private static class SpringBeanIFactory implements CommandLine.IFactory {

		private final ApplicationContext ctx;

		@Override public <K> K create(Class<K> cls) throws Exception {
			K bean = ctx.getBeanProvider(cls).getIfAvailable();
			if (bean != null) {
				return bean;
			}
			return CommandLine.defaultFactory().create(cls);
		}
	}
}