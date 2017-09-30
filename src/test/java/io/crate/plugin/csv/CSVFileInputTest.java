package io.crate.plugin.csv;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CSVFileInputTest {

    CSVFileInput subjectUnderTest;

    URI testURI = URI.create("file:///data/example.csv");
    Predicate<URI> testPredicateURI = Predicate.isEqual(true);


    @Before
    public void setup() {
        subjectUnderTest = new CSVFileInput();
    }

    @Test
    public void listUris_givenURI_thenReturnsListContainingURI() throws IOException {
        List<URI> result = subjectUnderTest.listUris(testURI, testPredicateURI);

        assertThat(result, is(Collections.singletonList(testURI)));
    }

}
