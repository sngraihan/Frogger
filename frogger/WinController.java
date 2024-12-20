/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package frogger;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * FXML Controller class
 *
 * @author raiha
 */
public class WinController implements Initializable {
  @FXML
    private ImageView reset;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label livesLabel;

    private Stage stage;
    private Scene scene;
    private Parent root;
    
    private MediaPlayer clk;
    private static final Media CLICK = new Media(FXMLDocumentController.class.getResource("/frogger/sfx/click.mp3").toExternalForm());

@Override
    public void initialize(URL url, ResourceBundle rb) {
        clk = new MediaPlayer(CLICK);
        clk.setVolume(0.7);
    }    

    public void setScore(int score, long time, int lives) {
        scoreLabel.setText("Score: " + score);
        timeLabel.setText(String.format("Time: %.2fs", (double)time));
        livesLabel.setText("Remaining Lives: " + lives);
    }

@FXML
    private void tekanSaya(MouseEvent event) {
        clk.play();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            stage.setScene(new Scene(root));
            stage.setTitle("Game Scene");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
