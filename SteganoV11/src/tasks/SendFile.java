package tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.concurrent.Task;

public class SendFile extends Task<Void> {

    private final File img;

    public SendFile(File img) {
        this.img = img;
    }

    @Override
    protected Void call() throws Exception {
        try {
            byte[] buffer = new byte[1024];
            int bw = 0;
            InputStream fis;
            PrintWriter printWriterOut;
            OutputStream fos;

            ServerSocket server = new ServerSocket(2009); //on prépare le serveur
            updateMessage("Attente d'un client");
            Socket clientSocket = server.accept(); //on attend un client
            
            fos = clientSocket.getOutputStream();
            
            //on commence par envoyer le nom du fichier au client
            printWriterOut = new PrintWriter(fos);
            printWriterOut.println(img.getName());
            printWriterOut.flush();
            
            updateMessage("Nom du fichier envoyé");
            
            //puis l'image
            fis = new FileInputStream(img); //on ouvre le fichier image
            while ((bw = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bw); //on envoie l'image
            }
            updateMessage("Image envoyé");
            
            fos.flush();
            fos.close();
            fis.close();
            clientSocket.close(); //on ferme le socket client
            server.close(); //on ferme notre socket
        } catch (IOException e) {
            updateMessage(e.getMessage());
        }

        return null;
    }
}
