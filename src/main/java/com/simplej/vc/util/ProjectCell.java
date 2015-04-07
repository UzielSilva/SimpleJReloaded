package com.simplej.vc.util;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Uziel on 06/04/2015.
 */

class ProjectCell extends ListCell<String> {
    ListView<String> parent;
    Stage selectorStage;
    HBox hbox = new HBox();
    VBox vbox = new VBox();
    Label pathLabel = new Label("(empty)");
    Label titleLabel = new Label("(empty)");
    Pane pane = new Pane();
    Button button = new Button();
    String lastItem;
    public String projectName;

    public ProjectCell(Label noProjects, ListView<String> parent,Stage selectorStage,Consumer actionActivate) {
        super();
        this.parent = parent;
        this.selectorStage = selectorStage;
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

        button.setOnAction(event -> {
            Action action = Dialogs.create()
                    .owner(selectorStage)
                    .title("SimpleJ")
                    .masthead("Are you sure?")
                    .message(
                            "Are you sure of remove from the " +
                                    "recent list?"
                    )
                    .actions(org.controlsfx.dialog.Dialog.ACTION_YES, org.controlsfx.dialog.Dialog.ACTION_NO)
                    .showConfirm();
            if (action.equals(org.controlsfx.dialog.Dialog.ACTION_YES)) {
                String simpleJTemporary = System.getProperty("simpleJ.home");
                if (simpleJTemporary == null) {
                    String home = System.getProperty("user.home");
                    simpleJTemporary = home + File.separator + Main.config.getProperty("selector.temporary.path");
                }
                try {
                    File f = new File(simpleJTemporary);
                    if(!f.exists()){
                        f.createNewFile();
                    }
                    String content = IOUtils.toString(new FileInputStream(f.getAbsolutePath()), "UTF-8");
                    List<String> files = Arrays.asList(content.split(","));
                    if(files.contains(pathLabel.getText())){
                        String listString = "";

                        for (String s : files)
                        {
                            if(s.compareTo(pathLabel.getText()) != 0)
                                listString += s + ",";
                        }
                        if(listString.compareTo("") != 0)
                            listString = listString.substring(0,listString.length() - 1);
                        Writer writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(f.getAbsolutePath()), "utf-8"));
                        writer.write(listString);
                        writer.close();
                    }
                } catch (IOException e) {
                    Dialogs.create()
                            .owner(selectorStage)
                            .title("SimpleJ")
                            .masthead("Error loading temporary files.")
                            .message("Couldn't load temporary projects.")
                            .showException(e);
                }
                parent.getItems().remove(this.pathLabel.getText());
                if(parent.getItems().size() == 0){
                    noProjects.setVisible(true);
                }
            }
        });

        this.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    actionActivate.accept(mouseEvent);
                }
            }
        });

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
            titleLabel.setText(item!=null ? item.split("\\\\|/")[item.split("\\\\|/").length - 1] : "<null>");
            setGraphic(hbox);
        }
    }
}
