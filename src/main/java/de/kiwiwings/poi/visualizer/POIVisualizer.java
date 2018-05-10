/* ====================================================================
   Copyright 2017 Andreas Beeker (kiwiwings@apache.org)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;

public class POIVisualizer extends Application {

    private Controller controller;
    private Parent root;

    @Override
    public void init() throws Exception {
        final String pckName = POIVisualizer.class.getPackage().getName();
        URL location = getClass().getResource("poivisualizer.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        root = loader.load();
        controller = loader.getController();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller.setStage(primaryStage);

        primaryStage.setTitle("POI Visualizer - <no file>");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();


        List<String> args = getParameters().getUnnamed();
        if (!args.isEmpty()) {
            File file = new File(args.get(0));
            if (file.exists()) {
                controller.openFile(file);
            }
        }
    }

    @Override
    public void stop() {
        // stop timer, otherwise awt event queue is always triggered again
        controller.getCodeArea().getCaret().setBlinkRate(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
