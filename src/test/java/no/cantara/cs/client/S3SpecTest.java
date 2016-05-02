package no.cantara.cs.client;

import java.net.URI;

import org.testng.annotations.Test;

import no.cantara.cs.client.DownloadUtil.S3Spec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Sindre Mehus
 */
public class S3SpecTest {

    @Test
    public void testCompleteUri() throws Exception {
        S3Spec spec = S3Spec.parse(new URI("s3://mybucket/foo/bar/file.txt?region=eu-west-1&cmkid=123abc"));
        assertEquals("mybucket", spec.s3Bucket);
        assertEquals("foo/bar/file.txt", spec.s3Object);
        assertEquals("eu-west-1", spec.region);
        assertEquals("123abc", spec.cmkId);
    }

    @Test
    public void testUriWithRegion() throws Exception {
        S3Spec spec = S3Spec.parse(new URI("s3://mybucket/foo/bar/file.txt?region=eu-west-1"));
        assertEquals("mybucket", spec.s3Bucket);
        assertEquals("foo/bar/file.txt", spec.s3Object);
        assertEquals("eu-west-1", spec.region);
        assertNull(spec.cmkId);
    }

    @Test
    public void testUriWithoutQuery() throws Exception {
        S3Spec spec = S3Spec.parse(new URI("s3://mybucket/foo/bar/file.txt"));
        assertEquals("mybucket", spec.s3Bucket);
        assertEquals("foo/bar/file.txt", spec.s3Object);
        assertNull(spec.region);
        assertNull(spec.cmkId);

        spec = S3Spec.parse(new URI("s3://mybucket/foo/bar/file.txt?"));
        assertEquals("mybucket", spec.s3Bucket);
        assertEquals("foo/bar/file.txt", spec.s3Object);
        assertNull(spec.region);
        assertNull(spec.cmkId);
    }
}