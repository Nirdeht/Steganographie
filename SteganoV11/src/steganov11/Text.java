public class Text
{
    public static final String END_OF_STRING = "00000000";
	
	public static String[] getParts(String s, int partitionSize)
	//découpe une chaine de caractères en chaines de partitionSize caractères
	{ 
        String[] groupes = new String[s.length()/partitionSize];
        
        for (int i=0; i<s.length(); i+=partitionSize)
            groupes[i/partitionSize] = new String(s.substring(i, Math.min(s.length(), i + partitionSize))); //on extrait la chaine caractère suivante de taille partitionSize
            
        return groupes;
    }
	
    public static String decToBin(int dec, int length)
    //dec : à la valeur décimale à coder en binaire
    //length : la taille de la chaine de caractère (on complète à gauche par des 0)
    //si le nombre doit etre codé sur plus de length bits, alors length est ignoré
    {
        String bin="";
        while(dec!=0)
        {
            bin=String.valueOf(dec%2)+bin;
            dec=dec/2;
        } //transformation par division euclidienne

        while(bin.length() < length) //ajout de 0 à gauche 
            bin = "0" + bin;

        return bin;
    }

    public static int binToDec(String s)
    //s : représentation binaire du nombre décimal
    //retourne le nombre décimal
    {
        int res=0;
        for(int i=s.length()-1; i>=0; i--)
                res+=Math.pow(2.0, (double) (s.length()-1-i)) * Character.getNumericValue(s.charAt(i));

        return res;
    }

    public static String stringToBin(String s)
    //s : texte à transformer en binaire
    //retourne la représentation des caractères de s en binaire
    {
        String binary="";
        byte[] infoBin;

        try
        {
            infoBin = s.getBytes("US-ASCII");
            for (byte b : infoBin)
            {
                String bin = Integer.toBinaryString(b); 
                while ( bin.length() < 8 )
                        bin = "0" + bin;
                binary+=bin;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return binary;
    }

    public static String replaceCharAt(String s, int pos, char c)
    {
        return s.substring(0,pos) + c + s.substring(pos+1);
    }

    public static String binToString(String s)
    {
        String output = "";
        for(int i = 0; i <= s.length() - 8; i+=8) //tous les 8 bits
        {
            int k = binToDec(s.substring(i, i+8));
            output += (char) k;
        }

        return output;   
    }
}
