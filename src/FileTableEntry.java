/*
File Table Entry that holds the Inode, a seek ptr, inumber, count and mode.
Very basic data type
 */


public class FileTableEntry {
    public int seekPtr;
    public final Inode inode;
    public final short iNumber;
    public int count;
    public final String mode;

    public FileTableEntry(Inode i, short inumber, String m) {
        seekPtr = 0;
        inode = i;
        iNumber = inumber;
        count = 1;
        mode = m;
        if (mode.compareTo("a") == 0) {
            seekPtr = inode.length;
        }
    }

    public int write(byte[] buffer){
        return this.inode.write(seekPtr, iNumber,buffer);
    }

}
