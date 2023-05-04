package ru.liga.serverfortgtinder;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import ru.liga.serverfortgtinder.service.WordService;

import static org.junit.Assert.assertEquals;


public class WordServiceTest {
    @Test
    public void addHardSignToWordIfLastCharConsonant() {
        WordService wordService = new WordService();
        String input = "Мы говорим на русском языке.";
        String expectedOutput = "Мы говоримъ на русскомъ языке";
        String actualOutput = wordService.convertToSlavonic(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void replaceRuIToPreReformRuIIfNextCharVowels() {
        WordService wordService = new WordService();
        String input = "Линия, пить";
        String expectedOutput = "Линiя, пить";
        String actualOutput = wordService.convertToSlavonic(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void replaceRuSubstringToPreReformRuSubstringIfWordContainsIt() {
        WordService wordService = new WordService();
        String input = "Невеста кричала";
        String expectedOutput = "Невѣста кричала";
        String actualOutput = wordService.convertToSlavonic(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void replaceRuFToPreReformRuFIfWordName() {
        WordService wordService = new WordService();
        String input = "Невеста кричала";
        String expectedOutput = "Невѣста кричала";
        String actualOutput = wordService.convertToSlavonic(input);
        assertEquals(expectedOutput, actualOutput);
    }
}
