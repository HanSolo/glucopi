package eu.hansolo.fx.glucopi.fonts;

import javafx.scene.text.Font;


public class Fonts {
    private static final String SF_PRO_ROUNDED_REGULAR_NAME;
    private static final String SF_PRO_ROUNDED_SEMI_BOLD_NAME;
    private static final String SF_PRO_ROUNDED_BOLD_NAME;
    private static final String SF_PRO_TEXT_REGULAR_NAME;
    private static final String SF_PRO_TEXT_BOLD_NAME;
    private static final String SF_COMPACT_DISPLAY_MEDIUM_NAME;
    private static final String SF_COMPACT_DISPLAY_BOLD_NAME;
    private static       String sfProRoundedSemiBoldName;
    private static       String sfProRoundedRegularName;
    private static       String sfProRoundedBoldName;
    private static       String sfProTextRegularName;
    private static       String sfProTextBoldName;
    private static       String sfCompactDisplayMediumName;
    private static       String sfCompactDisplayBoldName;

    static {
        try {
            sfProRoundedRegularName    = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/glucopi/fonts/SF-Pro-Rounded-Regular.ttf"), 10).getName();
            sfProRoundedSemiBoldName   = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/glucopi/fonts/SF-Pro-Rounded-Semibold.ttf"), 10).getName();
            sfProRoundedBoldName       = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/glucopi/fonts/SF-Pro-Rounded-Bold.ttf"), 10).getName();
            sfProTextRegularName       = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/glucopi/fonts/SF-Pro-Text-Regular.ttf"), 10).getName();
            sfProTextBoldName          = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/glucopi/fonts/SF-Pro-Text-Bold.ttf"), 10).getName();
            sfCompactDisplayMediumName = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/glucopi/fonts/SF-Compact-Display-Medium.ttf"), 10).getName();
            sfCompactDisplayBoldName   = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/glucopi/fonts/SF-Compact-Display-Bold.ttf"), 10).getName();
        } catch (Exception exception) { }
        SF_PRO_ROUNDED_REGULAR_NAME    = sfProRoundedRegularName;
        SF_PRO_ROUNDED_SEMI_BOLD_NAME  = sfProRoundedSemiBoldName;
        SF_PRO_ROUNDED_BOLD_NAME       = sfProRoundedBoldName;
        SF_PRO_TEXT_REGULAR_NAME       = sfProTextRegularName;
        SF_PRO_TEXT_BOLD_NAME          = sfProTextBoldName;
        SF_COMPACT_DISPLAY_MEDIUM_NAME = sfCompactDisplayMediumName;
        SF_COMPACT_DISPLAY_BOLD_NAME   = sfCompactDisplayBoldName;
    }


    // ******************** Methods *******************************************
    public static Font sfProRoundedRegular(final double size) { return new Font(SF_PRO_ROUNDED_REGULAR_NAME, size); }
    public static Font sfProRoundedSemiBold(final double size) { return new Font(SF_PRO_ROUNDED_SEMI_BOLD_NAME, size); }
    public static Font sfProRoundedBold(final double size) { return new Font(SF_PRO_ROUNDED_BOLD_NAME, size); }
    public static Font sfProTextRegular(final double size) { return new Font(SF_PRO_TEXT_REGULAR_NAME, size); }
    public static Font sfProTextBold(final double size) { return new Font(SF_PRO_TEXT_BOLD_NAME, size); }
    public static Font sfCompactDisplayMedium(final double size) { return new Font(SF_COMPACT_DISPLAY_MEDIUM_NAME, size); }
    public static Font sfCompactDisplayBold(final double size) { return new Font(SF_COMPACT_DISPLAY_BOLD_NAME, size); }
}
