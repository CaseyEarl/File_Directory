

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

    void toDisk(short var1) {
        byte[] tempByte = new byte[32];
        byte offset = 0;

        SysLib.int2bytes(this.length, tempByte, offset);
        int offsetInt = offset + 4;

        SysLib.short2bytes(this.count, tempByte, offsetInt);
        offsetInt += 2;

        SysLib.short2bytes(this.flag, tempByte, offsetInt);
        offsetInt += 2;

        int i;
        for(i = 0; i < 11; ++i) {
            SysLib.short2bytes(this.direct[i], tempByte, offsetInt);
            offsetInt += 2;
        }

        SysLib.short2bytes(this.indirect, tempByte, offsetInt);
        i = 1 + var1 / 16;
        byte[] var5 = new byte[512];
        SysLib.rawread(i, var5);
        offsetInt = var1 % 16 * 32;

        System.arraycopy(tempByte, 0, var5, offsetInt, 32);
        SysLib.rawwrite(i, var5);
    }

    public int write(int seek, int iNumber, byte[] buffer) {
        //set the length

        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize];
        //SysLib.rawwrite(blockNumber, buffer);
        int offset = (iNumber % 16) * 32;
        length = SysLib.bytes2int(buffer,offset);
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

    int findIndexBlock() {
        return this.indirect;
    }

    boolean registerIndexBlock(short var1) {
        for (int var2 = 0; var2 < 11; ++var2) {
            if (this.direct[var2] == -1) {
                return false;
            }
        }

        if (this.indirect != -1) {
            return false;
        } else {
            this.indirect = var1;
            byte[] var4 = new byte[512];

            for (int var3 = 0; var3 < 256; ++var3) {
                SysLib.short2bytes((short) -1, var4, var3 * 2);
            }

            SysLib.rawwrite(var1, var4);
            return true;
        }
    }

    int findTargetBlock(int var1) {
        int var2 = var1 / 512;
        if (var2 < 11) {
            return this.direct[var2];
        } else if (this.indirect < 0) {
            return -1;
        } else {
            byte[] var3 = new byte[512];
            SysLib.rawread(this.indirect, var3);
            int var4 = var2 - 11;
            return SysLib.bytes2short(var3, var4 * 2);
        }
    }

    int registerTargetBlock(int var1, short var2) {
        int var3 = var1 / 512;
        if (var3 < 11) {
            if (this.direct[var3] >= 0) {
                return -1;
            } else if (var3 > 0 && this.direct[var3 - 1] == -1) {
                return -2;
            } else {
                this.direct[var3] = var2;
                return 0;
            }
        } else if (this.indirect < 0) {
            return -3;
        } else {
            byte[] var4 = new byte[512];
            SysLib.rawread(this.indirect, var4);
            int var5 = var3 - 11;
            if (SysLib.bytes2short(var4, var5 * 2) > 0) {
                SysLib.cerr("indexBlock, indirectNumber = " + var5 + " contents = " + SysLib.bytes2short(var4, var5 * 2) + "\n");
                return -1;
            } else {
                SysLib.short2bytes(var2, var4, var5 * 2);
                SysLib.rawwrite(this.indirect, var4);
                return 0;
            }
        }
    }

    byte[] unregisterIndexBlock() {
        if (this.indirect >= 0) {
            byte[] var1 = new byte[512];
            SysLib.rawread(this.indirect, var1);
            this.indirect = -1;
            return var1;
        } else {
            return null;
        }
    }

    public int getLength() {
        return length;
    }

}



/*
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class Inode {
    public static final int iNodeSize = 32;
    public static final int directSize = 11;
    public static final int NoError = 0;
    public static final int ErrorBlockRegistered = -1;
    public static final int ErrorPrecBlockUnused = -2;
    public static final int ErrorIndirectNull = -3;
    public int length;
    public short count;
    public short flag;
    public short[] direct = new short[11];
    public short indirect;

    Inode() {
        this.length = 0;
        this.count = 0;
        this.flag = 1;

        for(int var1 = 0; var1 < 11; ++var1) {
            this.direct[var1] = -1;
        }

        this.indirect = -1;
    }

    Inode(short var1) {
        int var2 = 1 + var1 / 16;
        byte[] var3 = new byte[512];
        SysLib.rawread(var2, var3);
        int var4 = var1 % 16 * 32;
        this.length = SysLib.bytes2int(var3, var4);
        var4 += 4;
        this.count = SysLib.bytes2short(var3, var4);
        var4 += 2;
        this.flag = SysLib.bytes2short(var3, var4);
        var4 += 2;

        for(int var5 = 0; var5 < 11; ++var5) {
            this.direct[var5] = SysLib.bytes2short(var3, var4);
            var4 += 2;
        }

        this.indirect = SysLib.bytes2short(var3, var4);
        var4 += 2;
    }



    int findIndexBlock() {
        return this.indirect;
    }

    boolean registerIndexBlock(short var1) {
        for(int var2 = 0; var2 < 11; ++var2) {
            if(this.direct[var2] == -1) {
                return false;
            }
        }

        if(this.indirect != -1) {
            return false;
        } else {
            this.indirect = var1;
            byte[] var4 = new byte[512];

            for(int var3 = 0; var3 < 256; ++var3) {
                SysLib.short2bytes((short)-1, var4, var3 * 2);
            }

            SysLib.rawwrite(var1, var4);
            return true;
        }
    }

    int findTargetBlock(int var1) {
        int var2 = var1 / 512;
        if(var2 < 11) {
            return this.direct[var2];
        } else if(this.indirect < 0) {
            return -1;
        } else {
            byte[] var3 = new byte[512];
            SysLib.rawread(this.indirect, var3);
            int var4 = var2 - 11;
            return SysLib.bytes2short(var3, var4 * 2);
        }
    }

    int registerTargetBlock(int var1, short var2) {
        int var3 = var1 / 512;
        if(var3 < 11) {
            if(this.direct[var3] >= 0) {
                return -1;
            } else if(var3 > 0 && this.direct[var3 - 1] == -1) {
                return -2;
            } else {
                this.direct[var3] = var2;
                return 0;
            }
        } else if(this.indirect < 0) {
            return -3;
        } else {
            byte[] var4 = new byte[512];
            SysLib.rawread(this.indirect, var4);
            int var5 = var3 - 11;
            if(SysLib.bytes2short(var4, var5 * 2) > 0) {
                SysLib.cerr("indexBlock, indirectNumber = " + var5 + " contents = " + SysLib.bytes2short(var4, var5 * 2) + "\n");
                return -1;
            } else {
                SysLib.short2bytes(var2, var4, var5 * 2);
                SysLib.rawwrite(this.indirect, var4);
                return 0;
            }
        }
    }

    byte[] unregisterIndexBlock() {
        if(this.indirect >= 0) {
            byte[] var1 = new byte[512];
            SysLib.rawread(this.indirect, var1);
            this.indirect = -1;
            return var1;
        } else {
            return null;
        }
    }
}

 */

