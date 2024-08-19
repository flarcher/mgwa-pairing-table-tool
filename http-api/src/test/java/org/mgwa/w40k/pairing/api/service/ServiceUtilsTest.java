package org.mgwa.w40k.pairing.api.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

public class ServiceUtilsTest {

    @Test
    public void testFileExtensionSupport() {
        Stream.of( "test.xls", "test.xlsx", "test.json" )
            .forEach(fileName -> {
                Optional<FileExtensionSupport> fileSupport = ServiceUtils.getFileSupport(fileName);
                Assert.assertNotNull(fileSupport);
                Assert.assertTrue(fileSupport.isPresent());
                Assert.assertFalse(fileSupport.get().name().isBlank());
            });
    }

    @Test
    public void testFileExtensionNotSupported() {
        Stream.of( "test.wtf", "test" )
            .forEach(fileName -> {
                Optional<FileExtensionSupport> fileSupport = ServiceUtils.getFileSupport(fileName);
                Assert.assertNotNull(fileSupport);
                Assert.assertFalse(fileSupport.isPresent());
            });
    }
}
