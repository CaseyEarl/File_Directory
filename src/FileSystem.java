import java.io.File;

/**
 * Created by caseyearl on 12/8/15.
 */
public class FileSystem {
    private Superblock superBlock;
    private Directory directory;
    private FileTable fileTable;

    public FileSystem(int diskBlocks){
        this.superBlock = new Superblock(diskBlocks);
        this.directory = new Directory(this.superBlock.inodeBlocks);
        this.fileTable = new FileTable(this.directory);

        FileTableEntry dir = open("/", "r");
        int dirSize = fsize(dir);
        if(dirSize > 0){
            byte[] dirData = new byte[dirSize];
            read(dir, dirData);
            this.directory.bytes2directory(dirData);
        }
        close(dir);
    }

    public FileTableEntry open(String fileName, String mode){
        FileTableEntry ftEnt = fileTable.falloc(fileName, mode);
        if(mode.equals("w") || mode.equals("w+") || mode.equals("a") ){
            if(deallocAllBlocks(ftEnt) == false){
                return null;//
            }
        }
        if (mode.equals("r")) {

           // if (deallocAllBlocks(ftEnt))
        }
        return ftEnt;
    }

    public int write(FileTableEntry ftEnt, byte[] buffer){
        return -1;
    }

    public int read(FileTableEntry ftEnt, byte[] buffer){
        return -1;
    }

    public int fsize(FileTableEntry ftEnt){


        //return SysLib.int2bytes fileTable[ftEnt]
        return -1;
    }

    public int close(FileTableEntry ftEnt){
       //go through the entire table
        --ftEnt.count;
        if (ftEnt.count > 0) {
            return 0;
        }
        if (this.fileTable.ffree(ftEnt)) {
            return 0;
        } else {
            return -1;
        }


        /*


        synchronized(var1) {
            --var1.count;
            if(var1.count > 0) {
                return true;
            }
        }

        return this.filetable.ffree(var1);
    }
         */
    }

    public boolean deallocAllBlocks(FileTableEntry ftEnt){

        if (ftEnt.inode.count != 1){ //does not exist
            return false;
        } else {
            byte[] ftEnt2 = ftEnt.inode.unregisterIndexBlock();
            if (ftEnt2 != null) {
                byte ftEnt3 = 0;

            }
        }



        return false;


        /*
         if(var1.inode.count != 1) {
            return false;
        } else {
            byte[] var2 = var1.inode.unregisterIndexBlock();
            if(var2 != null) {
                byte var3 = 0;

                short var4;
                while((var4 = SysLib.bytes2short(var2, var3)) != -1) {
                    this.superblock.returnBlock(var4);
                }
            }

            int var5 = 0;

            while(true) {
                Inode var10001 = var1.inode;
                if(var5 >= 11) {
                    var1.inode.toDisk(var1.iNumber);
                    return true;
                }

                if(var1.inode.direct[var5] != -1) {
                    this.superblock.returnBlock(var1.inode.direct[var5]);
                    var1.inode.direct[var5] = -1;
                }

                ++var5;
            }
        }
         */
    }

}
