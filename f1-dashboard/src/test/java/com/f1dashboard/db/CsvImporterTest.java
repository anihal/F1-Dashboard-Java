package com.f1dashboard.db;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CsvImporterTest {

    @Test
    void parsesSimpleLine() {
        String[] result = CsvImporter.parseCsvLine("1,hamilton,44,HAM,Lewis,Hamilton");
        assertEquals(6, result.length);
        assertEquals("hamilton", result[1]);
    }

    @Test
    void parsesQuotedFieldWithComma() {
        String[] result = CsvImporter.parseCsvLine("1,\"Australian Grand Prix\",2009");
        assertEquals(3, result.length);
        assertEquals("Australian Grand Prix", result[1]);
    }

    @Test
    void parsesNullMarker() {
        String[] result = CsvImporter.parseCsvLine("1,\\N,HAM");
        assertEquals("\\N", result[1]);
    }

    @Test
    void parsesEscapedQuoteInsideQuotedField() {
        String[] result = CsvImporter.parseCsvLine("1,\"He said \"\"hi\"\"\",done");
        assertEquals(3, result.length);
        assertEquals("He said \"hi\"", result[1]);
    }
}
