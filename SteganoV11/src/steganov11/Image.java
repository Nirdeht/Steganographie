package steganov11;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image {

    private RandomAccessFile file;
    private int degradation;
    private int largeur;
    private int hauteur;
    private int adresseDebut;

    public Image(String path, int deg) throws FileNotFoundException {
        this.file = new RandomAccessFile(path, "rw");
        this.degradation = deg;
        this.setDimension();
    }

    public Image(String path) throws FileNotFoundException {
        this(path, 1);
    }

    public int getLargeur() {
        return this.largeur;
    }

    public int getHauteur() {
        return this.hauteur;
    }

    public int getNbOctets() {
        return this.hauteur * this.largeur * 3;
    }

    public void destroy() {
        try {
            this.file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDimension() {
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
            for (int i = 0; i < 4; i++) //hauteur sur 4 octets
            {
                s = Text.decToBin(file.read(), 8) + s;
            }
            this.hauteur = Text.binToDec(s);
        } catch (IOException ex) {
            Logger.getLogger(Image.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setAdresseDebut(int adr) {
        this.adresseDebut = adr;
    }

    public void setDegradation(int value) {
        this.degradation = value;
    }

    public int getDegradation() {
        return this.degradation;
    }

    public void creerEntete() {
        String binDeg = Text.decToBin(this.degradation, 4);
        try {
            int offset = this.getOffsetBMP();
            this.setAtBeginningBMP();
            String[] groupes = Text.getParts(binDeg, 2); // on decoupe 2 bit par 2 bit
            for (int i = 0; i < groupes.length; i++) {
                String b = Text.decToBin(this.file.read(), 8);
                b = Text.replaceCharAt(b, b.length() - 2, groupes[i].charAt(0)); // on change les deux derniers bit pour 2 octets
                b = Text.replaceCharAt(b, b.length() - 1, groupes[i].charAt(1));
                
                this.file.seek(offset + i);
                this.file.write(Text.binToDec(b));
            }//on a écrit la dégradation

            int nbOctets = this.getLengthEntete() - 2;
            String adresseDebutBin = Text.decToBin(this.adresseDebut, nbOctets*2);
 
            groupes = Text.getParts(adresseDebutBin, 2);

            for (int i = 0; i < groupes.length; i++) {
                String b = Text.decToBin(this.file.read(), 8);
                b = Text.replaceCharAt(b, b.length() - 2, groupes[i].charAt(0)); // on change les deux derniers bit pour 2 octets
                b = Text.replaceCharAt(b, b.length() - 1, groupes[i].charAt(1));

                this.file.seek(offset + i  + 2);
                this.file.write(Text.binToDec(b));
            }//on a écrit l'adresse de début

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void coderImage(String texteACoder) {

        try {
            int offset = this.getOffsetBMP();

            this.creerEntete();
            this.file.skipBytes(this.adresseDebut);
            String texteBinaire = Text.stringToBin(texteACoder);
            texteBinaire += Text.END_OF_STRING; //on ajoute le caractère fin de chaine '\0'
            String[] groupes = Text.getParts(texteBinaire, this.degradation);

            for (int i = 0; i < groupes.length; i++)//pour chaque groupe de 2/4/8 bits
            {
                String b = Text.decToBin(this.file.read(), 8);
                for (int j = 0; j < this.degradation; j++) {
                    b = Text.replaceCharAt(b, b.length() - (j + 1), groupes[i].charAt(this.degradation - j - 1));
                }

                this.file.seek(offset + this.getLengthEntete() + this.adresseDebut + i);
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
            for (int i = 0; i < 2; i++) {
                String s = Text.decToBin(file.read(), 8);
                deg += s.substring(s.length() - 2);
            }
            this.degradation = Text.binToDec(deg);
            
            String adresseDebut = "";
            for(int i=0; i<this.getLengthEntete()-2; i++)
            {
                String s = Text.decToBin(file.read(), 8);
                adresseDebut += s.substring(s.length() - 2);
            }
            
            this.adresseDebut = Text.binToDec(adresseDebut);
            this.file.skipBytes(this.adresseDebut);
            
            String msg = "";
            String curr_char = readByteBMP();
            while (!curr_char.equals(Text.binToString(Text.END_OF_STRING))) {
                msg += curr_char;
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
                for (int j = this.degradation - 1; j >= 0; j--) {
                    s += String.valueOf(value.charAt(value.length() - j - 1));
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
            file.seek(0);
            this.file.skipBytes(10);

            String s = "";

            for (int i = 0; i < 4; i++)//on lit l'info pour le début de l'image
            {
                s = Text.decToBin(this.file.read(), 8) + s;
            }

            return Text.binToDec(s);
        } catch (IOException e) {
            return -1;
        }
    }

    public void setAtBeginningBMP() {
        try {
            this.file.seek(this.getOffsetBMP());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLengthEntete() //retourne la taille en octets de l'entête
    {
        int lengthEntete = 2; //taux de dégradation
        int nbPixels = this.getHauteur() * this.getLargeur() * 3;
        int nbBits = 0;

        while (Math.pow(2, nbBits) < nbPixels) {
            nbBits++;
        } //on cherche le nombre de bits nécessaires pour coder nbPixels

        lengthEntete += nbBits / 2 + nbBits % 2;

        return lengthEntete;
    }

    public int genRandomAdress(int tailleTexte) {
        int nbOctets = this.getNbOctets() - this.getLengthEntete();
        int nbOctetsNecessairesTexte = (tailleTexte + 1) * (int) (8 / Math.pow(2, this.degradation));
        int adresseMax = nbOctets - nbOctetsNecessairesTexte;

        Random adresse = new Random();
        return adresse.nextInt(adresseMax);
    }
}
