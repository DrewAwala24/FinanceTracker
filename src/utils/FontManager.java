package utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FontManager {

    private static Font montserratRegular;
    private static Font montserratBold;
    private static Font montserratItalic;

    static {
        try {
            // Load fonts from the fonts folder
            File fontFile = new File("fonts/MontserratAlternates-Regular.ttf");
            montserratRegular = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);

            fontFile = new File("fonts/MontserratAlternates-Bold.ttf");
            montserratBold = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);

            fontFile = new File("fonts/MontserratAlternates-Italic.ttf");
            montserratItalic = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(12f);

            // Register the fonts with the graphics environment
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(montserratRegular);
            ge.registerFont(montserratBold);
            ge.registerFont(montserratItalic);

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // Fallback to default fonts if loading fails
            montserratRegular = new Font("Arial", Font.PLAIN, 14);
            montserratBold = new Font("Arial", Font.BOLD, 14);
            montserratItalic = new Font("Arial", Font.ITALIC, 12);
        }
    }

    public static Font getRegularFont(float size) {
        return montserratRegular.deriveFont(size);
    }

    public static Font getBoldFont(float size) {
        return montserratBold.deriveFont(size);
    }

    public static Font getItalicFont(float size) {
        return montserratItalic.deriveFont(size);
    }
}