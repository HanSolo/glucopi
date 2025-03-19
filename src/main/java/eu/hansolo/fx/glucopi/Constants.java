package eu.hansolo.fx.glucopi;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;


public final class Constants {
    public static final String            HOME_FOLDER             = new StringBuilder(System.getProperty("user.home")).append(File.separator).toString();
    public static final int               NIGHT_STARTS_AT         = 20;
    public static final int               NIGHT_ENDS_AT           = 6;

    public static final int               PING_INTERVAL_IN_SEC    = 5;
    public static final String            TEST_CONNECTIVITY_URL   = "https://apple.com";

    public static final Color             NIGHT_BACKGROUND        = Color.BLACK; //Color.rgb(128, 0, 0);
    public static final Color             NIGHT_FOREGROUND        = Color.rgb(128, 0, 0);
    public static final Color             NIGHT_BRIGHT_FOREGROUND = Color.rgb(255, 0, 0);
    public static final Color             DAY_BACKGROUND          = Color.rgb(200, 200, 200);
    public static final Color             DAY_FOREGROUND          = Color.WHITE;
    public static final Color             DIM_BLACK               = Color.color(0.0, 0.0, 0.0, 0.5);
    public static final Background        DIM_OVERLAY             = new Background(new BackgroundFill(DIM_BLACK, new CornerRadii(240), Insets.EMPTY));
    public static final Background        TRANSPARENT_OVERLAY     = new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(240), Insets.EMPTY));

    public static final double            DELTA_CHART_WIDTH       = 480 * 0.36;
    public static final double            DELTA_CHART_HEIGHT      = DELTA_CHART_WIDTH * 0.2;

    public static final DateTimeFormatter TIME_FORMATTER          = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final Color             RED                     = Color.color(0.996, 0.000, 0.000); // RGB 254,   0, 0
    public static final Color             ORANGE                  = Color.color(1.000, 0.365, 0.004); // RGB 255,  93, 0
    public static final Color             YELLOW                  = Color.color(1.000, 0.659, 0.000); // RGB 255, 168, 0
    public static final Color             GREEN                   = Color.color(0.000, 0.761, 0.004); //   0, 194, 1
    public static final Color             DARK_GREEN              = Color.color(0.00, 0.50, 0.13);

    public enum Glucose {
        TOO_HIGH(250, RED, RED.deriveColor(0.0, 1.0, 0.9, 1.0)),
        ACCEPTABLE_HIGH(180, ORANGE, ORANGE.deriveColor(0.0, 1.0, 0.95, 1.0)),
        HIGH(140, YELLOW, YELLOW.deriveColor(0.0, 1.0, 0.95, 1.0)),
        LOW(70, YELLOW, YELLOW.deriveColor(0.0, 1.0, 0.95, 1.0)),
        ACCEPTABLE_LOW(65, ORANGE, ORANGE.deriveColor(0.0, 1.0, 0.95, 1.0)),
        TOO_LOW(55, RED, RED.deriveColor(0.0, 1.0, 0.9, 1.0));

        public final double value;
        public final Color color;
        public final Color backgroundColor;

        Glucose(final double value, final Color color, final Color backgroundColor) {
            this.value           = value;
            this.color           = color;
            this.backgroundColor = backgroundColor;
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
