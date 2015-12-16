
public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    public FileSystem(int diskSize) {
        this.superblock = new SuperBlock(diskSize);
        this.directory = new Directory(this.superblock.inodeBlocks);
        this.filetable = new FileTable(this.directory);
        FileTableEntry ftEnt = this.open("/", "r");
        int ftEntSize = this.fsize(ftEnt);
        if(ftEntSize > 0) {
            byte[] buffer = new byte[ftEntSize];
            this.read(ftEnt, buffer);
            this.directory.bytes2directory(buffer);
        }

        this.close(ftEnt);
    }

    void sync() {
        FileTableEntry ftEnt = this.open("/", "w");
        byte[] buffer = this.directory.directory2bytes();
        this.write(ftEnt, buffer);
        this.close(ftEnt);
        this.superblock.sync();
    }

    boolean format(int diskSize) {
        while(!this.filetable.fempty()) {
            ;
        }

        this.superblock.format(diskSize);
        this.directory = new Directory(this.superblock.inodeBlocks);
        this.filetable = new FileTable(this.directory);
        return true;
    }

    FileTableEntry open(String fileName, String mode) {
        FileTableEntry ftEnt = this.filetable.falloc(fileName, mode);
        return mode == "w" && !this.deallocAllBlocks(ftEnt)?null:ftEnt;
    }

    boolean close(FileTableEntry ftEnt) {
        synchronized(ftEnt) {
            --ftEnt.count;
            if(ftEnt.count > 0) {
                return true;
            }
        }

        return this.filetable.ffree(ftEnt);
    }

    int fsize(FileTableEntry ftEnt) {
        synchronized(ftEnt) {
            return ftEnt.inode.getLength();
        }
    }

    int read(FileTableEntry fileEnt, byte[] buffer) {
        if(fileEnt.mode != "w" && fileEnt.mode != "a") {
            int numBytes = 0;
            int bufferSize = buffer.length;
            synchronized(fileEnt) {
               int tempSize = this.fsize(fileEnt);
                while(bufferSize > 0 && fileEnt.seekPtr < this.fsize(fileEnt)) {
                    int targetBlock = fileEnt.inode.findTargetBlock(fileEnt.seekPtr);
                    if(targetBlock == -1) {
                        break;
                    }

                    byte[] block = new byte[512];
                    SysLib.rawread(targetBlock, block);
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

    int write(FileTableEntry fileEnt, byte[] buffer) {
        if(fileEnt == null || fileEnt.mode == "r") {
            return -1;
        } else {
            synchronized(fileEnt) {
                int count = 0;
                int length = buffer.length;

                while(length > 0) {
                    int targetBlock = fileEnt.inode.findTargetBlock(fileEnt.seekPtr);
                    if(targetBlock == -1) {
                        short freeBlock = (short)this.superblock.getFreeBlock();
                        switch(fileEnt.inode.registerTargetBlock(fileEnt.seekPtr, freeBlock)) {
                            case -3:
                                short freeBlock1 = (short)this.superblock.getFreeBlock();
                                if(!fileEnt.inode.registerIndexBlock(freeBlock1)) {
                                    SysLib.cerr("ThreadOS: panic on write\n");
                                    return -1;
                                }

                                if(fileEnt.inode.registerTargetBlock(fileEnt.seekPtr, freeBlock) != 0) {
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

                    byte[] otherBuffer = new byte[512];
                    if(SysLib.rawread(targetBlock, otherBuffer) == -1) {
                        System.exit(2);
                    }

                    int ptr = fileEnt.seekPtr % 512;
                    int offset = 512 - ptr;
                    int min = Math.min(offset, length);
                    System.arraycopy(buffer, count, otherBuffer, ptr, min);
                    SysLib.rawwrite(targetBlock, otherBuffer);
                    fileEnt.seekPtr += min;
                    count += min;
                    length -= min;
                    if(fileEnt.seekPtr > fileEnt.inode.length) {
                        fileEnt.inode.length = fileEnt.seekPtr;
                    }
                }

                fileEnt.inode.toDisk(fileEnt.iNumber);
                return count;
            }
        }
    }

    private boolean deallocAllBlocks(FileTableEntry ftEnt) {
        if(ftEnt.inode.count != 1) {
            return false;
        } else {
            byte[] buffer = ftEnt.inode.unregisterIndexBlock();
            if(buffer != null) {
                byte var3 = 0;

                short temp;
                while((temp = SysLib.bytes2short(buffer, var3)) != -1) {
                    this.superblock.returnBlock(temp);
                }
            }

            int counter = 0;

            while(true) {
                Inode thisiNode = ftEnt.inode;
                if(counter >= 11) {
                    thisiNode.toDisk(ftEnt.iNumber);
                    return true;
                }

                if(thisiNode.direct[counter] != -1) {
                    this.superblock.returnBlock(thisiNode.direct[counter]);
                    thisiNode.direct[counter] = -1;
                }

                ++counter;
            }
        }
    }

    boolean delete(String fileName) {
        FileTableEntry ftEnt = this.open(fileName, "r");
        ftEnt.inode.flag = 2;
        short iNum = ftEnt.iNumber;
        return this.close(ftEnt) && this.directory.ifree(iNum);
    }

    int seek(FileTableEntry ftEnt, int offsest, int whence) {
        synchronized(ftEnt) {
            switch(whence) {
                case 0:
                    if(offsest >= 0 && offsest <= this.fsize(ftEnt)) {
                        ftEnt.seekPtr = offsest;
                        break;
                    }

                    return -1;
                case 1:
                    if(ftEnt.seekPtr + offsest >= 0 && ftEnt.seekPtr + offsest <= this.fsize(ftEnt)) {
                        ftEnt.seekPtr += offsest;
                        break;
                    }

                    return -1;
                case 2:
                    if(this.fsize(ftEnt) + offsest < 0 || this.fsize(ftEnt) + offsest > this.fsize(ftEnt)) {
                        return -1;
                    }

                    ftEnt.seekPtr = this.fsize(ftEnt) + offsest;
            }

            return ftEnt.seekPtr;
        }
    }
}
