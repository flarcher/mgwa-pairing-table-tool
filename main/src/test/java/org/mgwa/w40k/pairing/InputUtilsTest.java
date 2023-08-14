package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;
import org.mgwa.w40k.pairing.web.WebAppUtils;

import java.nio.file.Path;
import java.util.List;

public class InputUtilsTest {

    @Test
    public void testWebAppResourceListing() {
        Path matchingPath = WebAppUtils.WEB_APP_FILES_FOLDER.resolve("index.html");
        List<Path> resources = InputUtils.listResourcesFromClassPath(
            (src, rsc) -> rsc.endsWith(matchingPath));
        Assert.assertNotNull(resources);
        Assert.assertFalse(resources.isEmpty());
        //System.err.println(resources.stream().map(Path::toString).collect(Collectors.joining("\n")));
    }

    @Test
    public void testResourceFiltering() {
        List<Path> resources = InputUtils.listResourcesFromClassPath(
            (src, rsc) -> rsc.endsWith("InputUtils.class"));
        Assert.assertNotNull(resources);
        Assert.assertFalse(resources.isEmpty());
        Assert.assertEquals(1, resources.size());
    }
}
