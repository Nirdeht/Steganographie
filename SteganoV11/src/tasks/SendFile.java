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

            ServerSocket server = new ServerSocket(2009); //on prépare le serveur
            updateMessage("Attente d'un client");
            Socket clientSocket = server.accept(); //on attend un client
            

            //on commence par envoyer le nom du fichier au client
            printWriterOut = new PrintWriter(clientSocket.getOutputStream());
            printWriterOut.println(img.getName());
            printWriterOut.close();
            updateMessage("Nom du fichier envoyé");

            //puis l'image
            fis = new FileInputStream(img);
            OutputStream fos = clientSocket.getOutputStream();
            while ((bw = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bw); //on envoie l'image
            }

            fos.close();
            fis.close();
            clientSocket.close(); //on ferme le socket client
            server.close(); //on ferme notre socket
            updateMessage("Image envoyé");
        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }
}
