package org.mgwa.w40k.pairing.util;

import org.junit.Assert;
import org.junit.Test;
import org.mgwa.w40k.pairing.util.LabelGetter;

import java.util.Locale;

public class LabelGetterTest {

    private final LabelGetter.LocaleParser localeParser = new LabelGetter.LocaleParser();
    private final Locale localeEnglish = LabelGetter.SUPPORTED_LOCALES.get(0);
    private final Locale localeFrench = LabelGetter.SUPPORTED_LOCALES.get(1);
    private final Locale localeFrenchOfFrance = new Locale(localeFrench.getLanguage(), Locale.FRANCE.getCountry());
    private final Locale localeEnglishOfUK= new Locale(localeEnglish.getLanguage(), Locale.UK.getCountry());
    private final Locale localeUnknown = new Locale("de");

    @Test
    public void testLocaleParsing_empty() {
        Assert.assertNull(localeParser.apply(null));
        Assert.assertNull(localeParser.apply(""));
        Assert.assertNull(localeParser.apply("a")); // single character
    }

    @Test
    public void testLocaleParsing_nominalCases() {
        Assert.assertEquals(new Locale("fr", "FR"),  localeParser.apply("fr_FR"));
        Assert.assertEquals(new Locale("en"),  localeParser.apply("en"));
    }

    @Test
    public void testLocaleGetter_supportedLocales() {
        LabelGetter labelGetter = LabelGetter.create(localeFrench);
        Assert.assertEquals(localeFrench, labelGetter.getLocale());
        Assert.assertEquals("Langue", labelGetter.getLabel("language"));

        labelGetter = LabelGetter.create(localeEnglish);
        Assert.assertEquals(localeEnglish, labelGetter.getLocale());
        Assert.assertEquals("Language", labelGetter.getLabel("language"));
    }

    @Test
    public void testLocaleGetter_localeFallback() {
        LabelGetter labelGetter = LabelGetter.create(localeFrenchOfFrance);
        Assert.assertEquals(localeFrench, labelGetter.getLocale());
        Assert.assertEquals("Langue", labelGetter.getLabel("language"));

        labelGetter = LabelGetter.create(localeEnglishOfUK);
        Assert.assertEquals(localeEnglish, labelGetter.getLocale());
        Assert.assertEquals("Language", labelGetter.getLabel("language"));
    }

    @Test
    public void testLocaleGetter_defaultLocale() {
        Assert.assertTrue(LabelGetter.SUPPORTED_LOCALES.stream()
                .map(Locale::getLanguage)
                .noneMatch(lang -> lang.equals(localeUnknown.getLanguage())));
        LabelGetter labelGetter = LabelGetter.create(localeUnknown);
        Assert.assertEquals(LabelGetter.DEFAULT_LOCALE, labelGetter.getLocale());
        //Assert.assertEquals("Language", labelGetter.getLabel("language"));
    }
}
