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

//File Table will hold a Reference to the directory and a vector of the file table entries
public class FileTable {
    private Directory dir;
    private Vector<FileTableEntry> table;

    //Single argument constructor that will set the directory and initialize a new vector of entries
    FileTable(Directory setDir) {
        dir = setDir;
        table = new Vector<FileTableEntry>();
    }

    //Allocates memory and pulls file data information from the disk and into memory
    public synchronized FileTableEntry falloc(String fname, String mode) {
        short iNumber = -1;
        Inode inode = null;


        iNumber = (fname.equals("/") ? 0 : dir.namei(fname));   //Checks for root directory
        if (iNumber >= 0) {
            inode = new Inode(iNumber); //Creates new Inode
            if (mode.equals("r")) {
                if (inode.flag == 0) { //flag is short. compare to read

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

        inode.count++;  //Since we just pulled a file into memory, there is one reference to it
        inode.toDisk(iNumber);  //Writes to disk
        FileTableEntry e = new FileTableEntry(inode, iNumber, mode);    //Creates new filetable entry based off information passed
        table.addElement(e); //create table entry and register it
        return e;
    }

    //Deletes the FileTable Entry
    public synchronized boolean ffree(FileTableEntry toDelete){

        toDelete.count--;   //Removes a reference
        toDelete.inode.toDisk(toDelete.iNumber);    //Writes information to disk
        table.removeElement(toDelete);  //Removes element from vector
        toDelete = null;    //Reference is set to null
        this.notify();  //Notify
        return true;


    }


}
