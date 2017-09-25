package io.crate.plugin.csv;

import io.crate.operation.collect.files.FileInput;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class CSVFileInput implements FileInput {

    private static final Logger logger = Loggers.getLogger(CSVFileInput.class);

    @Override
    public List<URI> listUris(URI fileUri, Predicate<URI> uriPredicate) throws IOException {
        return Collections.singletonList(fileUri);
    }

    @Override
    public InputStream getStream(URI uri) throws IOException {
        URL url = uri.toURL();
        try {
            return url.openStream();
        } catch (FileNotFoundException e) {
            return null;
        }
        // where do we check extension .csv??
    }

    @Override
    public boolean sharedStorageDefault() {
        return true;
    }
}
