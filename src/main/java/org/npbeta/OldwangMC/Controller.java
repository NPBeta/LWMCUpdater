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
    @FXML private Button Install;
    @FXML private Button Check;
    @FXML private ProgressBar ProgressBar;

    private static final Updater updater = new Updater();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bg.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        Loading.setText("正在检查本地游戏文件");
        Install.setDisable(checkLocalRepo());
        Loading.setText("正在检查更新");
        StatusPane.setStyle("-fx-background-color: rgb(255, 255, 255); -fx-background-radius: 8;");
        Check.setStyle("-fx-background-color: rgb(255, 255, 255); -fx-background-radius: 8;");
        Install.setStyle("-fx-background-color: rgb(255, 255, 255); -fx-background-radius: 8;");
        start.setVisible(true);
        load.setVisible(false);
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
        Platform.runLater(() -> {
            setVersion(finalVersion);
            setPing(finalPing);
            setPlayers(finalOnlinePlayers);
            setStatus(finalPing);
        });
    }

    public void setJMetroStyle() {
        start.setBackground(new Background(new BackgroundImage(new Image("img/bg.png"),
                null, null, null, new BackgroundSize(bg.getWidth(),
                bg.getHeight(), false, false, false, false))));
    }

    public void setVersion(String version) {
        if (version == null) {
            this.version.setText("\uD83C\uDFAE  ------");
        } else {
            String rageX = "[^(0-9).]";
            this.version.setText("\uD83C\uDFAE  " + version.replaceAll(rageX, ""));
        }
    }

    public void setPing(int ping) {
        if (ping != -1) {
            this.ping.setText("\uD83D\uDCE1   " + ping + "ms");
        } else {
            this.ping.setText("\uD83D\uDCE1   --ms");
        }
    }

    public void setPlayers(int players) {
        if (players != -1) {
            this.players.setText("\uD83D\uDC65  " + players);
        } else {
            this.players.setText("\uD83D\uDC65  --");
        }
    }

    public void setStatus(int ping) {
        if (ping == -1) {
            Status.setTextFill(Color.web("#FF0000"));
            Status.setText("❌");
        } else {
            Status.setTextFill(Color.web("#00FF00"));
                Status.setText("✔");
        }
    }

    public void setProgressBar(double value) {
        ProgressBar.setProgress(value);
    }

    public void onInstallClick() {
        ProgressBar.setVisible(true);
        Install.setDisable(true);
        Thread thread = new Thread(updater.CloneRepo);
        thread.start();
        updater.CloneRepo.progressProperty().addListener((observable, oldValue, newValue) ->
                setProgressBar(updater.CloneRepo.getProgress()));
        ProgressBar.setVisible(false);
    }

    public void onCheckClick() {
        ProgressBar.isIndeterminate();
        ProgressBar.setVisible(true);
        Check.setDisable(true);
        updater.CheckUpdate.setOnSucceeded((WorkerStateEvent event) -> {
            System.out.println("OnSucceeded.");
            Check.setDisable(false);
            ProgressBar.setVisible(false);
        });
        Thread thread = new Thread(updater.CheckUpdate);
        thread.start();
    }

    public boolean checkLocalRepo() {
        final boolean[] isExist = new boolean[1];
        updater.OpenExistingRepo.setOnSucceeded(event -> {
            System.out.println("OnSucceeded.");
            isExist[0] = updater.OpenExistingRepo.getValue();
        });
        Thread thread = new Thread(updater.CheckUpdate);
        thread.start();
        return isExist[0];
    }
}
