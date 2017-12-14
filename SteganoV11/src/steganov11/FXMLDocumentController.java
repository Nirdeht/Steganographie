/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganov11;

import java.io.BufferedReader;
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
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author Thibault
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private TextArea textToHide;
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
    @FXML
    private AnchorPane mainPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabCoderDecoder;
    @FXML
    private AnchorPane paneCoderDecoder;
    @FXML
    private Label tailleTexteMax;
    @FXML
    private Label tailleTexteActuel;
    @FXML
    private Tab tabEnvoyerRecevoir;
    @FXML
    private AnchorPane paneEnvoyerRecevoir;
    @FXML
    private SplitPane spRecevoirEnvoyer;
    @FXML
    private AnchorPane spEnvoyer;
    @FXML
    private Label labelEnvoyer;
    @FXML
    private Button buttonEnvoyer;
    @FXML
    private AnchorPane spRecevoir;
    @FXML
    private Label labelRecevoir;
    @FXML
    private TextField ipRecevoir;
    @FXML
    private TextField portRecevoir;
    @FXML
    private Button buttonRecevoir;
    @FXML
    private Button choixDossier;
    @FXML
    private ImageView visualisationImage;
    @FXML
    private Label destination;
    @FXML
    private Label messageTask;

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
        textToHide.setDisable(true);

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
            setTailleTexteActuel();
        });

        textToHide.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            coder.setDisable(!isCodable());
            setTailleTexteActuel();
        });
    }

    @FXML
    private void handleButtonCoder(ActionEvent event) {
        image.setDegradation((int) Math.pow(2, degradation.getValue()));
        image.coderImage(textToHide.getText());
        imageView.setImage(new Image(new File(cheminImage).toURI().toString()));
    }

    @FXML
    private void handleButtonDecoder(ActionEvent event) {
        String s = image.decoderImage();
        textToHide.setText(s);
    }

    private boolean isCodable() {
        int nbOctets = image.getNbOctets();
        int nbOctetsNecessaires = (textToHide.getText().length() + 1) * (int) (8 / Math.pow(2, degradation.getValue())) + steganov11.Image.TAILLE_ENTETE;

        return !textToHide.getText().equals("") && imageView.getImage() != null && nbOctetsNecessaires <= nbOctets;
    }

    private boolean isDecodable() {
        return imageView.getImage() != null;
    }

    private void setTailleTexteActuel() {
        tailleTexteActuel.setText(String.valueOf((textToHide.getText().length() + 1) * (int) (8 / Math.pow(2, degradation.getValue())) + steganov11.Image.TAILLE_ENTETE));
    }

    @FXML
    private void handleButtonParcourir(ActionEvent event) {
        File selectedFile = chooseImageFile();
        if (selectedFile != null) {
            imageView.setImage(new Image(selectedFile.toURI().toString()));
            cheminImage = selectedFile.getAbsolutePath();
            setImage(cheminImage);

            degradation.setDisable(false);
            textToHide.setDisable(false);
            coder.setDisable(!isCodable());
            decoder.setDisable(!isDecodable());
            tailleTexteMax.setText(String.valueOf(image.getNbOctets()));
        }
    }

    private File chooseImageFile() {
        Window mainStage = mainPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(
                new ExtensionFilter("Image Files", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(mainStage);

        return selectedFile;
    }

    /*TAB RECEVOIR/ENVOYER*/
    @FXML
    private void handleChoixDestination(ActionEvent event) {
        Window mainStage = ((Node) event.getTarget()).getScene().getWindow();
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Dossier d'enregistrement");

        File selectedFolder = dc.showDialog(mainStage);
        if (selectedFolder != null) {
            destination.setText(selectedFolder.getAbsolutePath());
        }
    }

    @FXML
    private void handleEnvoiImage(ActionEvent event) {
        File selectedFile;

        selectedFile = chooseImageFile(); //on commence par sélectionner l'image à envoyer

        if (selectedFile != null) {
            visualisationImage.setImage(new Image(selectedFile.toURI().toString()));
            tasks.SendFile sf = new tasks.SendFile(selectedFile);
            sf.messageProperty().addListener((obs, oldMsg, newMsg) -> {
                messageTask.setText(newMsg);
            });
        }
    }

    @FXML
    private void handleRecevoirFichier(ActionEvent event) {
        byte[] buffer = new byte[1024];
        int bw = 0;
        File img;
        BufferedReader in;
        String fileName;

        try {
            Socket client = new Socket(ipRecevoir.getText(), Integer.valueOf(portRecevoir.getText())); //on se connecte au serveur

            //on récupère le nom du fichier
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            fileName = in.readLine();

            in.close();

            img = new File(destination.getText() + "/" + fileName); //fichier de l'image

            //on récupère l'image
            FileOutputStream fos = new FileOutputStream(img);
            InputStream fis = client.getInputStream();

            while ((bw = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bw);
            }

            fos.close();
            fis.close();
            client.close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
