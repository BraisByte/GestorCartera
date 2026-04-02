package com.example.gestorcartera;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        stage.setTitle("Gestor de Cartera");
        stage.setScene(scene);
        var logoUrl = HelloApplication.class.getResourceAsStream("logo.png");
        if (logoUrl != null) stage.getIcons().add(new Image(logoUrl));
        stage.show();
    }
}