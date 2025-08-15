package net.suteren.stardict.wiktionary2stardict;

import net.suteren.stardict.wiktionary2stardict.cli.CliCommand;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext ctx = app.run(args);

        try {
            CliCommand top = ctx.getBean(CliCommand.class);
            int exitCode = new CommandLine(top, ctx.getBeanFactory().getBean(PicocliSpringFactory.class)).execute(args);
            System.exit(exitCode);
        } finally {
            ctx.close();
        }
    }
}
