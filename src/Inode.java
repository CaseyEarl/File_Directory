
public class Inode {
    private final static int iNodeSize = 32;
    private final static int directSize = 11;

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
        for (int i = 0; i < directSize; i++) {
            direct[i] = SysLib.bytes2short(data, offset);
            offset += 2;
        }
        indirect = SysLib.bytes2short(data, offset);
    }

    int toDisk(FileTableEntry iNumber) {
        return 1;
    }

    public void toDisk(short iNumber) {

        //return 1;
    }

    public int write(int seek, int iNumber , byte[] buffer) {

        int blockNumber = 1 + iNumber / 16;
        //byte[] data = new byte[Disk.blockSize];
        //SysLib.rawwrite(blockNumber, buffer);
        int offset = (iNumber % 16) * 32;
        //length = SysLib.bytes2int(data, offset);
        offset += 4;
        //count = SysLib.bytes2short(data, offset);
        count++;
        offset += 2;
        //flag = SysLib.bytes2short(data, offset);
        offset += 2;
        //offset += seek;
        for (int i = 0; i < directSize && i < buffer.length; i++) {
            direct[i] = buffer[i];
            offset += 2;
        }
        indirect = buffer[buffer.length - 1];
        return buffer.length;
    }

    public byte[] unregisterIndexBlock()
        {

        if (this.indirect >= 0) {
            byte[] toReturn = new byte[512];
            SysLib.rawread(this.indirect, toReturn);
            this.indirect = -1;
            return toReturn;
        } else {
            return null;
        }

    }


}
