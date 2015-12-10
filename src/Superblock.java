//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class Superblock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks;
    public int inodeBlocks;
    public int freeList;

    public Superblock(int diskSize) {
        byte[] var2 = new byte[diskSize];
        SysLib.rawread(0, var2);
        this.totalBlocks = SysLib.bytes2int(var2, 0);
        this.inodeBlocks = SysLib.bytes2int(var2, 4);
        this.freeList = SysLib.bytes2int(var2, 8);
        if(this.totalBlocks != diskSize || this.inodeBlocks <= 0 || this.freeList < 2) {
            this.totalBlocks = diskSize;
            SysLib.cerr("default format( 64 )\n");
            this.format(defaultInodeBlocks);
        }
    }

    void sync() {
        byte[] var1 = new byte[512];
        SysLib.int2bytes(this.totalBlocks, var1, 0);
        SysLib.int2bytes(this.inodeBlocks, var1, 4);
        SysLib.int2bytes(this.freeList, var1, 8);
        SysLib.rawwrite(0, var1);
        SysLib.cerr("Superblock synchronized\n");
    }

    void format(int iBlocks) {
        this.inodeBlocks = iBlocks;

        for(short i = 0; i < this.inodeBlocks; ++i) {
            Inode iNode = new Inode();
            iNode.flag = 0;
            iNode.toDisk(i);
        }

        this.freeList = 2 + this.inodeBlocks * 32 / 512;

        for(int var5 = this.freeList; var5 < this.totalBlocks; ++var5) {
            byte[] var6 = new byte[512];

            for(int var4 = 0; var4 < 512; ++var4) {
                var6[var4] = 0;
            }

            SysLib.int2bytes(var5 + 1, var6, 0);
            SysLib.rawwrite(var5, var6);
        }

        this.sync();
    }

    public int getFreeBlock() {
        int free = this.freeList;
        if(free != -1) {
            byte[] var2 = new byte[512];
            SysLib.rawread(free, var2);
            this.freeList = SysLib.bytes2int(var2, 0);
            SysLib.int2bytes(0, var2, 0);
            SysLib.rawwrite(free, var2);
        }

        return free;
    }

    public boolean returnBlock(int var1) {
        if(var1 < 0) {
            return false;
        } else {
            byte[] var2 = new byte[512];

            for(int var3 = 0; var3 < 512; ++var3) {
                var2[var3] = 0;
            }

            SysLib.int2bytes(this.freeList, var2, 0);
            SysLib.rawwrite(var1, var2);
            this.freeList = var1;
            return true;
        }
    }
}
