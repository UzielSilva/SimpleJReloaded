package com.simplej.vc.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.scene.image.Image;
import org.controlsfx.dialog.Dialogs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * Created by Uziel on 06/04/2015.
 */

class ProjectCell extends ListCell<String> {
    HBox hbox = new HBox();
    VBox vbox = new VBox();
    Label pathLabel = new Label("(empty)");
    Label titleLabel = new Label("(empty)");
    Pane pane = new Pane();
    Button button = new Button();
    String lastItem;

    public ProjectCell() {
        super();
        String path = Main.config.getProperty("fxml.images.path");
        Image image = new Image(Main.class.getResourceAsStream(String.format(path + "/%s", "recycle_bin_full.png")));
        button.setGraphic(new ImageView(image));
        button.setMinSize(22, 22);
        button.setMaxSize(22, 22);
        button.setPrefSize(22, 22);
        pathLabel.setMaxWidth(300);
        pathLabel.setTextOverrun(OverrunStyle.CENTER_WORD_ELLIPSIS);

        vbox.getChildren().addAll(titleLabel, pathLabel);
        hbox.getChildren().addAll(vbox, pane, button);

        titleLabel.getStyleClass().add("title-label");
        pathLabel.getStyleClass().add("path-label");

        HBox.setHgrow(pane, Priority.ALWAYS);

        button.setOnAction(event -> System.out.println(lastItem + " : " + event));
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            lastItem = null;
            setGraphic(null);
        } else {
            lastItem = item;
            pathLabel.setText(item!=null ? item : "<null>");
            titleLabel.setText(item!=null ? item.split("/")[item.split("/").length - 1] : "<null>");
            setGraphic(hbox);
        }
    }
}

public class SelectorFX implements Initializable {

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
            stage.close();
        }
    }
    @FXML
    private void cancelAction(){
        stage.close();
    }
    @FXML
    private void goAction(){
        stage.close();
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
                .title("SimpleJ DevKit")
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

//        ObservableList<String> list = FXCollections.observableArrayList(
//                "C:/Users/Uziel/simplej/projects/ChooxRescueasfasdfasdf",
//                "C:/Users/Uziel/simplej/projects/ChooxRescue",
//                "C:/Users/Uziel/simplej/projects/ChooxRescue",
//                "C:/Users/Uziel/simplej/projects/ChooxRescue",
//                "C:/Users/Uziel/simplej/projects/ChooxRescue",
//                "C:/Users/Uziel/simplej/projects/ChooxRescue",
//                "C:/Users/Uziel/simplej/projects/ChooxRescue"
//                );
//        projectList.setItems(list);

        // Create a MenuItem and place it in a ContextMenu
        MenuItem helloWorld = new MenuItem("Hello World!");
        ContextMenu contextMenu = new ContextMenu(helloWorld);

        // sets a cell factory on the ListView telling it to use the previously-created ContextMenu (uses default cell factory)
        projectList.setCellFactory(param -> new ProjectCell());

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
                    .title("SimpleJ DevKit")
                    .masthead("Error loading program.")
                    .message("Couldn't load resources.")
                    .showException(e);
            System.exit(2);
        }

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

