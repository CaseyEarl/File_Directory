
public class Superblock {
    private final int inodeBlocks = 64;
    public int totalBlocks;
    public int totalInodes;
    public int freeList;

    public Superblock(int diskSize){
        byte[] superBlock = new byte[Disk.blockSize];
        SysLib.rawread(0, superBlock);
        totalBlocks = SysLib.bytes2int(superBlock, 0);
        totalInodes = SysLib.bytes2int(superBlock, 4);
        freeList = SysLib.bytes2int(superBlock, 8);

        if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2){
            return;
        }
        else{
            totalBlocks = diskSize;
            //format(inodeBlocks);
        }

    }

    void sync() {
        byte[] tempBlock = new byte[512];
        SysLib.int2bytes(this.totalBlocks, tempBlock, 0);
        SysLib.int2bytes(this.inodeBlocks, tempBlock, 4);
        SysLib.int2bytes(this.freeList, tempBlock, 8);
        SysLib.rawwrite(0, tempBlock);
        SysLib.cerr("Superblock synchronized\n");
    }

    void format() {
        this.totalInodes = this.inodeBlocks;

        for(short var2 = 0; var2 < this.inodeBlocks; ++var2) {
            Inode var3 = new Inode();
            var3.flag = 0;
            var3.toDisk(var2);
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
}
