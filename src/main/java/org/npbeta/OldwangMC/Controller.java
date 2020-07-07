package org.npbeta.OldwangMC;

import javafx.application.Platform;
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

    @FXML private Label version;
    @FXML private Label players;
    @FXML private Label ping;
    @FXML private Label Status;
    @FXML private AnchorPane start;
    @FXML private Label LocalVer;
    @FXML private Label LatestVer;
    @FXML private Pane StatusPane;
    @FXML private ProgressBar ProgressBar;
    @FXML private Button Install;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.Install.setDisable(Updater.openExistingRepo());
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
//        this.start.getStyleClass().add(JMetroStyleClass.BACKGROUND);
//        this.start.setStyle("-fx-background-color: rgb(255, 255, 255);");
        this.start.setBackground(new Background(new BackgroundImage(new Image("img/bg.png"),
                null, null, null, new BackgroundSize(this.start.getWidth(),
                this.start.getHeight(), false, false, false, false))));
    }

    public void setVersion(String version) {
        if (version == null) {
            this.version.setText("\uD83C\uDFAE  ----");
        } else {
            String rageX = "[^(0-9).]";
            this.version.setText("\uD83C\uDFAE  " + version.replaceAll(rageX, ""));
        }
    }

    public void setPing(int ping) {
        if (ping != -1) {
            this.ping.setText("\uD83D\uDCE1  " + ping + "ms");
        } else {
                this.ping.setText("\uD83D\uDCE1  --ms");
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
            this.Status.setTextFill(Color.web("#FF0000"));
            this.Status.setText("❌");
        } else {
            this.Status.setTextFill(Color.web("#00FF00"));
                this.Status.setText("✔");
        }
    }

    public void setLatestVer(String LatestVer) {
        this.LatestVer.setText(LatestVer);
    }

    public void setLocalVer(String LocalVer) {
        this.LocalVer.setText(LocalVer);
    }

    public void setStatusPane() {
        this.StatusPane.setStyle("-fx-background-color: rgb(255, 255, 255); -fx-background-radius: 8;");
    }

    public void setProgressBar(double value) {
        this.ProgressBar.setProgress(value);
    }

    public void onInstallClick() {
        ProgressBar.setVisible(true);
        Install.setDisable(true);
        Updater updater = new Updater();
        updater.setRepositoryUrl("https://gitee.com/npbeta/OldwangMC.git");
//        updater.setRepositoryUrl("https://gitee.com/willbeahero/IOTGate.git");
        Thread thread = new Thread(updater.Clone);
        thread.start();
        updater.Clone.progressProperty().addListener((observable, oldValue, newValue) ->
                setProgressBar(updater.Clone.getProgress()));
    }
}
