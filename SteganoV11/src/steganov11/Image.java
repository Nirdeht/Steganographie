package steganov11;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image {

    private RandomAccessFile file;
    private int degradation;
    private int largeur;
    private int hauteur;

    public static final int TAILLE_ENTETE = 2; //Entete du texte, qui suit l'entete de l'image image etant en bitmap

    public Image(String path, int deg) throws FileNotFoundException {
        this.file = new RandomAccessFile(path, "rw");
        this.degradation = deg;
    }

    public Image(String path) throws FileNotFoundException {
        this.file = new RandomAccessFile(path, "rw");
    }
    
    public int getLargeur()
    {
        return this.largeur;
    }
    
    public int getHauteur()
    {
        return this.hauteur;
    }
    
    public int getNbOctets()
    {
        return this.hauteur*this.largeur*3;
    }

    public void destroy() {
        try {
            this.file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDimension(){
        try {
            file.seek(0);
            file.skipBytes(18); //c'est constant
            String s = new String();
            for (int i = 0; i < 4; i++) //largeur sur 4 octets
            {
                s = Text.decToBin(file.read(), 8) + s;
            }
            this.largeur = Text.binToDec(s);

            s = new String();
            for (int i = 0; i < 4; i++) //largeur sur 4 octets
            {
                s = Text.decToBin(file.read(), 8) + s;
            }
            this.hauteur = Text.binToDec(s);
        } catch (IOException ex) {
            Logger.getLogger(Image.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void creerEntete() {
        String binDeg = Text.decToBin(this.degradation, 4);

        try {
            file.seek(0);
            int offset = this.getOffsetBMP(); // on se positionne apres l'entete Bitmap
            String[] groupes = Text.getParts(binDeg, 2); // on decoupe 2 bit par 2 bit
            for (int i = 0; i < groupes.length; i++) {
                String b = Text.decToBin(this.file.read(), 8);
                b = Text.replaceCharAt(b, b.length() - 2, groupes[i].charAt(0)); // on change les deux derniers bit pour 2 octets
                b = Text.replaceCharAt(b, b.length() - 1, groupes[i].charAt(1));

                this.file.seek(offset + i);
                this.file.write(Text.binToDec(b));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void coderImage(String texteACoder) {
        this.creerEntete();
        String texteBinaire = Text.stringToBin(texteACoder);
        texteBinaire += Text.END_OF_STRING; //on ajoute le caractère fin de chaine '\0'
        String[] groupes = Text.getParts(texteBinaire, this.degradation); //découpe la chaine en chaine de 2/4/8 bits 

        try {
            this.file.seek(0);
            int offset = this.getOffsetBMP();

            for (int i = 0; i < groupes.length; i++)//pour chaque groupe de 2/4/8 bits
            {
                String b = Text.decToBin(this.file.read(), 8);
                for (int j = 0; j < this.degradation; j++) {
                    b = Text.replaceCharAt(b, b.length() - (j + 1), groupes[i].charAt(this.degradation - j - 1));
                }

                this.file.seek(offset + Image.TAILLE_ENTETE + i);
                this.file.write(Text.binToDec(b));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String decoderImage() {
        try {
            setAtBeginningBMP();
            String deg = "";
            for (int i = 0; i < Image.TAILLE_ENTETE; i++) {

                String s = Text.decToBin(file.read(), 8);
                deg += s.substring(s.length() - 2);
            }
            this.degradation = Text.binToDec(deg);

            String msg = "";
            String curr_char = readByteBMP();
            while (!curr_char.equals(Text.binToString(Text.END_OF_STRING))) {
                msg += curr_char;
                System.out.println(curr_char);
                curr_char = readByteBMP();
            }

            return msg;
        } catch (Exception e) {
            return "";
        }
    }

    public String readByteBMP() {
        try {
            String s = "";
            for (int i = 0; i < 8 / this.degradation; i++)//on lit par groupe de deux bits. Pour former un octet il faut en lire 4 groupes
            {
                String value = Text.decToBin(this.file.read(), 8);
                System.out.println(value);
                for (int j = 0; j < this.degradation; j++) {
                    s = String.valueOf(value.charAt(value.length() - j - 1)) + s;
                }
            }

            return Text.binToString(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public int getOffsetBMP() {
        try {
            this.file.skipBytes(10);

            String s = "";

            for (int i = 0; i < 4; i++)//on lit l'info pour le début de l'image
            {
                s = Text.decToBin(this.file.read(), 8) + s;
            }

            return Text.binToDec(s);
        } catch (Exception e) {
            return -1;
        }
    }

    public void setAtBeginningBMP() {
        try {
            this.file.skipBytes(10); //on se déplace jusqu'à l'info sur l'adresse de début de l'image

            String s = "";

            for (int i = 0; i < 4; i++)//on lit l'info sur le début de l'image, codé sur 4 octets (en sens inverse)
            {
                s = Text.decToBin(this.file.read(), 8) + s;
            }

            int offset = Text.binToDec(s);
            this.file.seek(offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
