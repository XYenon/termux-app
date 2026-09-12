package com.termux.app;

import static org.junit.Assert.assertEquals;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.termux.shared.file.FileUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowMimeTypeMap;

import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class LocaleIndependentFilesTest {
    @Test
    public void turkishLocaleDoesNotChangeFileNamesOrMimeExtensions() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            // Robolectric's MIME map starts empty, unlike the Android system map.
            ShadowMimeTypeMap mimeTypes = Shadow.extract(MimeTypeMap.getSingleton());
            mimeTypes.addExtensionMimeTypMapping("gif", "image/gif");
            assertEquals("image_file.gif", FileUtils.sanitizeFileName("IMAGE FILE.GIF", true, true));
            assertEquals("image/gif", new TermuxOpenReceiver.ContentProvider()
                .getType(Uri.parse("content://com.termux.files/IMAGE.GIF")));
        } finally {
            Locale.setDefault(original);
        }
    }
}
