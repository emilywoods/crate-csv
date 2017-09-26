package io.crate.plugin.csv;

import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class CSVFileProcessorTest {
    CSVFileProcessor subjectUnderTest;

    public static final String EXAMPLE_CSV_FILE = "/data/example.csv";
    InputStream inputStream;
    BufferedReader reader;
    ByteArrayOutputStreamWithExposedBuffer outputStream;
    ByteArrayInputStream resultInputStream;
    BufferedReader resultReader;

    @Test
    public void CSVFileProcessor_processesFile() throws IOException {
        givenInputStreamIsNull();
        try {
            givenInputStreamReceives(EXAMPLE_CSV_FILE);
            givenBufferedReaderWithInputStreamAndCharset(inputStream, StandardCharsets.UTF_8);
            givenByteArrayOutputStreamWithSize(209700000);

            whenCSVFileProcessorIsInitialised();
            whenProcessToStreamIsCalled();

            givenByteArrayInputStreamIsInitialisedWith(outputStream.getBuffer());
            givenBufferedReaderWithResultInputStreamAndCharset(resultInputStream, StandardCharsets.UTF_8);

            thenResultStartsWith("{\"Code:\":\"AFG\",\"Name\":\"Afghanistan\"");
        } finally {
            if (inputStream != null) {
                inputStream.close(); //teardown instead??
            }
        }
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
