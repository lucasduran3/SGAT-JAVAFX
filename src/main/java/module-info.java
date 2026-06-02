module com.mvcjava.sagt.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires de.jensd.fx.glyphs.fontawesome;
    requires jbcrypt;

    opens com.mvcjava.sagt.javafx              to javafx.fxml;
    opens com.mvcjava.sagt.javafx.view         to javafx.fxml;
    opens com.mvcjava.sagt.javafx.controller   to javafx.fxml;
    opens com.mvcjava.sagt.javafx.auth         to javafx.fxml;

    exports com.mvcjava.sagt.javafx;
}