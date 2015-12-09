
public class Inode {
    private final static int iNodeSize = 32;
    private final static int directSize = 32;

    public int length;
    public short count;
    public short flag;
    public short direct[] = new short[directSize];
    public short indirect;

    public Inode() {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < directSize; i++) {
            direct[i] = -1;
        }
        indirect = -1;
    }

    public Inode(short iNumber) {
        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(blockNumber, data);
        int offset = (iNumber % 16) * 32;

        length = SysLib.bytes2int(data, offset);
        offset += 4;
        count = SysLib.bytes2short(data, offset);
        offset += 2;
        flag = SysLib.bytes2short(data, offset);
        offset += 2;



    }

    int toDisk (short iNumber) {


        return 1;
    }




}
