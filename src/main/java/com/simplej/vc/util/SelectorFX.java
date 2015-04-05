/**
 * Created by Uziel on 04/04/2015.
 */

package com.simplej.vc.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class SelectorFX implements Initializable {

    @FXML
    private ListView<String> projectList;
    @FXML
    private TextField newProject;
    private static Stage stage;

    public static void setStage(Stage theStage){
        stage = theStage;
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
