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
import javafx.beans.value.ObservableValue;
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

    @FXML
    private TextArea texte;
    @FXML
    private Button coder;
    @FXML
    private Button decoder;
    @FXML
    private Button parcourir;
    @FXML
    private ImageView imageView;
    @FXML
    private Slider degradation;

    private steganov11.Image image;
    private String cheminImage;

    public void setImage(String path) {
        try {
            image = new steganov11.Image(path);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coder.setDisable(true);
        decoder.setDisable(true);
        degradation.setDisable(true);
        texte.setDisable(true);
        
        degradation.setMin(0);
        degradation.setMax(3);
        degradation.setValue(0);
        degradation.setShowTickLabels(true);

        degradation.valueProperty().addListener((ObservableValue<? extends Number> obs, Number oldValue, Number newValue) -> {
            coder.setDisable(!isCodable());
            if (newValue.doubleValue() - Math.floor(newValue.doubleValue()) < 0.5) //on est avant la moitié
            {
                degradation.setValue(Math.floor(newValue.doubleValue()));
            } else {
                degradation.setValue(Math.ceil(newValue.doubleValue()));
            }
        });

        texte.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            coder.setDisable(!isCodable());

        });
    }

    @FXML
    private void handleButtonCoder(ActionEvent event) {
        image.setDegradation((int)Math.pow(2,degradation.getValue()));
        image.coderImage(texte.getText());
        imageView.setImage(new Image(new File(cheminImage).toURI().toString()));
    }

    @FXML
    private void handleButtonDecoder(ActionEvent event) {
        String s = image.decoderImage();
        texte.setText(s);
    }

    private boolean isCodable() {
        int nbOctets = image.getNbOctets();
        int nbOctetsNecessaires = (texte.getText().length() + 1) * (int) (8 / Math.pow(2, degradation.getValue())) + steganov11.Image.TAILLE_ENTETE;

        return !texte.getText().equals("") && imageView.getImage() != null && nbOctetsNecessaires <= nbOctets;
    }

    private boolean isDecodable() {
        return imageView.getImage() != null;
    }

    @FXML
    private void handleButtonParcourir(ActionEvent event) {

        Window mainStage = ((Node) event.getTarget()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(
                new ExtensionFilter("Image Files", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            imageView.setImage(new Image(selectedFile.toURI().toString()));
            cheminImage = selectedFile.getAbsolutePath();
            setImage(cheminImage);

            degradation.setDisable(false);
            texte.setDisable(false);
            coder.setDisable(!isCodable());
            decoder.setDisable(!isDecodable());
            System.out.println(image.getNbOctets());
        }
    }

}
