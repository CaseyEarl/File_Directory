import java.io.File;
import java.io.InterruptedIOException;
import java.util.Vector;

public class FileTable {
    private Directory dir;
    private Vector<FileTableEntry> table;

    FileTable(Directory setDir) {

        dir = setDir;
        table = new Vector<FileTableEntry>();
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
                } else if (inode.flag > 1) {
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


    public synchronized boolean ffree(FileTableEntry toDelete){

        toDelete.count--;
        toDelete.inode.toDisk(toDelete.iNumber);
        table.removeElement(toDelete);
        toDelete = null;
        this.notify();
        return true;


    }

    public boolean fempty() {
        return this.table.size() <= 1;
    }

    public synchronized int write(int fdId, byte[] buffer) {


        for (int i = 0; i < this.table.size(); i++) {
            if (this.table.elementAt(i).iNumber == fdId) {
                return this.table.elementAt(i).write(buffer);
            }
        }
        short iNum = (short) fdId;
        Inode toAdd = new Inode(iNum);
        toAdd.flag = 1;
        FileTableEntry ftToAdd = new FileTableEntry(toAdd, iNum, "w");
        int toReturn = ftToAdd.write(buffer);
        this.table.addElement(ftToAdd);
        toAdd.flag = 0;
        return toReturn;
    }


}
