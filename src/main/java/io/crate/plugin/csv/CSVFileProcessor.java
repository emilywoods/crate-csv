package io.crate.plugin.csv;


import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class CSVFileProcessor {
    private static final Logger logger = Loggers.getLogger(CSVFileProcessor.class);

    private static final byte NEW_LINE = (byte) '\n';
    private int skipped;
    private int recordsWritten;

    XContentBuilder builder;
    BufferedReader sourceReader;
    OutputStream outputStream;

    List<String> keys;
    List<List<String>> values;

    public CSVFileProcessor(BufferedReader sourceReader, OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        builder = jsonBuilder(this.outputStream);
        this.sourceReader = sourceReader;
        this.recordsWritten = 0;
        this.skipped = 0;
    }

    public void processToStream() throws IOException {
        String firstLine = verifyFileNotEmpty();
        keys = extractColumnValues(firstLine);
        values = extractValues();
        List<Map<String, String>> inputAsMap = convertCSVToListOfMaps(keys, values);
        convertListOfMapsToXContentBuilder(inputAsMap);
    }

    public int getRecordsWritten() {
        return recordsWritten;
    }

    public int getSkipped() {
        return skipped;
    }

    private String verifyFileNotEmpty() throws IOException {
        String firstLine = sourceReader.readLine();
        if (sourceReader == null) throw new IOException("empty file");
        return firstLine;
    }

    private List<List<String>> extractValues() {
        return sourceReader.lines()
                .filter(row -> (row != null || !row.isEmpty()))
                .map(this::extractColumnValues)
                .collect(toList());
    }

    private List<String> extractColumnValues(String firstLine) {
        return Arrays.asList(firstLine.split(","));
    }

    private List<Map<String, String>> convertCSVToListOfMaps(List<String> keys, List<List<String>> values) throws IOException {
        List<Map<String, String>> csvMap = new ArrayList<>();
        values.forEach(row -> {
            Map<String, String> mapForSingleRow = IntStream.range(0, row.size())
                    .boxed()
                    .collect(Collectors.toMap(keys::get, row::get));
            logger.debug(mapForSingleRow);
            csvMap.add(mapForSingleRow);
            this.recordsWritten++;
        });
        return csvMap;
    }

    private void convertListOfMapsToXContentBuilder(List<Map<String, String>> inputAsMap) throws IOException {
        builder.startObject();
        builder.field(inputAsMap.toString());
        builder.endObject();
        builder.flush();
        outputStream.write(NEW_LINE);
    }

}

