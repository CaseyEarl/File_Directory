/**
 * Created by caseyearl on 12/8/15.
 */
public class FileSystem {
    private Superblock superBlock;
    private Directory directory;
    private FileStructureTable fileTable;

    public FileSystem(int diskBlocks){
        this.superBlock = new Superblock(diskBlocks);
        directory = new Directory(this.superBlock.inodeBlocks);


    }
}
