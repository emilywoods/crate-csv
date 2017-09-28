package io.crate.plugin.csv;

import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class CSVFileProcessorTest {
    CSVFileProcessor subjectUnderTest;

    public static final String EXAMPLE_CSV_FILE = "/data/smallerInput.csv";
    InputStream inputStream;
    BufferedReader reader;
    ByteArrayOutputStreamWithExposedBuffer outputStream;
    ByteArrayInputStream resultInputStream;
    BufferedReader resultReader;

    @Ignore
    @Test
    public void CSVFileProcessor_givenCSVInput_thenProcessesFile_andConvertsToJson() throws IOException {
        givenInputStreamIsNull();
        try {
            givenInputStreamReceives(EXAMPLE_CSV_FILE);
            givenBufferedReaderWithInputStreamAndCharset(inputStream, StandardCharsets.UTF_8);
            givenByteArrayOutputStreamWithSize(209700000);

            whenCSVFileProcessorIsInitialised();
            whenProcessToStreamIsCalled();

            givenByteArrayInputStreamIsInitialisedWith(outputStream.getBuffer());
            givenBufferedReaderWithResultInputStreamAndCharset(resultInputStream, StandardCharsets.UTF_8);

            thenResultStartsWith("{\"[{Code=AFG, Name=Afghanistan}, {Code=ALB, Name=Albania}\"}");
        } finally {
            if (inputStream != null) {
                inputStream.close(); //teardown instead??
            }
        }
    }

    @Test
    public void processToStream_givenFileIsEmpty_thenLogsError_andSkipsFile(){
    }

    @Test
    public void processToStream_givenInvalidFirstLine_thenLogsError_andSkipsFile() {
        String input = "Code,,Capital";
    }

    @Test
    public void processToStream_givenRowWithMissingValue_andTheFileHasOneRowOfValues_thenLogsError_andSkipsFile() {
        String input = "Code,,Capital";
    }

    @Test
    public void processToStream_givenRowWithMissingValue_andTheFileHasMoreThanOneRowOfValues_thenLogsError_andSkipsRow() {
        String input = "Code,Name\n";
    }

    @Test
    public void processToStream_givenRowWithExtraValue_andTheFileHasMoreThanOneRow_thenLogsError_andSkipsFile() {
        String input = "Code,Name\n";
    }

    @Test
    public void processToStream_givenRowWithExtraValue_andTheFileHasMoreThanOneRowOfValues_thenLogsError_andSkipsRow() {
        String input = "Code,Name\n";
    }

    @Test
    public void processToStream_givenWithInvalidCharacter_andTheFileHasMoreThanOneRowOfValues_thenLogsError_andSkipsRow() {
        String input = "Code,Name\n";
    }

    @Test
    public void processToStream_givenValidInput_thenProcessesFileCorrectly() {
        String input = "Code,Name\n";
    }

    @Test
    public void processToStream_givenRowSpreadsAcrossMultipleLines_thenProcessesFileCorrectly() {
        String input = "Code,Name\n";
    }


    private void thenResultStartsWith(String result) throws IOException {
        assertThat(resultReader.readLine(), startsWith(result));
    }

    class ByteArrayOutputStreamWithExposedBuffer extends ByteArrayOutputStream {
        public ByteArrayOutputStreamWithExposedBuffer(int size) {
            super(size);
        }

        public byte[] getBuffer() {
            return buf;
        }
    }

    private void givenInputStreamIsNull() {
        inputStream = null;
    }

    private void givenInputStreamReceives(String exampleFile) throws IOException {
        inputStream = getClass().getResource(exampleFile).openStream();
    }

    private void givenBufferedReaderWithInputStreamAndCharset(InputStream inputStream, Charset charset) {
        reader = new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    private void givenBufferedReaderWithResultInputStreamAndCharset(ByteArrayInputStream inputStream, Charset charset) {
        resultReader = new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    private void givenByteArrayOutputStreamWithSize(int size) {
        outputStream = new ByteArrayOutputStreamWithExposedBuffer(size);
    }

    private void whenCSVFileProcessorIsInitialised() throws IOException {
        subjectUnderTest = new CSVFileProcessor(reader, outputStream);
    }

    private void whenProcessToStreamIsCalled() throws IOException {
        subjectUnderTest.processToStream();
    }

    private ByteArrayInputStream givenByteArrayInputStreamIsInitialisedWith(byte[] buffer) {
        return resultInputStream = new ByteArrayInputStream(buffer);
    }


}
