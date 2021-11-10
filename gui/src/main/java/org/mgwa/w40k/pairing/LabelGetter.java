package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.util.LoggerSupplier;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public final class LabelGetter implements Function<String, String> {

    static class LocaleParser implements Function<String, Locale> {

        @Override
        public Locale apply(String langAsStr) {
            if (langAsStr == null || langAsStr.length() < 2) {
                return null;
            }
            Locale.Builder builder = new Locale.Builder();
            String localeLang = langAsStr.substring(0, 2);
            builder.setLanguage(localeLang);
            if (langAsStr.length() >= 5) {
                String localeRegion = langAsStr.substring(3, 5);
                builder.setRegion(localeRegion);
            }
            return builder.build();
        }
    }

    private static final LocaleParser LOCALE_PARSER = new LocaleParser();

    static final List<Locale> SUPPORTED_LOCALES = List.of(
            new Locale("en"),
            new Locale("fr"));
    static final Locale DEFAULT_LOCALE = SUPPORTED_LOCALES.get(0);

    private static final Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();

    private static Locale fetchLocale() {
        return Optional.ofNullable(System.getenv("LANG"))
                .map(LOCALE_PARSER)
                .orElseGet(() -> Locale.getDefault(Locale.Category.DISPLAY));
    }

    private static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("labels", locale);
    }

    public static LabelGetter create() {
        return create(fetchLocale());
    }

    public static LabelGetter create(Locale locale) {
        Objects.requireNonNull(locale);
        ResourceBundle bundle;
        if (SUPPORTED_LOCALES.stream()
                .map(Locale::getLanguage)
                .noneMatch(l -> l.equals(locale.getLanguage()))) {
            bundle = getBundle(DEFAULT_LOCALE);
            LOGGER.warning(String.format("Wanted locale %s not supported: using default locale %s", locale, bundle.getLocale()));
        }
        else {
            bundle = getBundle(locale);
            LOGGER.info(String.format("Using supported locale %s", locale));
        }
        return new LabelGetter(bundle);
    }

    private LabelGetter(@Nonnull ResourceBundle bundle) {
        this.bundle = bundle;
    }

    private final ResourceBundle bundle;

    public Locale getLocale() {
        return bundle.getLocale();
    }

    public String getLabel(String key) {
        return bundle.getString(key);
    }

    @Override
    public String apply(String s) {
        return getLabel(s);
    }
}
