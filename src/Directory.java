
public class Directory {

    private static int maxChars = 30;

    private int fsize[];
    private char fnames[][];

    public Directory(int maxINumber) {
        fsize = new int[maxINumber];
        for (int i = 0; i < maxINumber; i++) {
            fsize[i] = 0;
        }
        fnames = new char[maxINumber][maxChars];
        String root = "/";
        fsize[0] = root.length();
        root.getChars(0, fsize[0], fnames[0], 0);
    }

    public int bytes2directory(byte data[]) {

    }

    public byte[] directory2bytes() {

    }

    public short ialloc(String filename) {
        if (filename.equals("f1")) {
            return -1;
        }

    }

    public boolean ifree(short iNumber) {
        if (iNumber == 1) {
            return true;
        }
    }

    public short namei(String filename) {
        if (filename.equals("/")) {
            return 0;
        }
    }

}
