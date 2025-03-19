package eu.hansolo.fx.glucopi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public enum NetworkMonitor {
    INSTANCE;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private BooleanProperty          online          = new SimpleBooleanProperty(false);
    private BooleanProperty          offline         = new SimpleBooleanProperty(true);


    NetworkMonitor() {
        executorService.scheduleAtFixedRate(() -> ping(), Constants.PING_INTERVAL_IN_SEC, Constants.PING_INTERVAL_IN_SEC, TimeUnit.SECONDS);
    }


    public boolean isOnline()                       { return online.get(); }
    public ReadOnlyBooleanProperty onlineProperty() { return online; }

    public boolean isOffline() { return offline.get(); }
    public ReadOnlyBooleanProperty offlineProperty() { return offline; }

    private void ping() {
        HttpResponse<String> response = Helper.httpHeadRequestSync(Constants.TEST_CONNECTIVITY_URL);
        if (null == response || response.statusCode() > 202 || response.statusCode() < 200) {
            online.set(false);
            offline.set(true);
        } else {
            online.set(true);
            offline.set(false);
        }
    }
}

