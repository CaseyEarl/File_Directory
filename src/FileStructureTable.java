import java.io.File;
import java.io.InterruptedIOException;
import java.util.Vector;

public class FileStructureTable {
    private Directory dir;
    private Vector table;

    FileStructureTable(Directory setDir) {
        setDir = dir;
    }


    public synchronized FileTableEntry falloc(String fname, String mode) {
        short iNumber = -1;
        Inode inode = null;


        iNumber = (fname.equals("/") ? 0 : dir.namei(fname));
        if (iNumber >= 0) {
            inode = new Inode(iNumber);
            if (mode.equals("r")) {
                if (inode.flag == 0) { //CHECK THIS. flag is short. compare to read which = 0 ?

                } else if (inode.flag == 1) {
                    try {
                        wait();

                    } catch (InterruptedException e) {

                    }
                } else if (inode.flag > 1) {  //CHECK THIS. flag is short. compare to read which = 0 ?
                    iNumber = -1;
                    return null;
                } else if (mode.equals("w")) {

                }

            }
        }

        inode.count++;
        inode.toDisk(iNumber);
        FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
        table.addElement(e); //create table entry and register it
        return e;
    }

}
