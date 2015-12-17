/**
 * Directory.java
 *
 * Authors: Casey Earl and Norell Tagle
 *
 * Dec 18 2015
 */

//Directory holds the names and sizes of the files that it holds
public class Directory {

    private static int maxChars = 30;

    private int fsize[];
    private char fnames[][];

    //Single argument constructor that initializes the 2 arrays to the size of the max Inumbers and chars
    public Directory(int maxINumber) {
        fsize = new int[maxINumber];
        for (int i = 0; i < maxINumber; i++) {
            fsize[i] = 0;   //Initializes all elements to 0
        }
        fnames = new char[maxINumber][maxChars];    //2D Array of max I numbers and max chars
        String root = "/";  //Root directory name
        fsize[0] = root.length();
        root.getChars(0, fsize[0], fnames[0], 0);
    }

    //Conversion of bytes to directories, fills the arrays with contents converted to directories from disk
    public int bytes2directory(byte data[]) {
        int offset = 0;
        for (int i = 0; i < fsize.length; i++, offset += 4) {   //runs through file size and reads the data from the disk
            fsize[i] = SysLib.bytes2int(data, offset);
        }
        for (int i = 0; i < fnames.length; i++, offset += maxChars * 2) {   //runs through the file names and reads the data from the disk
            String fname = new String(data, offset, maxChars * 2);
            fname.getChars(0, fsize[i], fnames[i], 0);
        }
        return 0;
    }


    //Deletes the file at the passed in index
    public boolean ifree(short iNumber) {
        this.fsize[iNumber] = 0;
        return true;
    }

    //Checks if the if the file name is valid (Not root)
    public short namei(String filename) {
        //must return the inumber of file
        if (filename.equals("/")) {
            return 0;
        }
        return 1;

    }


}
