import java.io.File;
import java.io.InterruptedIOException;

public class FileStructureTable {
    private Directory dir;

    FileStructureTable(Directory setDir) {
        setDir = dir;
    }


    public synchronized FileTableEntry falloc(String fname, String mode) {
        short iNumber = -1;
        Inode inode = null;

        while (true) {
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
                    } else if (inode.flag == "to be deleted") {
                        iNumber = -1;
                        return null;
                    } else if (mode.equals("w")) {

                    }

                }
            }
        }
        inode.count++;
        inode.toDisk(iNumber);
        FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
        //table.addElement(e); //create table entry and register it
        return e;
    }

}
