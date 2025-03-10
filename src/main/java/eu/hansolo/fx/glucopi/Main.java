package eu.hansolo.fx.glucopi;

import eu.hansolo.fx.glucopi.Constants.Glucose;
import eu.hansolo.fx.glucopi.Constants.Trend;
import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;
import eu.hansolo.nightscoutconnector.Connector;
import eu.hansolo.nightscoutconnector.Entry;
import eu.hansolo.toolbox.properties.ObjectProperty;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static eu.hansolo.toolbox.unit.UnitDefinition.MILLIGRAM_PER_DECILITER;
import static eu.hansolo.toolbox.unit.UnitDefinition.MILLIMOL_PER_LITER;


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
    private List<Double>          deltas;
    private double                deltaMin;
    private double                deltaMax;


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
                                 .textColor(Constants.DAY_FOREGROUND)
                                 .running(true)
                                 .build();

        clockSkin = new GlucoPiDigitalSkin(this.clock);
        this.clock.setSkin(clockSkin);

        this.pane = new StackPane(this.clock);
        this.pane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

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
                            //clock.setBackgroundPaint(Constants.RED);
                            clock.setTextColor(Constants.NIGHT_BACKGROUND);
                            clock.setHourColor(Constants.NIGHT_FOREGROUND);
                            clock.setMinuteColor(Constants.NIGHT_FOREGROUND);
                            clock.setSecondColor(Constants.NIGHT_FOREGROUND);
                            clockSkin.setNight(true);
                        } else {
                            final double value = currentEntry.getValue().sgv();
                            if (value > Glucose.TOO_HIGH.value) {
                                clock.setBackgroundPaint(Glucose.TOO_HIGH.color);
                            } else if (value > Glucose.ACCEPTABLE_HIGH.value) {
                                clock.setBackgroundPaint(Glucose.ACCEPTABLE_HIGH.color);
                            } else if (value > Glucose.HIGH.value) {
                                clock.setBackgroundPaint(Glucose.HIGH.color);
                            } else if (value > Glucose.LOW.value) {
                                clock.setBackgroundPaint(Constants.GREEN);
                            } else if (value < Glucose.TOO_LOW.value) {
                                clock.setBackgroundPaint(Glucose.TOO_LOW.color);
                            } else if (value < Glucose.ACCEPTABLE_LOW.value) {
                                clock.setBackgroundPaint(Glucose.ACCEPTABLE_LOW.color);
                            } else if (value < Glucose.LOW.value) {
                                clock.setBackgroundPaint(Glucose.LOW.color);
                            }
                            clockSkin.setNight(false);
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
        this.deltas    = new ArrayList<>();
        this.deltaMin  = 401;
        this.deltaMax  = 0;

        getInitialValues();

        registerListeners();
    }

    private void registerListeners() {
        this.currentEntry.addObserver(evt -> {
            final Entry  entry     = evt.getValue();
            final double value     = entry.sgv();
            final double delta     = this.lastEntry == null ? 0 : value - this.lastEntry.sgv();
            final long   deltaTime = lastEntry == null ? 0 : entry.datelong() - lastEntry.datelong();

            if (this.deltas.size() == 12) { this.deltas.removeFirst(); }
            this.deltas.add(delta);
            this.deltaMin = deltas.stream().min(Comparator.naturalOrder()).get();
            this.deltaMax = deltas.stream().max(Comparator.naturalOrder()).get();
            this.clockSkin.setDeltas(this.deltas, this.deltaMin, this.deltaMax);

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
                this.clockSkin.setNight(true);
                this.clock.setBackgroundPaint(Constants.NIGHT_BACKGROUND);
                if (value > Glucose.TOO_HIGH.value || value < Glucose.TOO_LOW.value) {
                    this.clock.setTextColor(Constants.RED);
                } else {
                    this.clock.setTextColor(Constants.NIGHT_FOREGROUND);
                }
            } else {
                if (value > Glucose.TOO_HIGH.value) {
                    this.clock.setBackgroundPaint(Glucose.TOO_HIGH.color);
                    this.isBlinking.set(true);
                } else if (value > Glucose.ACCEPTABLE_HIGH.value) {
                    this.clock.setBackgroundPaint(Glucose.ACCEPTABLE_HIGH.color);
                    this.isBlinking.set(false);
                } else if (value > Glucose.HIGH.value) {
                    this.clock.setBackgroundPaint(Glucose.HIGH.color);
                    this.isBlinking.set(false);
                } else if (value > Glucose.LOW.value) {
                    this.clock.setBackgroundPaint(Constants.GREEN);
                    this.isBlinking.set(false);
                } else if (value < Glucose.TOO_LOW.value) {
                    this.clock.setBackgroundPaint(Glucose.TOO_LOW.color);
                    this.isBlinking.set(true);
                } else if (value < Glucose.ACCEPTABLE_LOW.value) {
                    this.clock.setBackgroundPaint(Glucose.ACCEPTABLE_LOW.color);
                    this.isBlinking.set(true);
                } else if (value < Glucose.LOW.value) {
                    this.clock.setBackgroundPaint(Glucose.LOW.color);
                    this.isBlinking.set(false);
                }
                this.clockSkin.setSubText((delta > 0 ? "+" : "") + String.format("%.0f", delta));
            }
            this.lastEntry = entry;

            this.clockSkin.redraw();
        });

        this.isBlinking.addListener(o -> {
            if (!isBlinking.get()) {
                if (isNight.get()) {
                    this.clockSkin.setNight(true);
                    this.clock.setBackgroundPaint(Constants.NIGHT_BACKGROUND);
                    this.clock.setHourColor(Constants.NIGHT_FOREGROUND);
                    this.clock.setMinuteColor(Constants.NIGHT_FOREGROUND);
                    this.clock.setTextColor(Constants.NIGHT_FOREGROUND);
                } else {
                    final double value = this.currentEntry.getValue().sgv();
                    if (value > Glucose.TOO_HIGH.value) {
                        this.clock.setBackgroundPaint(Glucose.TOO_HIGH.color);
                    } else if (value > Glucose.ACCEPTABLE_HIGH.value) {
                        this.clock.setBackgroundPaint(Glucose.ACCEPTABLE_HIGH.color);
                    } else if (value > Glucose.HIGH.value) {
                        this.clock.setBackgroundPaint(Glucose.HIGH.color);
                    } else if (value > Glucose.LOW.value) {
                        this.clock.setBackgroundPaint(Constants.GREEN);
                    } else if (value < Glucose.TOO_LOW.value) {
                        this.clock.setBackgroundPaint(Glucose.TOO_LOW.color);
                    } else if (value < Glucose.ACCEPTABLE_LOW.value) {
                        this.clock.setBackgroundPaint(Glucose.ACCEPTABLE_LOW.color);
                    } else if (value < Glucose.LOW.value) {
                        this.clock.setBackgroundPaint(Glucose.LOW.color);
                    }
                    this.clock.setHourColor(Constants.DAY_FOREGROUND);
                    this.clock.setMinuteColor(Constants.DAY_FOREGROUND);
                    this.clock.setTextColor(Constants.DAY_FOREGROUND);
                    this.clockSkin.setNight(false);
                }
            }
        });

        this.isNight.addListener((o, ov, nv) -> {
           if (nv) {
               this.clockSkin.setNight(true);
               this.clock.setBackgroundPaint(Constants.NIGHT_BACKGROUND);
               this.clock.setHourColor(Constants.NIGHT_FOREGROUND);
               this.clock.setMinuteColor(Constants.NIGHT_FOREGROUND);
               this.clock.setSecondColor(Constants.NIGHT_FOREGROUND);
               this.clock.setTextColor(Constants.NIGHT_FOREGROUND);
           } else {
               final double value = this.currentEntry.getValue().sgv();
               if (value > Glucose.TOO_HIGH.value) {
                   this.clock.setBackgroundPaint(Glucose.TOO_HIGH.color);
               } else if (value > Glucose.ACCEPTABLE_HIGH.value) {
                   this.clock.setBackgroundPaint(Glucose.ACCEPTABLE_HIGH.color);
               } else if (value > Glucose.HIGH.value) {
                   this.clock.setBackgroundPaint(Glucose.HIGH.color);
               } else if (value > Glucose.LOW.value) {
                   this.clock.setBackgroundPaint(Constants.GREEN);
               } else if (value < Glucose.TOO_LOW.value) {
                   this.clock.setBackgroundPaint(Glucose.TOO_LOW.color);
               } else if (value < Glucose.ACCEPTABLE_LOW.value) {
                   this.clock.setBackgroundPaint(Glucose.ACCEPTABLE_LOW.color);
               } else if (value < Glucose.LOW.value) {
                   this.clock.setBackgroundPaint(Glucose.LOW.color);
               }
               this.clock.setHourColor(Constants.DAY_FOREGROUND);
               this.clock.setMinuteColor(Constants.DAY_FOREGROUND);
               this.clock.setSecondColor(Constants.DAY_FOREGROUND);
               this.clock.setTextColor(Constants.DAY_FOREGROUND);
               this.clockSkin.setNight(false);
           }
        });
    }

    @Override public void start(final Stage stage) throws Exception {
        Scene scene = new Scene(pane, 480, 480);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.setTitle("GlucoPi");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
        stage.centerOnScreen();

        timer.start();
    }

    @Override public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private void getInitialValues() {
        final List<Entry> entries = Connector.getLastNEntries(15, Constants.NIGHTSCOUT_URL, "", "");
        if (entries.size() > 13) {
            for (int i = 12; i > 0; i--) {
                double delta;
                delta = entries.get(i - 1).sgv() - entries.get(i).sgv();
                this.deltas.add(delta);
            }
            this.deltaMin = deltas.stream().min(Comparator.naturalOrder()).get();
            this.deltaMax = deltas.stream().max(Comparator.naturalOrder()).get();
        }
        this.clockSkin.setDeltas(this.deltas, this.deltaMin, this.deltaMax);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
