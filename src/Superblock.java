
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
            format(inodeBlocks);
        }

    }
}
