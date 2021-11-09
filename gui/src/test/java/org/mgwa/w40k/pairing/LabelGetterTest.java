package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class LabelGetterTest {

    private final LabelGetter.LocaleParser localeParser = new LabelGetter.LocaleParser();
    private final Locale localeEnglish = new Locale("en");
    private final Locale localeFrench = new Locale("fr", "FR");

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
    public void testLocaleGetterRead() {
        LabelGetter labelGetter = LabelGetter.create(localeFrench);
        Assert.assertEquals(localeFrench, labelGetter.getLocale());
        Assert.assertEquals("Langue", labelGetter.getLabel("language"));

        labelGetter = LabelGetter.create(localeEnglish);
        Assert.assertEquals(localeEnglish, labelGetter.getLocale());
        Assert.assertEquals("Language", labelGetter.getLabel("language"));
    }
}
