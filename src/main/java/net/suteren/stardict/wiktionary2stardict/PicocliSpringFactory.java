package net.suteren.stardict.wiktionary2stardict;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class PicocliSpringFactory implements CommandLine.IFactory, ApplicationContextAware {

    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        try {
            return beanFactory.createBean(cls);
        } catch (BeansException ex) {
            return cls.getDeclaredConstructor().newInstance();
        }
    }
}
