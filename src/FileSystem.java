/**
 * Created by caseyearl on 12/8/15.
 */
public class FileSystem {
    private Superblock superBlock;
    private Directory directory;
    private FileStructureTable fileTable;

    public FileSystem(int diskBlocks){
        this.superBlock = new Superblock(diskBlocks);
        this.directory = new Directory(this.superBlock.inodeBlocks);
        this.fileTable = new FileStructureTable(this.directory);

        FileTableEntry dir = open("/", "r");
        int dirSize = fsize(dir);
        if(dirSize > 0){
            byte[] dirData = new byte[dirSize];
            read(dir, dirData);
            this.directory.bytes2directory(dirData);
        }
        close(dir);
    }

}
