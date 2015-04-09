package com.simplej.vc.util;

import com.simplej.vc.env.Environment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.IOUtils;
import org.controlsfx.dialog.*;

import javafx.scene.input.KeyEvent;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SelectorFX implements Initializable {

    private static double xOffset = 0;
    private static double yOffset = 0;

    @FXML
    private ListView<String> projectList;
    @FXML
    private Label noProjectsLabel;
    @FXML
    private Label contentNameLabel;
    @FXML
    private Label contentDescriptionLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label projectLabel;
    @FXML
    private Button newButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button goButton;

    private static Stage stage;

    private static String type;

    public static String result;

    public static void setStage(Stage stage){
        SelectorFX.stage = stage;
    }

    public static void setType(String type){
        SelectorFX.type = type;
    }

    @FXML
    private void newAction(){
        // TODO: Create new project dialog.
    }
    @FXML
    private void searchAction(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if(selectedDirectory != null && validateDirectory(selectedDirectory.getAbsolutePath())) {
            result = selectedDirectory.getAbsolutePath();
            goAction();
        }
    }
    @FXML
    private void cancelAction(){
        result = "";
        stage.close();
    }
    @FXML
    private void goAction(){
        String simpleJHome = System.getProperty("simpleJ.home");
        if (simpleJHome == null) {
            String home = System.getProperty("user.home");
            simpleJHome = home + File.separator + Main.config.getProperty("folder.name");
        }
        String simpleJTemporary = simpleJHome + File.separator + Main.config.getProperty("selector.temporary.name");

        try {
            File f = new File(simpleJHome);
            if(!f.exists()){
                f.mkdir();
            }
            f = new File(simpleJTemporary);
            if(!f.exists()){
                f.createNewFile();
            }
            String content = IOUtils.toString(new FileInputStream(f.getAbsolutePath()), "UTF-8");
            List<String> files = Arrays.asList(content.split(","));
            if(!files.contains(result)){
                if(content.compareTo("") == 0){
                    content = result;
                }else{
                    content += "," + result;
                }
                Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(f.getAbsolutePath()), "utf-8"));
                writer.write(content);
                writer.close();
            }
        } catch (IOException e) {
            Dialogs.create()
                    .owner(stage)
                    .title("SimpleJ")
                    .masthead("Error loading temporary files.")
                    .message("Couldn't load temporary projects.")
                    .showException(e);
        }
        stage.close();
    }

    @FXML
    private void listProjectPressed(KeyEvent keyEvent){
        if (keyEvent.getCode().equals(KeyCode.ENTER)){
            goAction();
        }
    }

    private boolean validateDirectory(String path){
        File f = new File(path);
        if(f.exists() && f.isDirectory()){
            File g = new File(path + "/main.sj");
            if(g.exists() && !g.isDirectory()){
                return true;
            }
        }
        Dialogs.create()
                .owner(stage)
                .title("SimpleJ")
                .masthead("Not a valid directory.")
                .message(
                        "Please confirm that directory exists and " +
                        "contains main.sj file."
                )
                .showInformation();
        return false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){

        readRecentProjects(projectList);

        typeLabel.setText(Main.language.getProperty(String.format("%s.selector.typeLabel", type)));
        projectLabel.setText(Main.language.getProperty(String.format("%s.selector.projectLabel", type)));
        searchButton.setText(Main.language.getProperty(String.format("%s.selector.searchButton", type)));

        nameLabel.setText(Main.language.getProperty("selector.nameLabel"));
        descriptionLabel.setText(Main.language.getProperty("selector.descriptionLabel"));
        newButton.setText(Main.language.getProperty("selector.newButton"));
        cancelButton.setText(Main.language.getProperty("selector.cancelButton"));
        goButton.setText(Main.language.getProperty("selector.goButton"));

        contentNameLabel.setText("");
        contentDescriptionLabel.setText("");

        noProjectsLabel.setText(Main.language.getProperty("selector.noProjectsLabel"));
        if(projectList.getItems().size() != 0){
            noProjectsLabel.setVisible(false);
        }

        newButton.setVisible(Boolean.parseBoolean(Main.config.getProperty(String.format("%s.selector.newButtonVisible", type))));
    }

    private void readRecentProjects(ListView<String> projectList) {

        String simpleJHome = System.getProperty("simpleJ.home");
        String[] files;
        if (simpleJHome == null) {
            String home = System.getProperty("user.home");
            simpleJHome = home + File.separator + Main.config.getProperty("folder.name");
        }
        String simpleJTemporary = simpleJHome + File.separator + Main.config.getProperty("selector.temporary.name");

        try {
            File f = new File(simpleJHome);
            if(!f.exists()){
                f.mkdir();
            }
            f = new File(simpleJTemporary);
            if(!f.exists()){
                f.createNewFile();
            }
            String path = IOUtils.toString(new FileInputStream(f.getAbsolutePath()), "UTF-8");
            files = (path == null ? "" : path).split(",");
            if(files.length >= 1 && files[0].compareTo("") != 0){
                ObservableList<String> list = FXCollections.observableArrayList(files);
                projectList.setItems(list);
            }
        } catch (IOException e) {
            Dialogs.create()
                    .owner(stage)
                    .title("SimpleJ")
                    .masthead("Error loading program.")
                    .message("Couldn't load resources.")
                    .showException(e);
            System.exit(2);
        }

        projectList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            result = observable.getValue();
            contentNameLabel.setText(result != null ? Environment.toProjectName(result):null);
        });
        projectList.setCellFactory(param -> new ProjectCell(noProjectsLabel,projectList, stage,
                        event -> goAction())
        );

    }



    public void show(){
        stage.showAndWait();
    }
    public static String select(SimpleJType type){

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Selector");

        SelectorFX.setStage(stage);
        String typeText= SimpleJTypeFactory.get(type);
        SelectorFX.setType(typeText);

        String selectorPath = Main.config.getProperty("fxml.selector.path");
        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource(selectorPath));
        } catch (IOException e) {
            Dialogs.create()
                    .owner(stage)
                    .title("SimpleJ")
                    .masthead("Error loading program.")
                    .message("Couldn't load resources.")
                    .showException(e);
            System.exit(2);
        }

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.showAndWait();
        return SelectorFX.result;
    }
}

class SimpleJTypeFactory{
    public static String get(SimpleJType type){
        switch(type){
            case DEVKIT:
                return "devkit";
            case TILESEDITOR:
                return "tiles.editor";
            case SPRITESEDITOR:
                return "sprites.editor";
            case CONSOLE:
                return "console";
            default:
                return null;
        }
    }
}

