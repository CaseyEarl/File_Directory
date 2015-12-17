/**
 * FileSystem.java
 *
 * Authors: Casey Earl and Norell Tagle
 * Based off Design from Professor
 *
 * Dec 18 2015
 */
public class FileSystem {

    //File System must hold a superblock, directory and filetable
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;


    //Constructor
    //Take in the diskSize to initialize the private variables (superbock, file table, directory, etc)
    public FileSystem(int diskSize) {

        this.superblock = new SuperBlock(diskSize);
        this.directory = new Directory(this.superblock.inodeBlocks);
        this.filetable = new FileTable(this.directory);
        FileTableEntry ftEnt = this.open("/", "r"); //Opens root directory file
        int ftEntSize = this.fsize(ftEnt);

        if (ftEntSize > 0) { //if the size is valid
            byte[] buffer = new byte[ftEntSize];    //Creates a buffer used to read from disk
            this.read(ftEnt, buffer);   //reads from disk into buffer
            this.directory.bytes2directory(buffer); //Creates a directory from the buffer
        }
        this.close(ftEnt);
    }


    // Takes in a diskSize to determine number of files to be created
    // Return a true on success, false otherwise
    boolean format(int diskSize) {
        this.superblock.format(diskSize);   //Calls Superblock format
        this.directory = new Directory(this.superblock.inodeBlocks);    //initializes Directory
        this.filetable = new FileTable(this.directory);     //initializes FileTable
        return true;
    }


    // Open the file specified by fileName and check if mode is "r","w", "w+", or "a"
    FileTableEntry open(String fileName, String mode) {
        FileTableEntry ftEnt = this.filetable.falloc(fileName, mode);   //Allocates memory to opena  file and returns entry
        return mode == "w" && !this.deallocAllBlocks(ftEnt) ? null : ftEnt; //Checks if the mode is write accessible
    }


    // Close the file and unregister the file descriptor of the file table entry passed in
    // Return true on success and false otherwise
    boolean close(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            --ftEnt.count;
            if (ftEnt.count > 0) {
                return true;
            }
        }

        return this.filetable.ffree(ftEnt); //Frees memory originally allocated for the file
    }

    // FSize
    // Return the size of the File Table entry passed in in bytes
    int fsize(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            return ftEnt.inode.getLength();
        }
    }


    // Read
    // Start at the seek pointer and start reading from fileEnt
    // and increment the seek pointer
    // Return the number of bytes read
    int read(FileTableEntry fileEnt, byte[] buffer) {
        if (fileEnt.mode != "w" && fileEnt.mode != "a") {   //If mode is write or append it shouldn't be calling this method
            int numBytes = 0;
            int bufferSize = buffer.length;
            synchronized (fileEnt) {    //Synchronize the file
                int tempSize = this.fsize(fileEnt);
                while (bufferSize > 0 && fileEnt.seekPtr < this.fsize(fileEnt)) {   //While the buffer isn't empty
                    int targetBlock = fileEnt.inode.findTargetBlock(fileEnt.seekPtr);
                    if (targetBlock == -1) {
                        break;
                    }

                    byte[] block = new byte[512];
                    SysLib.rawread(targetBlock, block); //read into the block
                    int ptr = fileEnt.seekPtr % 512;
                    int offset = 512 - ptr;
                    int selection = this.fsize(fileEnt) - fileEnt.seekPtr;
                    int min = Math.min(Math.min(offset, bufferSize), selection);
                    System.arraycopy(block, ptr, buffer, numBytes, min);
                    fileEnt.seekPtr += min;
                    numBytes += min;
                    bufferSize -= min;
                }

                return numBytes;
            }
        } else {
            return -1;
        }
    }

    //Write
    //Writes data from the buffer to the file entry starting from the
    //seek pointer and then writes to the disk
    int write(FileTableEntry fileEnt, byte[] buffer) {
        if (fileEnt == null || fileEnt.mode == "r") {   //Checks if the mode is read which doesn't have permissions to write
            return -1;
        } else {
            synchronized (fileEnt) {   //synchronizes the file so other threads don't interfere
                int count = 0;
                int length = buffer.length;

                while (length > 0) {    //While the buffer hasn't finished writing
                    int targetBlock = fileEnt.inode.findTargetBlock(fileEnt.seekPtr);
                    if (targetBlock == -1) {
                        short freeBlock = (short) this.superblock.getFreeBlock();   //Grabs the first free block
                        switch (fileEnt.inode.registerTargetBlock(fileEnt.seekPtr, freeBlock)) {    //Checks for a specific error
                            case -3:
                                short freeBlock1 = (short) this.superblock.getFreeBlock();
                                if (!fileEnt.inode.registerIndexBlock(freeBlock1)) {
                                    SysLib.cerr("ThreadOS: panic on write\n");
                                    return -1;
                                }

                                if (fileEnt.inode.registerTargetBlock(fileEnt.seekPtr, freeBlock) != 0) {
                                    SysLib.cerr("ThreadOS: panic on write\n");
                                    return -1;
                                }
                            case 0:
                            default:
                                targetBlock = freeBlock;
                                break;
                            case -2:
                            case -1:
                                SysLib.cerr("ThreadOS: filesystem panic on write\n");
                                return -1;
                        }
                    }

                    byte[] otherBuffer = new byte[512]; //Create a new buffer
                    if (SysLib.rawread(targetBlock, otherBuffer) == -1) {   //read into the buffer
                        System.exit(2);
                    }

                    int ptr = fileEnt.seekPtr % 512;
                    int offset = 512 - ptr; //Calculate the offset
                    int min = Math.min(offset, length);
                    System.arraycopy(buffer, count, otherBuffer, ptr, min);
                    SysLib.rawwrite(targetBlock, otherBuffer);  //from the disk to the buffer
                    fileEnt.seekPtr += min; //Increment seek pointer
                    count += min;
                    length -= min;
                    if (fileEnt.seekPtr > fileEnt.inode.length){    //Makes sure length is the min of seekPtr and Length
                        fileEnt.inode.length = fileEnt.seekPtr;
                    }
                }

                fileEnt.inode.toDisk(fileEnt.iNumber);  //Writes the contents to the disk
                return count;
            }
        }
    }

    //Deallocates all of the memory blocks of the file table Entry
    private boolean deallocAllBlocks(FileTableEntry ftEnt) {
        if (ftEnt.inode.count != 1) {   // If the file is closed or there are multiple references don't deallocate
            return false;
        } else {
            byte[] buffer = ftEnt.inode.unregisterIndexBlock(); //Fill the buffer with index block and unregister
            if (buffer != null) {
                byte tempByte = 0;

                short temp;
                while ((temp = SysLib.bytes2short(buffer, tempByte)) != -1) {
                    this.superblock.returnBlock(temp);  //Return block
                }
            }

            int counter = 0;

            while (true) {  //Infinite loop
                Inode thisiNode = ftEnt.inode;
                if (counter >= 11) {
                    thisiNode.toDisk(ftEnt.iNumber);
                    return true;    //returns out of the counter is greater than 11
                                    //means that all of the direct blocks have been written to disk
                }

                if (thisiNode.direct[counter] != -1) {
                    this.superblock.returnBlock(thisiNode.direct[counter]);
                    thisiNode.direct[counter] = -1;
                }

                ++counter;
            }
        }
    }

    //Deletes a file table entry from the file system
    boolean delete(String fileName) {
        FileTableEntry ftEnt = this.open(fileName, "r");    //Opens it with read because it's the lowest permission
        ftEnt.inode.flag = 2;   //Sets the flag to 2 so filesystem doesn't accientally leave remnants of the file
        short iNum = ftEnt.iNumber;
        return this.close(ftEnt) && this.directory.ifree(iNum); //Closes and then frees the file
    }

    //Seek recalculates the seek pointer so that it can append to files instead of
    //overwriting them
    int seek(FileTableEntry ftEnt, int offsest, int whence) {
        synchronized(ftEnt) {   //Synchronize the file
            switch(whence) {
                case 0:
                    if(offsest >= 0 && offsest <= this.fsize(ftEnt)) {  //If end of file is reached then error, otherwise return offset
                        ftEnt.seekPtr = offsest;
                        break;
                    }

                    return -1;
                case 1:
                    if(ftEnt.seekPtr + offsest >= 0 && ftEnt.seekPtr + offsest <= this.fsize(ftEnt)) {  //If end of file is reached then error, otherwise return offset
                        ftEnt.seekPtr += offsest;
                        break;
                    }

                    return -1;
                case 2:
                    if(this.fsize(ftEnt) + offsest < 0 || this.fsize(ftEnt) + offsest > this.fsize(ftEnt)) {    //If end of file is reached then error, otherwise return offset
                        return -1;
                    }

                    ftEnt.seekPtr = this.fsize(ftEnt) + offsest;
            }

            return ftEnt.seekPtr;   //Return the seek pointer when done
        }
    }
}
