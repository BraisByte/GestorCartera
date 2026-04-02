module com.example.gestorcartera {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.example.gestorcartera to javafx.fxml;
    exports com.example.gestorcartera;
}