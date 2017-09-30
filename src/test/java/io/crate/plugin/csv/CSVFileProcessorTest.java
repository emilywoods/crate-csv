package io.crate.plugin.csv;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CSVFileProcessorTest {
    CSVFileProcessor subjectUnderTest;

    public static final String EXAMPLE_CSV_FILE = "/data/smallerInput.csv";
    private static final byte NEW_LINE = (byte) '\n';

    int skipped;

    InputStream inputStream;

    @Mock
    BufferedReader sourceReader;

    @Mock
    ByteArrayOutputStreamWithExposedBuffer outputStream;

    @Mock
    Logger logger = Loggers.getLogger(CSVFileProcessor.class);;

    ByteArrayInputStream resultInputStream;
    BufferedReader resultReader;

    @Before
    public void setup() throws IOException {
        subjectUnderTest = new CSVFileProcessor(sourceReader, outputStream);
    }

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

            thenResultStartsWith("{[{Code=AFG, Name=Afghanistan}, {Code=ALB, Name=Albania}]}");
        } finally {
            if (inputStream != null) {
                inputStream.close(); //teardown instead??
            }
        }
    }

    @Test
    public void processToStream_givenFileIsEmpty_andSkipsFile() throws IOException {
        givenFileIsEmpty();
        
        whenProcessToStreamIsCalled();

        thenDoesNotWriteToOutputStream();
    }

    @Test
    public void processToStream_givenInvalidFirstLine_andSkipsFile() throws IOException {
        givenFirstLineIs("Code,,Capital");

        whenProcessToStreamIsCalled();

        thenDoesNotWriteToOutputStream();
    }

    @Test
    public void processToStream_givenValidRowOfKeys_andNoRowOfValues_thenSkipsFile() throws IOException {
        givenFileHasValidFirstLineOfKeys();
        givenRowOfValues(null);

        whenProcessToStreamIsCalled();

        thenDoesNotWriteToOutputStream();
    }

    @Test
    public void processToStream_givenRowWithMissingValue_andTheFileHasOneRowOfValues_thenSkipsFile() throws IOException {
        givenFileHasValidFirstLineOfKeys();
        givenRowOfValues("GBR,Great Britain,");

        whenProcessToStreamIsCalled();

        thenDoesNotWriteToOutputStream();
    }

    @Test
    public void processToStream_givenRowWithMissingValues_andTheFileHasMoreThanOneRowOfValues_thenSkipsRow() throws IOException {
        givenFileHasValidFirstLineOfKeys();
        givenFirstAndSecondRowsOfValuesAre("GBR,Great Britain,London", "IRL,Ireland,");

        whenProcessToStreamIsCalled();
        whenGetSkippedIsCalled();
        
        thenNumberOfSkippedRowsIsEqualTo(1);
    }

    private void whenGetSkippedIsCalled() {
        skipped = subjectUnderTest.getSkipped();
    }

    private void thenNumberOfSkippedRowsIsEqualTo(int i) {
        assertThat(skipped, is(1));
    }

    @Test
    public void Emptyrow() {

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

    private void givenFileIsEmpty() throws IOException {
        when(sourceReader.readLine()).thenReturn(null);
    }

    private void givenFileHasValidFirstLineOfKeys() throws IOException {
        when(sourceReader.readLine()).thenReturn("Code,Country,Capital\n");
    }

    private void givenRowOfValues(String row) {
        when(sourceReader.lines()).thenReturn(Stream.of(row));
    }


    private void givenFirstAndSecondRowsOfValuesAre(String firstRow, String secondRow) {
        when(sourceReader.lines()).thenReturn(Stream.of(firstRow), Stream.of(secondRow));
    }


    private void givenInputStreamIsNull() {
        inputStream = null;
    }

    private void givenFirstLineIs(String input) throws IOException {
        when(sourceReader.readLine()).thenReturn(input);
    }
    private void givenInputStreamReceives(String exampleFile) throws IOException {
        inputStream = getClass().getResource(exampleFile).openStream();
    }
    private void givenBufferedReaderWithInputStreamAndCharset(InputStream inputStream, Charset charset) {
        sourceReader = new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    private void givenBufferedReaderWithResultInputStreamAndCharset(ByteArrayInputStream inputStream, Charset charset) {
        resultReader = new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    private ByteArrayInputStream givenByteArrayInputStreamIsInitialisedWith(byte[] buffer) {
        return resultInputStream = new ByteArrayInputStream(buffer);
    }

    private void givenByteArrayOutputStreamWithSize(int size) {
        outputStream = new ByteArrayOutputStreamWithExposedBuffer(size);
    }

    private void whenCSVFileProcessorIsInitialised() throws IOException {
        subjectUnderTest = new CSVFileProcessor(sourceReader, outputStream);
    }

    private void whenProcessToStreamIsCalled() throws IOException {
        subjectUnderTest.processToStream();
    }

    private void thenDoesNotWriteToOutputStream() {
        verify(outputStream, times(0)).write(NEW_LINE);
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


}
