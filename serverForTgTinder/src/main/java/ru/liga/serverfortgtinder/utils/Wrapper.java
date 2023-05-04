package ru.liga.serverfortgtinder.utils;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
@Service
public class Wrapper {
    private static final Integer SOURCE_BEGIN = 0;
    private static final Integer DISPLACEMENT_BEGIN = 0;
    private static final String SPACE = " ";
    private static final char SPACE_FOR_CHAR = ' ';
    private static final Integer ZERO = 0;
    private static final String EMPTY = "";


    public List<String> doWrap(String text, int lineWidth, FontMetrics fm) {
            List<String> lines = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(text, SPACE);
            int spaceLeft = lineWidth;
            StringBuilder builder = new StringBuilder();
            boolean removed = false;
            String word = EMPTY;

            while(tokenizer.hasMoreTokens()) {
                if (removed) {
                    removed = false;
                } else {
                    word = tokenizer.nextToken() + SPACE;
                }

                char[] chars = new char[word.length()];
                word.getChars(SOURCE_BEGIN, word.length(), chars, DISPLACEMENT_BEGIN);

                for(int i = 0; i < chars.length; ++i) {
                    boolean nospaceleft;
                    if (fm.charWidth(chars[i]) > spaceLeft) {
                        if (chars[i] != SPACE_FOR_CHAR) {
                            builder.delete(builder.length() - i, builder.length());
                            removed = true;
                        }

                        if (builder.charAt(builder.length() - 1) == SPACE_FOR_CHAR) {
                            builder.delete(builder.length() - 1, builder.length());
                        }

                        lines.add(builder.toString());
                        spaceLeft = lineWidth;
                        builder.setLength(ZERO);
                        nospaceleft = true;
                    } else {
                        spaceLeft -= fm.charWidth(chars[i]);
                        nospaceleft = false;
                    }

                    if (removed || nospaceleft) {
                        break;
                    }

                    builder.append(chars[i]);
                }
            }

            if (removed) {
                builder.append(word.trim());
            }

            if (builder.length() > 1 && builder.charAt(builder.length() - 1) == SPACE_FOR_CHAR) {
                builder.delete(builder.length() - 1, builder.length());
            }

            lines.add(builder.toString());
            return lines;
        }
    }

