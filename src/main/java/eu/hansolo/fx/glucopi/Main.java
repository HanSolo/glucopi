package eu.hansolo.fx.glucopi;

import eu.hansolo.fx.glucopi.Constants.Glucose;
import eu.hansolo.fx.glucopi.Constants.Trend;
import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.ClockBuilder;
import eu.hansolo.nightscoutconnector.Connector;
import eu.hansolo.nightscoutconnector.Entry;
import eu.hansolo.toolbox.properties.ObjectProperty;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;


public class Main extends Application {
    private Clock                 clock;
    private GlucoPiDigitalSkin    clockSkin;
    private StackPane             pane;
    private ObjectProperty<Entry> currentEntry = new ObjectProperty<>();
    private BooleanProperty       isBlinking   = new SimpleBooleanProperty(false);
    private BooleanProperty       isNight      = new SimpleBooleanProperty(false);
    private long                  lastBlinkCalled;
    private long                  lastTimerCalled;
    private AnimationTimer        timer;
    private boolean               blink;
    private Entry                 lastEntry;


    @Override public void init() {
        this.clock = ClockBuilder.create()
                                 .areasVisible(false)
                                 .minuteTickMarksVisible(false)
                                 .backgroundPaint(Constants.DAY_BACKGROUND)
                                 .knobColor(Constants.DAY_FOREGROUND)
                                 .hourColor(Constants.DAY_FOREGROUND)
                                 .minuteColor(Constants.DAY_FOREGROUND)
                                 .secondColor(Constants.DAY_FOREGROUND)
                                 .hourTickMarkColor(Constants.DAY_FOREGROUND)
                                 .title("\u26A0")
                                 .titleVisible(true)
                                 .titleColor(Constants.DAY_FOREGROUND)
                                 .text("")
                                 .textVisible(true)
                                 .running(true)
                                 .build();

        clockSkin = new GlucoPiDigitalSkin(this.clock);
        this.clock.setSkin(clockSkin);

        this.pane = new StackPane(this.clock);

        this.lastBlinkCalled = System.nanoTime();
        this.lastTimerCalled = System.nanoTime() - 60_000_000_001l;
        this.timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now - lastTimerCalled > 60_000_000_000l) {
                    try {
                        final Entry entry = Connector.getCurrentEntry(Constants.NIGHTSCOUT_URL, "", "");
                        if (entry != null) {
                            currentEntry.set(entry);
                        }
                    } catch (AccessDeniedException ex) {}
                    lastTimerCalled = now;
                }
                if (isBlinking.get() && now - lastBlinkCalled > 1_000_000_000) {
                    if (blink) {
                        clock.setBackgroundPaint(Color.TRANSPARENT);
                        clock.setHourColor(Color.TRANSPARENT);
                        clock.setMinuteColor(Color.TRANSPARENT);
                        clock.setSecondColor(Color.TRANSPARENT);
                        if (isNight.get()) {
                            clock.setTextColor(Constants.RED);
                        }
                    } else {
                        if (isNight.get()) {
                            clock.setBackgroundPaint(Constants.RED);
                            clock.setTextColor(Constants.NIGHT_BACKGROUND);
                            clock.setHourColor(Constants.NIGHT_FOREGROUND);
                            clock.setMinuteColor(Constants.NIGHT_FOREGROUND);
                            clock.setSecondColor(Constants.NIGHT_FOREGROUND);
                        } else {
                            clock.setBackgroundPaint(Constants.DAY_BACKGROUND);
                            clock.setHourColor(Constants.DAY_FOREGROUND);
                            clock.setMinuteColor(Constants.DAY_FOREGROUND);
                            clock.setSecondColor(Constants.DAY_FOREGROUND);
                        }
                    }
                    blink ^= true;
                    lastBlinkCalled = now;
                }
                final int hour = LocalDateTime.now().getHour();
                isNight.set(hour > Constants.NIGHT_STARTS_AT || hour < Constants.NIGHT_ENDS_AT);
            }
        };
        this.blink     = false;
        this.lastEntry = null;

        registerListeners();
    }

    private void registerListeners() {
        this.currentEntry.addObserver(evt -> {
            final Entry  entry     = evt.getValue();
            final double value     = entry.sgv();
            final double delta     = this.lastEntry == null ? 0 : value - this.lastEntry.sgv();
            final long   deltaTime = lastEntry == null ? 0 : entry.datelong() - lastEntry.datelong();

            this.clock.setTitleVisible(deltaTime > 600);

            Trend trend  = Trend.getFromText(entry.direction());

            if (delta < 0) {
                if (Trend.DOUBLE_UP == trend) {
                    trend = Trend.DOUBLE_DOWN;
                } else if (Trend.SINGLE_UP == trend) {
                    trend = Trend.SINGLE_DOWN;
                } else if (Trend.FORTY_FIVE_UP == trend) {
                    trend = Trend.FORTY_FIVE_DOWN;
                }
            }

            this.clock.setText(String.format("%.0f", value) + " " + trend.getSymbol());
            if (isNight.get()) {
                if (value > Glucose.TOO_HIGH.value || value < Glucose.TOO_LOW.value) {
                    this.clock.setTextColor(Constants.RED);
                } else {
                    this.clock.setTextColor(Constants.NIGHT_FOREGROUND);
                }
            } else {
                if (value > Glucose.TOO_HIGH.value) {
                    this.clock.setTextColor(Glucose.TOO_HIGH.color);
                    this.isBlinking.set(true);
                } else if (value > Glucose.ACCEPTABLE_HIGH.value) {
                    this.clock.setTextColor(Glucose.ACCEPTABLE_HIGH.color);
                    this.isBlinking.set(false);
                } else if (value > Glucose.HIGH.value) {
                    this.clock.setTextColor(Glucose.HIGH.color);
                    this.isBlinking.set(false);
                } else if (value > Glucose.LOW.value) {
                    this.clock.setTextColor(Constants.GREEN);
                    this.isBlinking.set(false);
                } else if (value < Glucose.TOO_LOW.value) {
                    this.clock.setTextColor(Glucose.TOO_LOW.color);
                    this.isBlinking.set(true);
                } else if (value < Glucose.ACCEPTABLE_LOW.value) {
                    this.clock.setTextColor(Glucose.ACCEPTABLE_LOW.color);
                    this.isBlinking.set(true);
                } else if (value < Glucose.LOW.value) {
                    this.clock.setTextColor(Glucose.LOW.color);
                    this.isBlinking.set(false);
                }
                this.clockSkin.setSubText((delta > 0 ? "+" : "") + String.format("%.0f", delta));
            }
            this.lastEntry = entry;
        });

        this.isBlinking.addListener(o -> {
            if (!isBlinking.get()) {
                if (isNight.get()) {
                    this.clock.setBackgroundPaint(Constants.NIGHT_BACKGROUND);
                    this.clock.setHourColor(Constants.NIGHT_FOREGROUND);
                    this.clock.setMinuteColor(Constants.NIGHT_FOREGROUND);
                    this.clock.setKnobColor(Constants.NIGHT_FOREGROUND);
                    this.clock.setTextColor(Constants.NIGHT_FOREGROUND);
                } else {
                    this.clock.setBackgroundPaint(Constants.DAY_BACKGROUND);
                    this.clock.setHourColor(Constants.DAY_FOREGROUND);
                    this.clock.setMinuteColor(Constants.DAY_FOREGROUND);
                    this.clock.setKnobColor(Constants.DAY_FOREGROUND);
                }
            }
        });

        this.isNight.addListener((o, ov, nv) -> {
           if (nv) {
               this.clock.setBackgroundPaint(Constants.NIGHT_BACKGROUND);
               this.clock.setHourColor(Constants.NIGHT_FOREGROUND);
               this.clock.setMinuteColor(Constants.NIGHT_FOREGROUND);
               this.clock.setKnobColor(Constants.NIGHT_FOREGROUND);
           } else {
               this.clock.setBackgroundPaint(Constants.DAY_BACKGROUND);
               this.clock.setHourColor(Constants.DAY_FOREGROUND);
               this.clock.setMinuteColor(Constants.DAY_FOREGROUND);
               this.clock.setKnobColor(Constants.DAY_FOREGROUND);
           }
        });
    }

    @Override public void start(final Stage stage) throws Exception {
        Scene scene = new Scene(pane, 480, 480);

        stage.setScene(scene);
        stage.setTitle("GlucoPi");
        //stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
        stage.centerOnScreen();

        timer.start();
    }

    @Override public void stop() {
        Platform.exit();
        System.exit(0);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
