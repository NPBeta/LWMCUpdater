package org.npbeta.OldwangMC;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML private Pane bg;
    @FXML private AnchorPane load;
    @FXML private Label Loading;
    @FXML private AnchorPane start;
    @FXML private StackPane StatusPane;
    @FXML private Label version;
    @FXML private Label players;
    @FXML private Label ping;
    @FXML private Label Status;
    @FXML private Button Go;
    @FXML private Button Install;
    @FXML private Button Check;
    @FXML private ProgressBar progressBar;

    private boolean isUpdate = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bg.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        checkLocalRepo();
        StatusPane.setStyle("-fx-background-color: rgb(255, 255, 255); -fx-background-radius: 8; -fx-font-family: Microsoft YaHei UI;");
        Install.setStyle("-fx-background-color: rgb(76,159,220); -fx-background-radius: 8; -fx-font: 20 'Microsoft YaHei UI'; -fx-text-fill: white");
        Check.setStyle("-fx-background-color: rgb(255,255,255); -fx-background-radius: 8; -fx-font: 20 'Microsoft YaHei UI'");
        Go.setStyle("-fx-background-color: rgb(96,169,23); -fx-background-radius: 8; -fx-font: 32 'Microsoft YaHei UI'; -fx-text-fill: white");
    }

    public void RefreshServerInfo() {
        int onlinePlayers;
        int ping1;
        String Version = null;
        try {
            MotD Status = new MotD();
            InetSocketAddress server = new InetSocketAddress("zz-bgp.502.network", 3665);
            Status.setAddress(server);
            MotD.StatusResponse Response = Status.fetchData();
            Version = Response.getVersion().getName();
            ping1 = Response.getTime();
            onlinePlayers = Response.getPlayers().getOnline();
        } catch (Exception e) {
            ping1 = -1;
            onlinePlayers = -1;
        }
        String finalVersion = Version;
        int finalPing = ping1;
        int finalOnlinePlayers = onlinePlayers;
        Platform.runLater(() -> setServerInfo(finalVersion, finalPing, finalOnlinePlayers));
    }

    public void setJMetroStyle() {
        start.setBackground(new Background(new BackgroundImage(new Image("img/bg.png"),
                null, null, null, new BackgroundSize(bg.getWidth(),
                bg.getHeight(), false, false, false, false))));
    }

    private void setServerInfo(String version, int ping, int players) {
        if (version == null) {
            this.version.setText("\uD83C\uDFAE  ------");
            this.ping.setText("\uD83D\uDCE1   --ms");
            this.players.setText("\uD83D\uDC65  --");
            Status.setTextFill(Color.web("#FF0000"));
            Status.setText("❌");
        } else {
            this.version.setText("\uD83C\uDFAE  " + version.replaceAll("[^(0-9).]", ""));
            this.ping.setText("\uD83D\uDCE1   " + ping + "ms");
            this.players.setText("\uD83D\uDC65  " + players);
            Status.setTextFill(Color.web("#00FF00"));
            Status.setText("✔");
        }
    }

    private void setProgressBar(double value) {
        progressBar.setProgress(value);
    }

    public void onGoClick() {
        if (isUpdate){
            Updater updater = new Updater();
            Go.setDisable(true);
            Check.setDisable(true);
            progressBar.setVisible(true);
            updater.PullRepo.setOnSucceeded(event -> {
                if (updater.PullRepo.getValue()) {
                    Go.setText("开始游戏");
                    Check.setText("已是最新");
                    Go.setDisable(false);
                    isUpdate = false;
                } else {
                    Go.setText("更新失败");
                }
                Check.setDisable(false);
                progressBar.setVisible(false);
            });
            Thread thread = new Thread(updater.PullRepo);
            thread.start();
            updater.PullRepo.progressProperty().addListener((observable, oldValue, newValue) ->
                    setProgressBar(updater.PullRepo.getProgress()));
        } else {
            try {
                Runtime.getRuntime().exec("LwMC/HMCL.exe", new String[]{""}, new File("LwMC/"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    public void onInstallClick() {
        Updater updater = new Updater();
        progressBar.setVisible(true);
        Install.setDisable(true);
        updater.CloneRepo.setOnSucceeded(event -> {
            Go.setDisable(false);
            progressBar.setVisible(false);
        });
        Thread thread = new Thread(updater.CloneRepo);
        thread.start();
        updater.CloneRepo.progressProperty().addListener((observable, oldValue, newValue) ->
                setProgressBar(updater.CloneRepo.getProgress()));
    }

    public void onCheckClick() {
        Check.setDisable(true);
        checkUpdate(false, true);
    }

    public void onCheckOver() {
        Check.setText("检查更新");
    }

    public void checkLocalRepo() {
        Updater updater = new Updater();
        updater.OpenExistingRepo.setOnSucceeded(event -> {
            System.out.println("Local Check Completed.");
            boolean isExist = updater.OpenExistingRepo.getValue();
            Install.setDisable(isExist);
            Go.setDisable(!isExist);
            Check.setDisable(!isExist);
            checkUpdate(true, isExist);
        });
        Loading.setText("正在检查本地游戏文件");
        Thread thread = new Thread(updater.OpenExistingRepo);
        thread.start();
    }

    public void checkUpdate(boolean onStartUp, boolean isLocalExist) {
        if (isLocalExist) {
            Updater updater = new Updater();
            updater.CheckUpdate.setOnSucceeded((WorkerStateEvent event) -> {
                System.out.println("Cloud Check Completed.");
                Object result = updater.CheckUpdate.getValue();
                if (onStartUp) {
                    start.setVisible(true);
                    load.setVisible(false);
                } else {
                    Check.setDisable(false);
                }
                if (result != null) {
                    if (result.equals(true)) {
                        isUpdate = false;
                        Go.setDisable(false);
                        Check.setText("已是最新");
                        Go.setText("开始游戏");
                    } else {
                        isUpdate = true;
                        Go.setDisable(false);
                        Check.setText("发现更新");
                        Go.setText("立即更新");
                    }
                } else {
                    Go.setDisable(true);
                    Check.setText("网络错误");
                }
            });
            if (onStartUp) Loading.setText("正在检查更新");
            else Check.setText("正在检查");
            Thread thread = new Thread(updater.CheckUpdate);
            thread.start();
        } else {
            Check.setText("检查更新");
            Go.setText("开始游戏");
            start.setVisible(true);
            load.setVisible(false);
        }
    }
}