package org.npbeta.OldwangMC;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
        primaryStage.setTitle("老王 MC 6.0");
        primaryStage.setScene(scene);
        ctrl.setJMetroStyle();
        ScheduledService<Object> RSI = new ScheduledService<Object>() {
            @Override
            protected Task<Object> createTask() {
                return new Task<Object>() {
                    @Override
                    protected Object call() {
                        ctrl.RefreshServerInfo();
                        return null;
                    }
                };
            }
        };
        RSI.setPeriod(Duration.seconds(5)); //Keep Refreshing Server Status
        RSI.start();
        primaryStage.show();
    }
}
