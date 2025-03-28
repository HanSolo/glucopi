package eu.hansolo.fx.glucopi;

import eu.hansolo.fx.glucopi.fonts.Fonts;
import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.skins.ClockSkinBase;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static eu.hansolo.toolbox.unit.UnitDefinition.MILLIGRAM_PER_DECILITER;


public class GlucoPiDigitalSkin extends ClockSkinBase {
    private static final NetworkMonitor  networkMonitor = NetworkMonitor.INSTANCE;
    private              double          size;
    private              double          center;
    private              Pane            pane;
    private              Canvas          backgroundCanvas;
    private              GraphicsContext backgroundCtx;
    private              Canvas          foregroundCanvas;
    private              GraphicsContext foregroundCtx;
    private              Canvas          hoursCanvas;
    private              GraphicsContext hoursCtx;
    private              Canvas          minutesCanvas;
    private              GraphicsContext minutesCtx;
    private              Canvas          secondsCanvas;
    private              GraphicsContext secondsCtx;
    private              Color           tickBackgroundColor;
    private              Color           backgroundColor;
    private              Color           hourColor;
    private              Color           minuteColor;
    private              Color           fiveMinuteColor;
    private              Color           secondColor;
    private              Color           textColor;
    private              String          subText;
    private              boolean         isNight;
    private              List<Double>    deltas;
    private              double          deltaMin;
    private              double          deltaMax;


    // ******************** Constructors **************************************
    public GlucoPiDigitalSkin(Clock clock) {
        super(clock);
        tickBackgroundColor = (Color) clock.getBackgroundPaint();
        backgroundColor     = (Color) clock.getBackgroundPaint();
        hourColor           = clock.getHourColor();
        minuteColor         = clock.getMinuteColor();
        fiveMinuteColor     = minuteColor.darker();
        secondColor         = clock.getSecondColor();
        textColor           = clock.getTextColor();
        subText             = "";
        isNight             = false;
        this.deltas         = new ArrayList<>();
        this.deltaMin       = 401;
        this.deltaMax       = 0;

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        // Set initial size
        if (Double.compare(clock.getPrefWidth(), 0.0) <= 0 || Double.compare(clock.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(clock.getWidth(), 0.0) <= 0 || Double.compare(clock.getHeight(), 0.0) <= 0) {
            if (clock.getPrefWidth() > 0 && clock.getPrefHeight() > 0) {
                clock.setPrefSize(clock.getPrefWidth(), clock.getPrefHeight());
            } else {
                clock.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        backgroundCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        backgroundCtx = backgroundCanvas.getGraphicsContext2D();

        foregroundCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        foregroundCtx = foregroundCanvas.getGraphicsContext2D();

        hoursCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        hoursCtx = hoursCanvas.getGraphicsContext2D();

        minutesCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        minutesCtx = minutesCanvas.getGraphicsContext2D();

        secondsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        secondsCtx = secondsCanvas.getGraphicsContext2D();

        pane = new Pane(backgroundCanvas, foregroundCanvas, hoursCanvas, minutesCanvas, secondsCanvas);
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        getSkinnable().backgroundPaintProperty().addListener(o -> drawBackground());
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VISIBILITY".equals(EVENT_TYPE)) {

        } else if ("SECTION".equals(EVENT_TYPE)) {
            redraw();
        }
    }

    public void setSubText(final String subText) {
        this.subText = subText;
        drawForeground(ZonedDateTime.now());
    }

    public void setNight(final boolean isNight) { this.isNight = isNight; }

    public void setTickBackgroundColor(final Color tickBackgroundColor) {
        this.tickBackgroundColor = tickBackgroundColor;
        drawBackground();
    }

    public void setDeltas(final List<Double> deltas, final double deltaMin, final double deltaMax) {
        this.deltas   = deltas;
        this.deltaMin = deltaMin;
        this.deltaMax = deltaMax;
    }


    // ******************** Canvas ********************************************
    private void drawForeground(final ZonedDateTime TIME) {
        foregroundCtx.clearRect(0, 0, size, size);

        if (networkMonitor.isOffline()) {
            foregroundCtx.setFill(Color.WHITE);
            foregroundCtx.setTextBaseline(VPos.CENTER);
            foregroundCtx.setTextAlign(TextAlignment.CENTER);
            foregroundCtx.setFont(Fonts.sfProRoundedBold(size * 0.1));
            foregroundCtx.fillText("O F F L I N E", center, center);
        } else if (clock.isTextVisible()) {
            foregroundCtx.setFill(textColor);
            foregroundCtx.setTextBaseline(VPos.CENTER);
            foregroundCtx.setTextAlign(TextAlignment.CENTER);

            // Draw value
            foregroundCtx.setFont(Fonts.sfProRoundedBold(size * 0.17));
            foregroundCtx.setFill(textColor);
            foregroundCtx.fillText(clock.getText(), center, center);

            // Draw delta
            foregroundCtx.setFont(Fonts.sfProRoundedRegular(size * 0.1));
            foregroundCtx.fillText(this.subText, center, center + size * 0.15);

            // Draw delta chart
            foregroundCtx.setLineDashes();
            foregroundCtx.setStroke(textColor);
            foregroundCtx.setFont(Fonts.sfProRoundedRegular(size * 0.02));
            foregroundCtx.setLineWidth(0.5);
            double offsetX     = (480 - Constants.DELTA_CHART_WIDTH) * 0.5;
            double factorY     = Constants.DELTA_CHART_HEIGHT / Math.max(Math.abs(deltaMax), Math.abs(deltaMin));
            double boxWidth    = Constants.DELTA_CHART_WIDTH / 22;
            double spacer      = boxWidth;
            double zeroY       = center - size * 0.17;
            double textOffsetY = size * 0.02;
            if (this.deltas.size() > 0) {
                for (int i = 0; i < 12; i++) {
                    double delta     = this.deltas.get(i);
                    double boxHeight = Math.abs(delta * factorY);
                    //foregroundCtx.strokeRect(offsetX + i * (boxWidth + spacer), delta > 0 ? zeroY - boxHeight : zeroY, boxWidth, boxHeight);
                    foregroundCtx.fillRect(offsetX + i * (boxWidth + spacer), delta > 0 ? zeroY - boxHeight : zeroY, boxWidth, boxHeight);
                    if ((i + 1) % 2 == 0) {
                        foregroundCtx.fillText((delta > 0 ? "+" : "") + String.format("%.0f", delta), offsetX + i * (boxWidth + spacer) + boxWidth * 0.5, delta > 0 ? zeroY - boxHeight - textOffsetY : zeroY + boxHeight + textOffsetY);
                    }
                }
            }
            foregroundCtx.strokeLine(offsetX, zeroY, offsetX + Constants.DELTA_CHART_WIDTH, zeroY);

            // Draw outdated
            if (clock.isTitleVisible()) {
                foregroundCtx.setFont(Fonts.sfProRoundedBold(size * 0.05));
                foregroundCtx.fillText(clock.getTitle(), center, center - size * 0.275);
            }
        }
    }

    private void drawHours(final ZonedDateTime TIME) {
        int hourCounter = 1;
        int hour        = TIME.getHour();
        double strokeWidth = size * 0.06;
        hoursCtx.setLineCap(StrokeLineCap.BUTT);
        hoursCtx.clearRect(0, 0, size, size);
        for (int i = 450 ; i >= 90 ; i--) {
            hoursCtx.save();
            if (i % 30 == 0) {
                //draw hours
                hoursCtx.setStroke(hourColor);
                hoursCtx.setLineWidth(strokeWidth);
                if (hour == 0 || hour == 12) {
                    hoursCtx.strokeArc(strokeWidth * 0.5, strokeWidth * 0.5, size - strokeWidth, size - strokeWidth, i + 1 - 30, 28, ArcType.OPEN);
                } else if (hourCounter <= (TIME.get(ChronoField.AMPM_OF_DAY) == 1 ? hour - 12 : hour)) {
                    hoursCtx.strokeArc(strokeWidth * 0.5, strokeWidth * 0.5, size - strokeWidth, size - strokeWidth, i + 1 - 30, 28, ArcType.OPEN);
                    hourCounter++;
                }
            }
            hoursCtx.restore();
        }
    }

    private void drawMinutes(final ZonedDateTime TIME) {
        int minCounter  = 1;
        double strokeWidth = size * 0.06;
        minutesCtx.clearRect(0, 0, size, size);
        minutesCtx.setLineCap(StrokeLineCap.BUTT);
        for (int i = 450 ; i >= 90 ; i--) {
            minutesCtx.save();
            if (i % 6 == 0) {
                // draw minutes
                if (minCounter <= TIME.getMinute()) {
                    minutesCtx.setStroke(minCounter % 5 == 0 ? fiveMinuteColor : minuteColor);
                    minutesCtx.setLineWidth(strokeWidth);
                    minutesCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.1, strokeWidth * 0.5 + strokeWidth * 1.1, size - strokeWidth - strokeWidth * 2.2, size - strokeWidth - strokeWidth * 2.2, i + 1 - 6, 4, ArcType.OPEN);
                    minCounter++;
                }
            }
            minutesCtx.restore();
        }
    }

    private void drawSeconds(final ZonedDateTime TIME) {
        int secCounter  = 1;
        double strokeWidth = size * 0.06;
        secondsCtx.setLineCap(StrokeLineCap.BUTT);
        secondsCtx.clearRect(0, 0, size, size);
        for (int i = 450 ; i >= 90 ; i--) {
            secondsCtx.save();
            if (i % 6 == 0) {
                // draw seconds
                if (secCounter <= TIME.getSecond()) {
                    secondsCtx.setStroke(secondColor);
                    secondsCtx.setLineWidth(strokeWidth * 0.25);
                    secondsCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.8, strokeWidth * 0.5 + strokeWidth * 1.8, size - strokeWidth - strokeWidth * 3.6, size - strokeWidth - strokeWidth * 3.6, i + 1 - 6, 4, ArcType.OPEN);
                    secCounter++;
                }
            }
            secondsCtx.restore();
        }

        // Draw digital time
        secondsCtx.setTextAlign(TextAlignment.CENTER);
        secondsCtx.setTextBaseline(VPos.CENTER);
        secondsCtx.setFont(Fonts.sfProRoundedBold(size * 0.05));
        secondsCtx.setFill(textColor);
        secondsCtx.fillText(Constants.TIME_FORMATTER.format(TIME), center, center + size * 0.275);
    }

    private void drawBackground() {
        final double hueShift         = 0.0;
        final double saturationFactor = 0.85;
        final double brightnessFactor = 1.0;
        final double opacityFactor    = 1.0;
        final Color  darker           = this.backgroundColor.deriveColor(hueShift, saturationFactor, brightnessFactor, opacityFactor);

        double strokeWidth = size * 0.06;
        backgroundCtx.setLineCap(StrokeLineCap.BUTT);
        backgroundCtx.clearRect(0, 0, size, size);
        final Color backgroundColor = this.isNight ? Color.color(0.1, 0.1, 0.1, 1.0) : this.tickBackgroundColor;//darker;
        backgroundCtx.setStroke(backgroundColor);
        backgroundCtx.setLineWidth(strokeWidth);

        // Draw hours
        int hourCounter = 1;
        for (int hour = 0 ; hour < 13 ; hour++) {
            for (int i = 450 ; i >= 90 ; i--) {
                backgroundCtx.save();
                if (i % 30 == 0) {
                    //draw hours
                    if (hour == 0 || hour == 12) {
                        backgroundCtx.strokeArc(strokeWidth * 0.5, strokeWidth * 0.5, size - strokeWidth, size - strokeWidth, i + 1 - 30, 28, ArcType.OPEN);
                    } else if (hourCounter <= hour) {
                        backgroundCtx.strokeArc(strokeWidth * 0.5, strokeWidth * 0.5, size - strokeWidth, size - strokeWidth, i + 1 - 30, 28, ArcType.OPEN);
                        hourCounter++;
                    }
                }
                backgroundCtx.restore();
            }
        }

        // Draw minutes
        int minuteCounter = 0;
        for (int minute = 0 ; minute < 60 ; minute++) {
            for (int i = 450 ; i >= 90 ; i--) {
                backgroundCtx.save();
                if (i % 6 == 0) {
                    backgroundCtx.setLineWidth(strokeWidth);
                    backgroundCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.1, strokeWidth * 0.5 + strokeWidth * 1.1, size - strokeWidth - strokeWidth * 2.2, size - strokeWidth - strokeWidth * 2.2, i + 1 - 6, 4, ArcType.OPEN);
                    minuteCounter++;
                    if (minuteCounter == 60) { break; }
                }
                backgroundCtx.restore();
            }
        }

        // Draw seconds
        int secCounter  = 0;
        for (int second = 0 ; second < 60 ; second++) {
            for (int i = 450 ; i >= 90 ; i--) {
                backgroundCtx.save();
                if (i % 6 == 0) {
                    backgroundCtx.setLineWidth(strokeWidth * 0.25);
                    backgroundCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.8, strokeWidth * 0.5 + strokeWidth * 1.8, size - strokeWidth - strokeWidth * 3.6, size - strokeWidth - strokeWidth * 3.6, i + 1 - 6, 4, ArcType.OPEN);
                    secCounter++;
                    if (secCounter == 60) { break; }
                }
                backgroundCtx.restore();
            }
        }
    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        //drawForeground(TIME);
        drawHours(TIME);
        drawMinutes(TIME);
        drawSeconds(TIME);
    }

    @Override public void updateAlarms() {}


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        double width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        double height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();
        size          = width < height ? width : height;
        center        = size * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((clock.getWidth() - size) * 0.5, (clock.getHeight() - size) * 0.5);

            backgroundCanvas.setWidth(size);
            backgroundCanvas.setHeight(size);
            foregroundCanvas.setWidth(size);
            foregroundCanvas.setHeight(size);
            hoursCanvas.setWidth(size);
            hoursCanvas.setHeight(size);
            minutesCanvas.setWidth(size);
            minutesCanvas.setHeight(size);
            secondsCanvas.setWidth(size);
            secondsCanvas.setHeight(size);
            drawBackground();
        }
    }

    @Override protected void redraw() {
        this.backgroundColor = (Color) clock.getBackgroundPaint();

        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(backgroundColor, new CornerRadii(1024), Insets.EMPTY)));

        final ZonedDateTime time             = clock.getTime();
        final double        hueShift         = 0.0;
        final double        saturationFactor = 0.5;
        final double        brightnessFactor = 1.2;
        final double        opacityFactor    = 1.0;
        final Color         brighter         = backgroundColor.deriveColor(hueShift, saturationFactor, brightnessFactor, opacityFactor);

        hourColor       = isNight ? clock.getHourColor()              : brighter;
        minuteColor     = isNight ? clock.getMinuteColor()            : brighter;
        fiveMinuteColor = isNight ? clock.getMinuteColor().brighter() : brighter.brighter();
        secondColor     = isNight ? clock.getSecondColor()            : brighter;
        textColor       = clock.getTextColor();

        drawForeground(time);
        drawHours(time);
        drawMinutes(time);
        drawSeconds(time);
    }
}
