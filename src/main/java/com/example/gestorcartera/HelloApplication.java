package com.example.gestorcartera;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        stage.setTitle("Gestor de Cartera");
        stage.setScene(scene);
        stage.getIcons().add(crearIcono());
        stage.show();
    }

    private WritableImage crearIcono() {
        Canvas canvas = new Canvas(64, 64);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e2e"));
        gc.fillRoundRect(0, 0, 64, 64, 16, 16);
        gc.setFill(Color.web("#7c6af7"));
        gc.fillRoundRect(8, 32, 12, 24, 4, 4);
        gc.setFill(Color.web("#4ade80"));
        gc.fillRoundRect(26, 20, 12, 36, 4, 4);
        gc.setFill(Color.web("#fbbf24"));
        gc.fillRoundRect(44, 10, 12, 46, 4, 4);
        gc.setStroke(Color.web("#aaaacc"));
        gc.setLineWidth(1.5);
        gc.strokeLine(8, 8, 56, 8);
        WritableImage img = new WritableImage(64, 64);
        canvas.snapshot(null, img);
        return img;
    }
}