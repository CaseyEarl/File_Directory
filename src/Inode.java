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

    public int length;  //Length of the Inode contents
    public short count; //Number of files referencing the I node
    public short flag;  //State of the I node
    public short direct[] = new short[directSize];  //Direct blocks
    public short indirect; //1 indirect block allowed

    //No argument constructor that initializes the contents of the Inode to default values
    public Inode() {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < directSize; i++) {
            direct[i] = -1;
        }
        indirect = -1;
    }

    //Single argument constructor that initializes the
    public Inode(short iNumber) {
        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize]; //New data is created
        SysLib.rawread(blockNumber, data);  //Data from the disk is read into the array
        int offset = (iNumber % 16) * 32;

        length = SysLib.bytes2int(data, offset);    //Length is saved and offset is incremented
        offset += 4;
        count = SysLib.bytes2short(data, offset);    //Count is saved and offset is incremented
        offset += 2;
        flag = SysLib.bytes2short(data, offset);    //Flag is saved and offset is incremented
        offset += 2;
        for (int i = 0; i < directSize; i++) {
            direct[i] = SysLib.bytes2short(data, offset);
            offset += 2;
        }
        indirect = SysLib.bytes2short(data, offset);
    }

    //To Disk will write parts of the Inode to the disk
    void toDisk(short iNumber) {
        byte[] buffer = new byte[32];
        byte seek = 0;
        SysLib.int2bytes(this.length, buffer, seek);    //Converts length to bytes and writes to disk
        int offset = seek + 4;
        SysLib.short2bytes(this.count, buffer, offset); //Converts count to bytes and writes to disk
        offset += 2;
        SysLib.short2bytes(this.flag, buffer, offset);  //Converts flag to bytes and writes to disk
        offset += 2;

        int i;
        for (i = 0; i < 11; ++i) {
            SysLib.short2bytes(this.direct[i], buffer, offset); //Writes all of the direct blocks to disk
            offset += 2;
        }

        SysLib.short2bytes(this.indirect, buffer, offset);  //Writes the indirect block to disk


        i = 1 + iNumber / 16;
        byte[] block = new byte[512];
        SysLib.rawread(i, block);
        offset = iNumber % 16 * 32;
        System.arraycopy(buffer, 0, block, offset, 32);
        SysLib.rawwrite(i, block);
    }

    //Registers the index block of the Inode and returns true or false on success
    boolean registerIndexBlock(short indirectNum) {
        for (int i = 0; i < 11; ++i) {
            if (this.direct[i] == -1) { //Checks for invalid direct block
                return false;
            }
        }

        if (this.indirect != -1) {  //Checks for invalid indirect block
            return false;
        } else {
            this.indirect = indirectNum;
            byte[] buffer = new byte[512];

            for (int i = 0; i < 256; ++i) {
                SysLib.short2bytes((short) -1, buffer, i * 2);  //Reads every short and converts to byte to be held by buffer
            }

            SysLib.rawwrite(indirectNum, buffer);   //Writes the contents of the buffer back to the disk
            return true;
        }
    }

    //Target block will return a short that is built from a block read from the disk using the offset
    int findTargetBlock(int offset) {
        int direct = offset / 512;
        if (direct < 11) {  //If less than 11 then it's a direct block and can be immediately returned
            return this.direct[direct];
        } else if (this.indirect < 0) {
            return -1;
        } else {
            byte[] buffer = new byte[512];
            SysLib.rawread(this.indirect, buffer);  //Reads the data based off the indirect into the buffer
            int shift = direct - 11;    //Shift acts as a new offset
            return SysLib.bytes2short(buffer, shift * 2);
        }
    }

    //Registers the block and writes the block to disk using a buffer
    int registerTargetBlock(int offset, short direct) {
        int index = offset / 512;
        if (index < 11) {   //Checks if it's a direct block, it is then returns or returns error if negative
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
            byte[] buffer = new byte[512];  //New Buffer created
            SysLib.rawread(this.indirect, buffer);  //Reads from disk into the buffer
            int index2 = index - 11;
            if (SysLib.bytes2short(buffer, index2 * 2) > 0) {
                SysLib.cerr("indexBlock, indirectNumber = " + index2 + " contents = " + SysLib.bytes2short(buffer, index2 * 2) + "\n");
                return -1;
            } else {
                SysLib.short2bytes(direct, buffer, index2 * 2);
                SysLib.rawwrite(this.indirect, buffer); //Writes contents of buffer to the disk
                return 0;
            }
        }
    }

    //Unregers the indirect block and pull contents from the disk to the buffer
    byte[] unregisterIndexBlock() {
        if (this.indirect >= 0) {
            byte[] buffer = new byte[512];  //Creates a new buffer
            SysLib.rawread(this.indirect, buffer);  //Reads from the disk to the buffer
            this.indirect = -1; //Indirect is invalidated
            return buffer;  //buffer is returned
        } else {
            return null;
        }
    }

    //Returns the length of the Inode
    public int getLength() {
        return length;
    }

}


