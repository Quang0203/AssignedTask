package cviettel.loginservice.configuration.message;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
//@ConfigurationProperties(prefix = "spring.messages")
@Setter
public class MessageConfiguration {

    @Value("${spring.messages.encoding}")
    private String encoding;

    @Value("${spring.messages.basename}")
    private String basename;

    @Value("${spring.messages.cache-duration}")
    private int cacheDuration;

    @Value("${spring.messages.use-code-as-default-message}")
    private boolean useCodeAsDefaultMessage;

    /**
     * Locale resolver.
     *
     * @return the locale resolver
     */
    @Bean
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Labels.US);
        return localeResolver;
    }

    /**
     * Bundle message source.
     *
     * @return the message source
     */
    @Bean
    public MessageSource bundleMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename(basename);
        messageSource.setDefaultEncoding(encoding);
        messageSource.setCacheSeconds(cacheDuration);
        messageSource.setUseCodeAsDefaultMessage(useCodeAsDefaultMessage);

        return messageSource;
    }

}
