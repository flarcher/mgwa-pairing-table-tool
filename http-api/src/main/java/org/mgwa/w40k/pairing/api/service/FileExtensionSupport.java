package org.mgwa.w40k.pairing.api.service;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Describes file input/output support.
 */
public enum FileExtensionSupport {

    EXCEL_SPREADSHEET(
            "xls(x)?", "xlsx",
            "MicroSoft Excel Spreadsheet",
            MatrixConvertorFactory.EXCEL_FORMAT),
    JSON(
            "js(on)?", "json",
            "JavaScript Object Notation",
            MatrixConvertorFactory.JSON)
    ;

    FileExtensionSupport(String patternAsStr, String extension, String format, MatrixConvertorFactory factory) {
        this.pattern = Pattern.compile("^" + patternAsStr + "$", Pattern.CASE_INSENSITIVE);
        if (!pattern.matcher(extension).matches()) {
            throw new IllegalArgumentException(
                    String.format("Default extension %s must match the pattern %s",
                    extension, patternAsStr));
        }
        this.format = Objects.requireNonNull(format);
        this.defaultExtension = Objects.requireNonNull(extension);
        this.factory = Objects.requireNonNull(factory);
    }

    private final Pattern pattern;
    private final String format;
    private final String defaultExtension;
    private final MatrixConvertorFactory factory;

    boolean match(String fileName) {
        return pattern.matcher(fileName).matches();
    }

    public String getFormat() {
        return format;
    }

    MatrixConvertorFactory getFactory() {
        return factory;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    @Override
    public String toString() {
        return "{" +
                "pattern=" + pattern.pattern() +
                ", default='" + defaultExtension + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}
