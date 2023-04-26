package ru.liga.serverfortgtinder.service;

import liquibase.pro.packaged.S;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import ru.liga.serverfortgtinder.enums.Vowels;
import ru.liga.serverfortgtinder.model.OldSlavonicName;
import ru.liga.serverfortgtinder.model.Substring;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class WordService {
    SpringJdbcConnectionProvider springJdbcConnectionProvider;
    private static final String VOWELS = Arrays.stream(Vowels.values()).toList().toString().toLowerCase();
    private final List<Substring> SUBSTRINGS;
    private final List<OldSlavonicName> NAMES;
    private static final String SPACE = " ";
    private static final String DOT = ".";
    private static final String COMMA = ",";
    private static final String SOFT_SIGN = "ь";
    private static final Integer INDEX_IF_WORD_END_WITH_CHAR = 1;
    private static final Integer INDEX_IF_WORD_END_WITHOUT_CHAR = 2;
    private static final Integer LAST_SYMBOL = 1;
    private static final Integer FIRST_SYMBOL = 0;
    private static final Integer FIRST_INDEX_NEXT_SYMBOL = 1;
    private static final Integer LAST_INDEX_NEXT_SYMBOL = 2;
    private static final String RU_CHAR_I = "и";
    private static final String RU_CHAR_F = "ф";
    private static final String OLD_SLAVONIC_CHAR_I = "i";
    private static final String OLD_SLAVONIC_CHAR_F = "ѳ";
    private static final String HARD_SIGN = "ъ";
    private static final String REGEX_DOT = "\\.";


    public WordService(SpringJdbcConnectionProvider springJdbcConnectionProvider) {
        this.springJdbcConnectionProvider = springJdbcConnectionProvider;
        this.SUBSTRINGS = springJdbcConnectionProvider.getSubstringsList();
        this.NAMES = springJdbcConnectionProvider.getNamesList();
    }

    public String convertToSlovenian(String text) {
        List<String> splitText = List.of(text.toLowerCase().split(SPACE));
        List<String> ssText = List.of(splitText.stream()
                .map(word -> {
                    int minusIndex = INDEX_IF_WORD_END_WITH_CHAR;
                    String lastChar = word.substring(word.length() - minusIndex);
                    if (word.contains(COMMA) || word.contains(DOT)) { //todo переделать на проверку что последний знак буква
                        log.debug("Последний символ слова {} не буква" ,word);
                        minusIndex = INDEX_IF_WORD_END_WITHOUT_CHAR;
                        lastChar = word.substring(word.length() - minusIndex, word.length() - LAST_SYMBOL);
                    }
                    log.debug("Проверяем что последний символ слова {} согласная" ,word);
                    if (!VOWELS.contains(lastChar) && !lastChar.equalsIgnoreCase(SOFT_SIGN) && Character.isLetter(lastChar.charAt(0))) {
                        if (minusIndex == INDEX_IF_WORD_END_WITHOUT_CHAR) {
                            word = word.substring(FIRST_SYMBOL, word.length() - LAST_SYMBOL).concat(HARD_SIGN).concat(word.substring(word.length() - LAST_SYMBOL));
                            log.debug("Слову {} в конце дописали {}" ,word, HARD_SIGN);
                        } else {
                            word = word.concat(HARD_SIGN);
                            log.debug("Слову {} в конце дописали {}" ,word, HARD_SIGN);
                        }
                    }

                    log.debug("Проверяем что слово {} содержит {}" ,word, RU_CHAR_I);
                    if (word.contains(RU_CHAR_I)) {
                        for (int index = word.indexOf(RU_CHAR_I); index >= 0; index = word.indexOf(RU_CHAR_I, index + 1)) {
                            if (index < word.length() - LAST_SYMBOL) {
                                if (VOWELS.contains(word.substring(index + FIRST_INDEX_NEXT_SYMBOL, index + LAST_INDEX_NEXT_SYMBOL))) {
                                    word = word.substring(FIRST_SYMBOL, index).concat(OLD_SLAVONIC_CHAR_I).concat(word.substring(index + FIRST_INDEX_NEXT_SYMBOL));
                                    log.debug("В слове {} изменили {} с индексом {} на {}" ,word, RU_CHAR_I, index, OLD_SLAVONIC_CHAR_I);
                                }
                            }
                        }
                    }

                    log.debug("Проверяем что слово {} содержит одну из подстрок" ,word);
                    for (Substring substring : SUBSTRINGS) {
                        if (word.contains(substring.getRuSubstring())) {
                            word = word.replace(substring.getRuSubstring(), substring.getSsSubstring());
                            log.debug("В слове {} изменили подстроку {} на подстроку{}", word, substring.getRuSubstring(), substring.getSsSubstring());
                        }
                    }

                    log.debug("Проверяем что слово {} одно из имен в списке" ,word);
                    for (OldSlavonicName name : NAMES) {
                        if (word.contains(name.getName())) {
                            word = word.replaceFirst(RU_CHAR_F, OLD_SLAVONIC_CHAR_F);
                            log.debug("В слове {} изменили букву {} на {}", word, RU_CHAR_F, OLD_SLAVONIC_CHAR_F);
                            break;
                        }
                    }
                    return word;
                }).collect(Collectors.joining(SPACE)).split(REGEX_DOT));

        log.debug("Весь текст переведен");
        return ssText.stream().map(word -> StringUtils.capitalize(word.trim())).collect(Collectors.joining(DOT.concat(SPACE)));
    }

}