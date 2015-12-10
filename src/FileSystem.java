import java.io.File;

/**
 * Created by caseyearl on 12/8/15.
 */
public class FileSystem {
    private Superblock superBlock;
    private Directory directory;
    private FileTable fileTable;

    public FileSystem(int files){
        this.superBlock = new Superblock(files);
        this.directory = new Directory(this.superBlock.inodeBlocks);
        this.fileTable = new FileTable(this.directory);

        FileTableEntry dir = open("/", "r");
        int dirSize = fsize(dir);
        if(dirSize > 0){
            byte[] dirData = new byte[dirSize];
            read(dir.iNumber, dirData);
            this.directory.bytes2directory(dirData);
        }
        close(dir);
    }

    public FileTableEntry open(String fileName, String mode){
        FileTableEntry ftEnt = fileTable.falloc(fileName, mode);
        if(mode.equals("w")){
            if(deallocAllBlocks(ftEnt) == false){
                return null;
            }
        }
        return ftEnt;
    }

    public void format(int iBlocks){
        this.superBlock.format(iBlocks);
    }

    public int write(int ftEnt, byte[] buffer){
        return fileTable.write(ftEnt, buffer);
    }

    public int read(int ftEnt, byte[] buffer){
        return -1;
    }

    public int fsize(FileTableEntry ftEnt){

        return -1;
    }

    public boolean close(FileTableEntry ftEnt){
        return false;
    }

    public boolean deallocAllBlocks(FileTableEntry ftEnt){
        return false;
    }

}
