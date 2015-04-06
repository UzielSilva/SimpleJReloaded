/**
 * Created by Uziel on 04/04/2015.
 */

package com.simplej.vc.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class SelectorFX implements Initializable {

    private static Parent root;
    @FXML
    private ListView<String> projectList;
    @FXML
    private TextField newProject;
    private static Stage stage;
    @FXML
    private void cancelAction(){
        stage.close();
    }

    public static void setStage(Stage primaryStage) throws IOException {
        String selectorPath = Main.config.getProperty("fxml.selector.path");
        Parent root = FXMLLoader.load(Main.class.getResource(selectorPath));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("Selector");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        stage = primaryStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String[] s = {"C:/Users/Uziel/simplej/projects/Nivel_01",
                "C:/Users/Uziel/simplej/projects/Nivel_02",
                "C:/Users/Uziel/simplej/projects/Nivel_03"};
        ObservableList<String> projects = FXCollections.observableArrayList(s);
        projectList.setItems(projects);
    }
}
