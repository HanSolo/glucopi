package eu.hansolo.fx.glucopi;

import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Optional;


public final class Constants {
    public static final String NIGHTSCOUT_URL         = "https://glucose-anton.herokuapp.com";

    public static final int    NIGHT_STARTS_AT        = 20;
    public static final int    NIGHT_ENDS_AT          = 6;

    public static final Color NIGHT_BACKGROUND        = Color.BLACK; //Color.rgb(128, 0, 0);
    public static final Color NIGHT_FOREGROUND        = Color.rgb(128, 0, 0);
    public static final Color NIGHT_BRIGHT_FOREGROUND = Color.rgb(255, 0, 0);
    public static final Color DAY_BACKGROUND          = Color.rgb(200, 200, 200);
    public static final Color DAY_FOREGROUND          = Color.WHITE;

    public static final Color RED                     = Color.color(0.996, 0.000, 0.000); // RGB 254,   0, 0
    public static final Color ORANGE                  = Color.color(1.000, 0.365, 0.004); // RGB 255,  93, 0
    public static final Color YELLOW                  = Color.color(1.000, 0.659, 0.000); // RGB 255, 168, 0
    public static final Color GREEN                   = Color.color(0.000, 0.761, 0.004); //   0, 194, 1
    public static final Color DARK_GREEN              = Color.color(0.00, 0.50, 0.13);

    public enum Glucose {
        TOO_HIGH(250, RED),
        ACCEPTABLE_HIGH(180, ORANGE),
        HIGH(140, YELLOW),
        LOW(70, YELLOW),
        ACCEPTABLE_LOW(65, ORANGE),
        TOO_LOW(55, RED);

        public final double value;
        public final Color color;

        Glucose(final double value, final Color color) {
            this.value = value;
            this.color = color;
        }

    }

    public enum Trend {
        FLAT("Flat", 0, "\u2192"),
        SINGLE_UP("SingleUp", 1, "\u2191"),
        DOUBLE_UP("DoubleUp", 2, "\u2191\u2191"),
        DOUBLE_DOWN("DoubleDown", 3, "\u2193\u2193"),
        SINGLE_DOWN("SingleDown", 4, "\u2193"),
        FORTY_FIVE_DOWN("FortyFiveDown", 5, "\u2198"),
        FORTY_FIVE_UP("FortyFiveUp", 6, "\u2197"),
        NONE("", 7, "");

        private final String textKey;
        private final int    key;
        private final String symbol;


        // ******************** Constructors **************************************
        Trend(final String textKey, final int key, final String symbol) {
            this.textKey   = textKey;
            this.key       = key;
            this.symbol    = symbol;
        }


        // ******************** Methods *******************************************
        public String getTextKey() { return textKey; }

        public int getKey() { return key; }

        public String getSymbol() { return symbol; }

        public static Trend getFromText(final String text) {
            Optional<Trend> optTrend = Arrays.stream(Trend.values()).filter(trend -> trend.getTextKey().toLowerCase().equals(text.toLowerCase())).findFirst();
            if (optTrend.isPresent()) {
                return optTrend.get();
            } else {
                optTrend = Arrays.stream(Trend.values()).filter(trend -> Integer.toString(trend.getKey()).equals(text)).findFirst();
                if (optTrend.isPresent()) {
                    return optTrend.get();
                } else {
                    return NONE;
                }
            }
        }
    }

}
