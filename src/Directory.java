
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
        return 1;
    }

    public byte[] directory2bytes() {
        return new byte[100];
    }


}
