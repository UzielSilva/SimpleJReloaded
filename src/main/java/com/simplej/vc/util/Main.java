/**
 * Created by Uziel on 04/04/2015.
 */
package com.simplej.vc.util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class Main extends Application {
    public static final Properties config;

    static {
        config = new Properties();
        try {
            InputStream stream = Main.class.getResourceAsStream("/com/simplej/core.properties");
            try {
                config.load(stream);
            }
            finally {
                stream.close();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @Override

    public void start(Stage primaryStage) throws Exception{

        DefaultArtifactVersion minVersion = new DefaultArtifactVersion("1.8");
        DefaultArtifactVersion version = new DefaultArtifactVersion(System.getProperty("java.version"));

        if (version.compareTo(minVersion) < 0) {
            Dialogs.create()
                    .owner(primaryStage)
                    .title("SimpleJ DevKit")
                    .masthead("Error in Java version.")
                    .message(String.format("Should be %s or greater", minVersion))
                    .showError();
            System.exit(2);
        }
        List<String> args = getParameters().getRaw();

        boolean countMain;
        boolean countVBI;
        boolean countSFI;
        boolean profileVBI;

        for (String arg : args) {
            if (arg.equals("-main"))
                countMain = true;
            else if (arg.equals("-vbi"))
                countVBI = true;
            else if (arg.equals("-sfi"))
                countSFI = true;
            else if (arg.equals("-vbiProf"))
                profileVBI = true;
            else {
                System.err.println("Unknown option: " + arg);
                System.exit(2);
            }
        }
        Stage select = new Stage();
        select.initModality(Modality.APPLICATION_MODAL);
        SelectorFX.setStage(select);
        select.showAndWait();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
