package org.npbeta.OldwangMC;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Parent root = loader.load();
        Controller ctrl = loader.getController();
        Scene scene = new Scene(root, 640, 360);
        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("老王 MC 2.0");
        primaryStage.setScene(scene);
        ctrl.setJMetroStyle();
        primaryStage.show();
        ScheduledService<Object> RSI = new ScheduledService<Object>() {
            @Override
            protected Task<Object> createTask() {
                return new Task<Object>() {
                    @Override
                    protected Object call() {
                        ctrl.RefreshServerInfo();   //Refresh Server Status
                        return null;
                    }
                };
            }
        };
        RSI.setPeriod(Duration.seconds(5));
        RSI.start();
        ctrl.setStatusPane();
    }
}
