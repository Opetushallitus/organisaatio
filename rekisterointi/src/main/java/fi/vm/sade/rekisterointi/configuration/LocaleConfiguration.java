package fi.vm.sade.rekisterointi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
import java.util.TimeZone;

@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

  public static final String SESSION_ATTRIBUTE_NAME_LOCALE = "locale";
  public static final String SESSION_ATTRIBUTE_NAME_TIMEZONE = "timezonze";
  public static final Locale DEFAULT_LOCALE = new Locale("fi");
  public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("Europe/Helsinki");

  @Bean
  public LocaleResolver localeResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setLocaleAttributeName(SESSION_ATTRIBUTE_NAME_LOCALE);
    localeResolver.setTimeZoneAttributeName(SESSION_ATTRIBUTE_NAME_TIMEZONE);
    localeResolver.setDefaultLocale(DEFAULT_LOCALE);
    localeResolver.setDefaultTimeZone(DEFAULT_TIMEZONE);
    return localeResolver;
  }

  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    return new LocaleChangeInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor());
  }

}
