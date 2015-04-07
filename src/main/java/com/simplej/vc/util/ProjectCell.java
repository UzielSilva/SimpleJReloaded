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
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.*;

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

    public ProjectCell(ListView<String> parent,Stage selectorStage,Consumer actionActivate) {
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
                parent.getItems().remove(this.pathLabel.getText());
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
            titleLabel.setText(item!=null ? item.split("/")[item.split("/").length - 1] : "<null>");
            setGraphic(hbox);
        }
    }
}
