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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author raiha
 */
public class MenuController implements Initializable {

    @FXML
    private ImageView reset;
    
    private MediaPlayer clk;
    private static final Media CLICK = new Media(FXMLDocumentController.class.getResource("/frogger/sfx/click.mp3").toExternalForm());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        clk = new MediaPlayer(CLICK);
        clk.setVolume(0.7);
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
