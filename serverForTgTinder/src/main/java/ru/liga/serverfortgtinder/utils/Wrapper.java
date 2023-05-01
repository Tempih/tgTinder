package ru.liga.serverfortgtinder.utils;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
@Service
public class Wrapper {
//todo логирование, вынести переменные
        public List<String> doWrap(String text, int lineWidth, FontMetrics fm) {
            List<String> lines = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(text, " ");
            int spaceLeft = lineWidth;
            StringBuilder builder = new StringBuilder();
            boolean removed = false;
            String word = "";

            while(tokenizer.hasMoreTokens()) {
                if (removed) {
                    removed = false;
                } else {
                    word = tokenizer.nextToken() + " ";
                }

                char[] chars = new char[word.length()];
                word.getChars(0, word.length(), chars, 0);

                for(int i = 0; i < chars.length; ++i) {
                    boolean nospaceleft;
                    if (fm.charWidth(chars[i]) > spaceLeft) {
                        if (chars[i] != ' ') {
                            builder.delete(builder.length() - i, builder.length());
                            removed = true;
                        }

                        if (builder.charAt(builder.length() - 1) == ' ') {
                            builder.delete(builder.length() - 1, builder.length());
                        }

                        lines.add(builder.toString());
                        spaceLeft = lineWidth;
                        builder.setLength(0);
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

            if (builder.length() > 1 && builder.charAt(builder.length() - 1) == ' ') {
                builder.delete(builder.length() - 1, builder.length());
            }

            lines.add(builder.toString());
            return lines;
        }
    }

