package org.mgwa.w40k.pairing;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

public final class LabelGetter {

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

    private static Locale fetchLocale() {
        return Optional.ofNullable(System.getenv("LANG"))
                .map(LOCALE_PARSER)
                .orElseGet(() -> Locale.getDefault(Locale.Category.DISPLAY));
    }

    public static LabelGetter create() {
        return new LabelGetter(fetchLocale());
    }

    public static LabelGetter create(Locale locale) {
        return new LabelGetter(Objects.requireNonNull(locale));
    }

    private LabelGetter(@Nonnull Locale locale) {
        this.bundle = ResourceBundle.getBundle("labels", locale);
        this.inputLocale = locale;
    }

    private final ResourceBundle bundle;
    private final Locale inputLocale;

    public Locale getLocale() {
        return this.inputLocale;
    }

    public String getLabel(String key) {
        return bundle.getString(key);
    }

}
