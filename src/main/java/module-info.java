module eu.hansolo.fx.glucopi {
    // Java
    requires java.desktop;

    // Java-FX
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.swing;
    requires transitive javafx.fxml;

    // 3rd Party
    requires transitive eu.hansolo.toolboxfx;
    requires transitive eu.hansolo.medusa;
    requires transitive eu.hansolo.nightscoutconnector;
    requires transitive eu.hansolo.jdktools;
    requires java.net.http;

    exports eu.hansolo.fx.glucopi;
}