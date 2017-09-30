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
    List<List<String>> listOfRows;

    public CSVFileProcessor(BufferedReader sourceReader, OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        builder = jsonBuilder(this.outputStream);
        this.sourceReader = sourceReader;
        this.recordsWritten = 0;
        this.skipped = 0;
    }

    // improve naming
    
    public void processToStream() throws IOException {
        final int numberOfKeys;
        String firstLine = sourceReader.readLine();
        if (isFileEmpty(firstLine)) {
            logger.debug("Empty file input"); //Extract
            return;
        }

        keys = extractColumnValues(firstLine);
        numberOfKeys = keys.size();

        if (invalidKeyPresent(keys)) {
            logger.debug("Invalid key entry");
            return;
        }


        listOfRows = extractRowsOfValues();

        if (listOfRows.isEmpty()) {
            return;
        }

        boolean invalidRowPresent = rowWithEmptyValuePresent(listOfRows, numberOfKeys);


        if (invalidRowPresent && listOfRows.size() < 2) {
            return;
        } else if (invalidRowPresent) {
            List<List<String>> listOfValidRows = listOfRowsMinusInvalidRows(listOfRows, numberOfKeys);
            skipped = listOfRows.size() - listOfValidRows.size();
            System.out.println(skipped);
        }


        List<Map<String, String>> inputAsMap = convertCSVToListOfMaps(keys, listOfRows);
        convertListOfMapsToXContentBuilder(inputAsMap);
    }


    public int getRecordsWritten() {
        return recordsWritten;
    }

    public int getSkipped() {
        return skipped;
    }

    private boolean isFileEmpty(String content) throws IOException {
        return content == null;
    }

    private List<List<String>> extractRowsOfValues() {
        return sourceReader.lines()
                .filter(row -> row != null && !row.isEmpty())
                .map(this::extractColumnValues)
                .collect(toList());
    }

    private List<String> extractColumnValues(String firstLine) {
        return Arrays.asList(firstLine.split(","));
    }

    private boolean invalidKeyPresent(List<String> keys) {
        return keys.stream().anyMatch(key -> key == null || key.isEmpty());
    }

    private boolean rowWithEmptyValuePresent(List<List<String>> rowsOfValues, int numberOfKeys) {
        return rowsOfValues
                .stream()
                .anyMatch(row -> row.stream().anyMatch(value -> (value == null) || value.isEmpty()) ||
                        row.size() != numberOfKeys);
    }

    private  List<List<String>> listOfRowsMinusInvalidRows(List<List<String>> listOfRows, int numberOfKeys) {
        return listOfRows.stream()
                .filter(row -> row.size() != numberOfKeys)
                .collect(toList());
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

