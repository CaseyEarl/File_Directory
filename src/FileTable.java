/**
 * FileTable.java
 *
 * Authors: Casey Earl and Norell Tagle
 *
 * Dec 18 2015
 */

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


}
