/**
 * Inode.java
 * <p/>
 * Authors: Casey Earl and Norell Tagle
 * <p/>
 * Dec 18 2015
 */

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


    void toDisk(short iNumber) {
        byte[] buffer = new byte[32];
        byte seek = 0;
        SysLib.int2bytes(this.length, buffer, seek);
        int offset = seek + 4;
        SysLib.short2bytes(this.count, buffer, offset);
        offset += 2;
        SysLib.short2bytes(this.flag, buffer, offset);
        offset += 2;

        int i;
        for (i = 0; i < 11; ++i) {
            SysLib.short2bytes(this.direct[i], buffer, offset);
            offset += 2;
        }

        SysLib.short2bytes(this.indirect, buffer, offset);


        i = 1 + iNumber / 16;
        byte[] block = new byte[512];
        SysLib.rawread(i, block);
        offset = iNumber % 16 * 32;
        System.arraycopy(buffer, 0, block, offset, 32);
        SysLib.rawwrite(i, block);
    }


    boolean registerIndexBlock(short indirectNum) {
        for (int i = 0; i < 11; ++i) {
            if (this.direct[i] == -1) {
                return false;
            }
        }

        if (this.indirect != -1) {
            return false;
        } else {
            this.indirect = indirectNum;
            byte[] buffer = new byte[512];

            for (int i = 0; i < 256; ++i) {
                SysLib.short2bytes((short) -1, buffer, i * 2);
            }

            SysLib.rawwrite(indirectNum, buffer);
            return true;
        }
    }


    int findTargetBlock(int offset) {
        int direct = offset / 512;
        if (direct < 11) {
            return this.direct[direct];
        } else if (this.indirect < 0) {
            return -1;
        } else {
            byte[] buffer = new byte[512];
            SysLib.rawread(this.indirect, buffer);
            int shift = direct - 11;
            return SysLib.bytes2short(buffer, shift * 2);
        }
    }

    int registerTargetBlock(int offset, short direct) {
        int index = offset / 512;
        if (index < 11) {
            if (this.direct[index] >= 0) {
                return -1;
            } else if (index > 0 && this.direct[index - 1] == -1) {
                return -2;
            } else {
                this.direct[index] = direct;
                return 0;
            }
        } else if (this.indirect < 0) {
            return -3;
        } else {
            byte[] buffer = new byte[512];
            SysLib.rawread(this.indirect, buffer);
            int index2 = index - 11;
            if (SysLib.bytes2short(buffer, index2 * 2) > 0) {
                SysLib.cerr("indexBlock, indirectNumber = " + index2 + " contents = " + SysLib.bytes2short(buffer, index2 * 2) + "\n");
                return -1;
            } else {
                SysLib.short2bytes(direct, buffer, index2 * 2);
                SysLib.rawwrite(this.indirect, buffer);
                return 0;
            }
        }
    }

    byte[] unregisterIndexBlock() {
        if (this.indirect >= 0) {
            byte[] buffer = new byte[512];
            SysLib.rawread(this.indirect, buffer);
            this.indirect = -1;
            return buffer;
        } else {
            return null;
        }
    }

    public int getLength() {
        return length;
    }

}


