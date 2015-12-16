
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
        int offset = 0;
        for (int i = 0; i < fsize.length; i++, offset += 4) {
            fsize[i] = SysLib.bytes2int(data, offset);
        }
        for (int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
            String fname = new String(data, offset, maxChars * 2);
            fname.getChars(0, fsize[i], fnames[i], 0);
        }
        return 0;
    }


    public short ialloc(String filename) {
        for (short i = 1; i < this.fsize.length; i++) {
            if (this.fsize[i] == 0) {
                this.fsize[i] = Math.min(filename.length(), maxChars);
                filename.getChars(0, this.fsize[i], this.fnames[i], 0);
                return i;
            }
        }
        return (short) -1;
    }

    public byte[] directory2bytes() {
        return new byte[100];
    }

    public boolean ifree(short iNumber) {
        this.fsize[iNumber] = 0;
        return true;
    }

    public short namei(String filename) {
        //must return the inumber of file
        if (filename.equals("/")) {
            return 0;
        }
        return 1;

    }


}
