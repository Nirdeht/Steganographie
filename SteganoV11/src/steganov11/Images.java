import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Images {
    private RandomAccessFile file;
    
    public Images(String path) throws FileNotFoundException
    {
        this.file = new RandomAccessFile(path, "rw");
    }
    
    public void coderImages(String texteACoder)
    {

        String texteBinaire = steganov11.Text.stringToBin(texteACoder);
        texteBinaire += steganov11.Text.END_OF_STRING; //on ajoute le caractère fin de chaine '\0'
		String[] groupes = steganov11.Text.getParts(texteBinaire, 2); //découpe la chaine en chaine de deux bits 

        try
        {
            int offset = this.getOffsetBMP();

            for(int i=0; i<groupes.length; i++)//pour chaque groupe de deux bits
            {
                String b = steganov11.Text.decToBin(this.file.read(), 8);

				b = steganov11.Text.replaceCharAt(b, b.length()-2, groupes[i].charAt(0));
				b = steganov11.Text.replaceCharAt(b, b.length()-1, groupes[i].charAt(1));

                this.file.seek(offset+i);
                this.file.write(steganov11.Text.binToDec(b));
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public String decoderImages()
    {
        try
        {
            setAtBeginningBMP();

            String msg="";
            String curr_char = readByteBMP();
            while(!curr_char.equals(steganov11.Text.binToString(steganov11.Text.END_OF_STRING)))
            {
                msg += curr_char;
                curr_char = readByteBMP();
            }

            return msg;
        }
        catch(Exception e)
        {
            return "";
        }
    }
    
    public String readByteBMP()
    {
        try
        {
            String s = "";

            for(int i=0; i<4; i++)//on lit par groupe de deux bits. Pour former un octet il faut en lire 4 groupes
            {
                String value = steganov11.Text.decToBin(this.file.read(), 8);
                s += String.valueOf(value.charAt(value.length()-2)) + String.valueOf(value.charAt(value.length()-1));
            }

            return steganov11.Text.binToString(s);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
    
    public int getOffsetBMP()
    {
        try
        {
            this.file.skipBytes(10);

            String s = "";

            for(int i=0; i<4; i++)//on lit l'info pour le début de l'Images
                s = steganov11.Text.decToBin(this.file.read(), 8) + s;

            return steganov11.Text.binToDec(s);
        }
        catch(Exception e)
        {
            return -1;
        }
    }
    
    public void setAtBeginningBMP()
    {
        try
        {
            this.file.skipBytes(10); //on se déplace jusqu'à l'info sur l'adresse de début de l'Images

            String s = "";

            for(int i=0; i<4; i++)//on lit l'info sur le début de l'Images, codé sur 4 octets (en sens inverse)
                    s = steganov11.Text.decToBin(this.file.read(), 8) + s;

            int offset = steganov11.Text.binToDec(s);
            this.file.seek(offset);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
