package io.crate.plugin.csv;


import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class CSVFileProcessor {
    private static final Logger logger = Loggers.getLogger(CSVFileProcessor.class);

    private static final byte NEW_LINE = (byte) '\n';
    private int skipped;
    private int recordsWritten;

    XContentBuilder builder;
    BufferedReader sourceReader;
    OutputStream outputStream;

    List<String> columns;
    List<List<String>> values;

    String line;

    public CSVFileProcessor(BufferedReader sourceReader, OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        builder = XContentFactory.jsonBuilder(this.outputStream);
        this.sourceReader = sourceReader;
        this.recordsWritten = 0;
        this.skipped = 0;
    }

    public void processToStream() throws IOException {
        String firstLine = verifyFileNotEmpty();
        columns = extractColumns(firstLine);
        values = extractValues();
        System.out.println(columns);
        System.out.println(values);

    }

    private String verifyFileNotEmpty() throws IOException {
        String firstLine = sourceReader.readLine();
        if (sourceReader == null) throw new IOException("empty file");
        return firstLine;
    }

    private List<List<String>> extractValues() {
        return sourceReader.lines()
                .map(line -> extractColumns(line))
                .collect(toList());
    }

    private List<String> extractColumns(String firstLine) {
        return Arrays.asList(firstLine.split(","));
    }

    public int getRecordsWritten() {
        return recordsWritten;
    }

    public int getSkipped() {
        return skipped;
    }

    private boolean nextLine() throws IOException {
        line = sourceReader.readLine();
        return line != null;
    }

    private void writeObject() throws IOException {
        this.recordsWritten++;
    }
}
