package net.suteren.stardict.wiktionary2stardict;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Wiktionary2stardictApplication implements ApplicationRunner, ExitCodeGenerator {

	public static void main(String[] args) {
		SpringApplication.run(Wiktionary2stardictApplication.class, args);
	}

	@Override public void run(ApplicationArguments args) throws Exception {

	}

	@Override public int getExitCode() {
		return 0;
	}
}
