/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganov11;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javafx.scene.image.Image;

/**
 *
 * @author Thibault
 */
public class FXMLDocumentController implements Initializable {
    
    private Label label;
    private Button button;
    @FXML
    private TextArea texte;
    @FXML
    private Button coder;
    @FXML
    private Button decoder;
    @FXML
    private Button parcourir;
    @FXML
    private ImageView image;
    
    public static String cheminImage="";
    @FXML
    private Slider degradation;
    
    private void handleButtonAction(ActionEvent event) {
        button.setText("Test");
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        degradation.setShowTickMarks(true);
        degradation.setMin(1);
        degradation.setMax(3);
        coder.setDisable(true);
        decoder.setDisable(true);
    }    

    @FXML
    private void handleButtonCoder(ActionEvent event) {
        try {
            steganov11.Image imageU = new steganov11.Image(cheminImage);
            imageU.coderImage(texte.getText());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleButtonDecoder(ActionEvent event) {
        
        try {
            steganov11.Image imageU = new steganov11.Image(cheminImage);
            String s = imageU.decoderImage();
            texte.setText(s);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleButtonParcourir(ActionEvent event) {
        
        Window mainStage = ((Node)event.getTarget()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(
            new ExtensionFilter("Image Files", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null)
        {
            image.setImage(new Image(selectedFile.toURI().toString()));
            coder.setDisable(false);
            decoder.setDisable(false);
            cheminImage = selectedFile.getAbsolutePath();
        }
        
    }
    
}
