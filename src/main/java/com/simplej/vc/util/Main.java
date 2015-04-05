package com.simplej.vc.util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Uziel on 04/04/2015.
 */
public class Main extends Application {
    public Properties config;

    public void init(){

        config = new Properties();
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("com/simplej/core.properties");
            try {
                config.load(stream);
            }
            finally {
                stream.close();
            }
        }
        catch (IOException ex) {
            System.out.print("...");
            ex.printStackTrace();
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception{
        init();
        String selectorPath = config.getProperty("fxml.selector.path");
        Parent root = FXMLLoader.load(getClass().getResource(selectorPath));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("Selector");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        SelectorFX.setStage(primaryStage);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
