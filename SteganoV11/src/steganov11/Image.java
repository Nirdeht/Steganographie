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
            String adresseDebutBin = Text.decToBin(this.adresseDebut, nbOctets * 2);

            groupes = Text.getParts(adresseDebutBin, 2);

            for (int i = 0; i < groupes.length; i++) {
                String b = Text.decToBin(this.file.read(), 8);
                b = Text.replaceCharAt(b, b.length() - 2, groupes[i].charAt(0)); // on change les deux derniers bit pour 2 octets
                b = Text.replaceCharAt(b, b.length() - 1, groupes[i].charAt(1));

                this.file.seek(offset + i + 2);
                this.file.write(Text.binToDec(b));
            }//on a écrit l'adresse de début

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void coderImage(String texteACoder) {

        try {
            int offset = this.getOffsetBMP();

            this.creerEntete(); //on créé l'entete du texte
            this.file.skipBytes(this.adresseDebut); //on se déplace à la position du début du texte
            String texteBinaire = Text.stringToBin(texteACoder); //on prépare le texte à écrire
            texteBinaire += Text.END_OF_STRING; //on ajoute le caractère fin de chaine '\0'
            String[] groupes = Text.getParts(texteBinaire, this.degradation); //on découpe en groupe de {this.degradation} (puissance de 2) bits

            for (int i = 0; i < groupes.length; i++)//pour chaque groupe de 2/4/8 bits
            {
                String b = Text.decToBin(this.file.read(), 8); //on récupère l'octet suivant
                for (int j = 0; j < this.degradation; j++) {
                    b = Text.replaceCharAt(b, b.length() - (j + 1), groupes[i].charAt(this.degradation - j - 1)); //on remplace autant de bits que nécessaire
                }

                this.file.seek(offset + this.getLengthEntete() + this.adresseDebut + i); //on se replace à l'endroit que l'on vient de lire
                this.file.write(Text.binToDec(b)); //on y inscrit la nouvelle valeur
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String decoderImage() {
        try {
            setAtBeginningBMP(); //on se déplace au début de l'entete
            String deg = "";
            for (int i = 0; i < 2; i++) {
                String s = Text.decToBin(file.read(), 8);
                deg += s.substring(s.length() - 2);
            } //on lit l'info sur le taux de dégradation
            this.degradation = Text.binToDec(deg);

            String adresseDebut = "";
            for (int i = 0; i < this.getLengthEntete() - 2; i++) {
                String s = Text.decToBin(file.read(), 8);
                adresseDebut += s.substring(s.length() - 2);
            } //on lit l'adresse de début (qui correspond au nombre d'octets à passer à partir de la fin de l'entete
            this.adresseDebut = Text.binToDec(adresseDebut);

            this.file.skipBytes(this.adresseDebut); //on se place au début du message

            String msg = "";
            String curr_char = readByteBMP(); //on lit un caractère du message
            while (!curr_char.equals(Text.binToString(Text.END_OF_STRING))) { //tant que le caractère actuel ne correspond pas à une fin de chaine
                msg += curr_char; //on ajoute ce caracètre au message
                curr_char = readByteBMP(); //on lit le caractère suivant
            }

            return msg;
        } catch (IOException e) {
            return "";
        }
    }

    public String readByteBMP() {
        try {
            String s = "";
            for (int i = 0; i < 8 / this.degradation; i++)//on lit par groupe de {this.degradation} bits.
            {
                String value = Text.decToBin(this.file.read(), 8); //on lit un octet
                for (int j = this.degradation - 1; j >= 0; j--) {
                    s += String.valueOf(value.charAt(value.length() - j - 1)); //on compose le caractère
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
            file.seek(0); //on se place au début du fichier
            this.file.skipBytes(10); //on se place à l'endroit où il y a l'info 

            String s = "";

            for (int i = 0; i < 4; i++) { //on lit l'info pour le début de l'image
                s = Text.decToBin(this.file.read(), 8) + s;
            }

            return Text.binToDec(s);
        } catch (IOException e) {
            return -1;
        }
    }

    public void setAtBeginningBMP() {
    //déplace le pointeur de fichier au début de l'entete du texte
        try {
            this.file.seek(this.getOffsetBMP());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLengthEntete()
    //retourne la taille en octets de l'entête
    {
        int lengthEntete = 2; //taux de dégradation
        int nbPixels = this.getHauteur() * this.getLargeur() * 3;
        int nbBits = 0;

        while (Math.pow(2, nbBits) < nbPixels) {
            nbBits++;
        } //on cherche le nombre de bits nécessaires pour coder nbPixels

        lengthEntete += nbBits / 2 + nbBits % 2; //on veut une entete paire, on change les deux derniers pixels de chaque octet.

        return lengthEntete;
    }

    public int genRandomAdress(int tailleTexte) {
    //génère une adresse de départ pour le texte
        int nbOctets = this.getNbOctets() - this.getLengthEntete() - 1; //nombre d'octets modifiables dans l'image
        int nbOctetsNecessairesTexte = tailleTexte + 1 * (int) (8 / Math.pow(2, this.degradation)); //nombre d'octets nécessaires pour enregistrer le texte
        int adresseMax = nbOctets - nbOctetsNecessairesTexte; //adresse max que l'on veut générer

        Random adresse = new Random();
        return adresse.nextInt(adresseMax);
    }
}
