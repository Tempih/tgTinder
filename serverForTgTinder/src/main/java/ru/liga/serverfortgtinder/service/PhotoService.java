package ru.liga.serverfortgtinder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.liga.serverfortgtinder.utils.Wrapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

import static java.lang.Math.round;
@Slf4j
@Service
public class PhotoService {
    @Autowired
    private Wrapper wrapper;

    private static final String BACKGROUND_PATH = "/background/prerev-background.jpg";
    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String FONT = "Old Standard TT";
    private static final Integer X_MARGE = 5;
    private static final Integer X_COUNT_MARGE = 2;
    private static final Integer Y_MARGE = 5;
    private static final Integer Y_COUNT_MARGE = 3;
    private static final Integer X_MARGE_HEADER = 250;
    private static final Integer ONE = 1;
    private static final Integer X_START_POSITION = 0;
    private static final Integer Y_START_POSITION = 0;
    private static final String PHOTO_FORMAT = "png";

    public String signImageAdaptBasedOnImage(String header, String description){
        log.debug("Создаем InputStream для получения заднего фона для фото по пути {}", BACKGROUND_PATH);
        InputStream is = PhotoService.class.getResourceAsStream(BACKGROUND_PATH);
        BufferedImage image;
        try {
            log.debug("Создаем фото из заднего фона");
            image = ImageIO.read(is);
            is.close();
            log.debug("Закрываем InputStream");
        } catch (IOException e) {
            throw new RuntimeException(e); //todo ошибку для фото
        }
        List<String> headerList = new ArrayList<>();
        headerList.add(header.concat(COMMA));
        String maxLengthHeaderWord = Arrays.stream(header.split(SPACE))
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
        log.debug("Получаем самое длинное слово для заголовка: {}", maxLengthHeaderWord);

        String maxLengthDescriptionWord = Arrays.stream(description.split(SPACE))
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
        log.debug("Получаем самое длинное слово для описания: {}", maxLengthDescriptionWord);

        Graphics g = image.getGraphics();

        Font headerFont = null;

        log.debug("Адаптируем шрифт под заголовок");
        try {
            headerFont = createFontToFit(new Font(FONT, Font.BOLD, (image.getWidth() - X_MARGE * X_COUNT_MARGE) / maxLengthHeaderWord.length()), headerList.size(), maxLengthHeaderWord, image.getHeight() - Y_MARGE * X_COUNT_MARGE, image.getWidth() - X_MARGE_HEADER, image);
        } catch (IOException e) {
            throw new RuntimeException(e); //todo ошибку для  изменения шрифта
        }
        log.debug("Получили шрифт для заголовок размером {}", headerFont.getSize());
        FontMetrics metricsHeader = g.getFontMetrics(headerFont);

        log.debug("Создаем шрифт для описания");
        Font font = new Font(FONT, Font.PLAIN, (image.getWidth() - X_MARGE * X_COUNT_MARGE) / maxLengthDescriptionWord.length());
        log.debug("Получили шрифт для описания размером {}", font.getSize());

        FontMetrics metricsDescription = g.getFontMetrics(font);

        AttributedString attributedHeaderText = new AttributedString(header.concat(COMMA));
        attributedHeaderText.addAttribute(TextAttribute.FONT, headerFont);
        attributedHeaderText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);

        int positionXHeader = X_START_POSITION;
        int positionYHeader = metricsHeader.getAscent();
        log.debug("Размещаем на фото заголовок: {}", header.concat(COMMA));
        g.drawString(attributedHeaderText.getIterator(), positionXHeader, positionYHeader);

        List<String> descriptionList;
        Font newfont;
        while (true) {
            log.debug("Разбиваем описание на строки для фото");
            descriptionList = wrapper.doWrap(description, image.getWidth() - X_MARGE, metricsDescription);
            log.debug("Получили {} строк", descriptionList.size());
            log.debug("Считаем новый шрифт для фото");
            try {
                newfont = createFontToFit(font, descriptionList.size(), maxLengthDescriptionWord, image.getHeight() - metricsHeader.getHeight() - X_MARGE * (descriptionList.size() - ONE) - X_MARGE * Y_COUNT_MARGE, image.getWidth() -  X_MARGE * X_COUNT_MARGE, image);
            } catch (IOException e) {
                throw new RuntimeException(e); //todo ошибку для  изменения шрифта
            }
            log.debug("Размер старого шрифта {}. Размер нового шрифта {}", font.getSize(), newfont.getSize());
            if (font.getSize() == newfont.getSize() || font.getSize() - ONE == newfont.getSize()) {
                log.debug("Размер шрифта совпал");
                break;
            }
            font = newfont;
            metricsDescription = g.getFontMetrics(font);
        }
        Iterator descriptionIterator = descriptionList.iterator();
        int positionDescriptionX = X_START_POSITION;
        int positionDescriptionY = metricsDescription.getHeight() + metricsHeader.getHeight() + Y_MARGE * Y_COUNT_MARGE;
        while (descriptionIterator.hasNext()) {
            String descriptionText = (String) descriptionIterator.next();
            log.debug("Размещаем на фото строку описания: {}", descriptionText);
            AttributedString attributedDescriptionText = new AttributedString(descriptionText);
            attributedDescriptionText.addAttribute(TextAttribute.FONT, font);
            attributedDescriptionText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);

            g.drawString(attributedDescriptionText.getIterator(), positionDescriptionX, positionDescriptionY);
            positionDescriptionY += metricsDescription.getHeight();
        }

//        OutputStream os = new FileOutputStream("example.png");
//
//        ImageIO.write(image, "png", os);
        return encodeToString(image, PHOTO_FORMAT);


    }

    public static String encodeToString(BufferedImage image, String type) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, os);
            log.debug("Конвертируем фото в base64");
            return Base64.getEncoder().encodeToString(os.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Font createFontToFit(Font baseFont, int textSize, String maxLengthDescriptionWord, double imageHeight, double imageWidth, BufferedImage image) throws IOException {
        Font newFont = baseFont;

        FontMetrics ruler = image.getGraphics().getFontMetrics(baseFont);
        GlyphVector vector = baseFont.createGlyphVector(ruler.getFontRenderContext(), maxLengthDescriptionWord);

        Shape outline = vector.getOutline(X_START_POSITION, Y_START_POSITION);

        double expectedWidth = outline.getBounds().getWidth() + X_MARGE * X_COUNT_MARGE;
        double expectedHeight = outline.getBounds().getHeight() * textSize + Y_MARGE * (textSize - ONE);

        boolean textFits = imageWidth >= expectedWidth && imageHeight >= expectedHeight;
        log.debug("Влезет ли текст в фото {}", textFits);

        if (!textFits) {
            double widthBasedFontSize = (baseFont.getSize2D() * imageWidth) / expectedWidth;
            double heightBasedFontSize = (baseFont.getSize2D() * imageHeight) / expectedHeight;
            double newFontSize = Math.min(widthBasedFontSize, heightBasedFontSize);
            newFont = baseFont.deriveFont(baseFont.getStyle(), round(newFontSize));
        }
        return newFont;
    }


}
