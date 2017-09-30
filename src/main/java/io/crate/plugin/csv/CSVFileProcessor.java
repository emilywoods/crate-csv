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

    public CSVFileProcessor(BufferedReader sourceReader, OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        builder = jsonBuilder(this.outputStream);
        this.sourceReader = sourceReader;
        this.recordsWritten = 0;
        this.skipped = 0;
    }

    public void processToStream() throws IOException {
        final List<String> keys;
        final int numberOfKeys;
        final List<List<String>> listOfRows;

        final String fileContent = sourceReader.readLine();

        if (isFileEmpty(fileContent)) {
            logger.debug("An empty file has been input");
            return;
        }

        keys = extractColumnValues(fileContent);
        numberOfKeys = keys.size();

        if (emptyKeyPresent(keys)) {
            logger.error("One or more of the key entries was null or empty");
            return;
        }

        listOfRows = extractListOfRowsWithValues();

        if (isRowWithEmptyValuePresent(listOfRows, numberOfKeys)) {
            List<List<String>> listOfValidRows = listOfRowsWithInvalidRowsRemoved(listOfRows, numberOfKeys);
            skipped += listOfRows.size() - listOfValidRows.size();
            convertCSVToJson(keys, listOfValidRows);
        } else {
            convertCSVToJson(keys, listOfRows);
        }
    }

    public int getRecordsWritten() {
        return recordsWritten;
    }

    public int getSkipped() {
        return skipped;
    }

    private boolean isFileEmpty(String content) throws IOException {
        return content == null || content.isEmpty();
    }

    private boolean emptyKeyPresent(List<String> keys) {
        return keys.stream().anyMatch(key -> key == null || key.isEmpty());
    }

    private List<List<String>> extractListOfRowsWithValues() {
        return sourceReader.lines()
                .filter(row -> row != null && !row.isEmpty())
                .map(this::extractColumnValues)
                .collect(toList());
    }

    private List<String> extractColumnValues(String firstLine) {
        return Arrays.asList(firstLine.split(","));
    }

    private boolean isRowWithEmptyValuePresent(List<List<String>> rowsOfValues, int numberOfKeys) {
        return rowsOfValues
                .stream()
                .anyMatch(row -> row.size() != numberOfKeys);
    }

    private  List<List<String>> listOfRowsWithInvalidRowsRemoved(List<List<String>> listOfRows, int numberOfKeys) {
        return listOfRows.stream()
                .filter(row -> row.size() == numberOfKeys)
                .collect(toList());
    }

    private void convertCSVToJson(List<String> keys, List<List<String>> values) throws IOException {
        values.forEach(row -> {
            Map<String, String> mapForSingleRow = getMapForSingleRow(keys, row);

            for (Map.Entry<String, String> mapEntry : mapForSingleRow.entrySet()) {
                generateJsonForEachEntry(mapForSingleRow, mapEntry);
            }
        });
    }

    private Map<String, String> getMapForSingleRow(List<String> keys, List<String> row) {
        return IntStream.range(0, row.size())
                .boxed()
                .collect(Collectors.toMap(keys::get, row::get));
    }

    private void generateJsonForEachEntry(Map<String, String> mapForSingleRow, Map.Entry<String, String> mapEntry) {
        try {
            convertListOfMapsToXContentBuilder(mapEntry.getKey(), mapEntry.getValue());
            logger.debug("Entry: " + mapForSingleRow);
            this.recordsWritten++;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void convertListOfMapsToXContentBuilder(String key, String value) throws IOException {
        builder.startObject();
        builder.field(key, value);
        builder.endObject();
        builder.flush();
        outputStream.write(NEW_LINE);
    }

}

