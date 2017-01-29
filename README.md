# Raf-FileSystem
This simulates the inner workings of a file system by creating one inside of a random access file.

The credit for the following summary goes to Dr. Brett Tjaden of James Madison Univeristy.
https://users.cs.jmu.edu/tjadenbc/Web/

Your file system’s “partition”:

- The entire “partition” that holds your file system will just be a regular binary file (of size 131,072 bytes) stored on stu. Define the following constants in your program:
- SECTOR_SIZE = 512 
- NUM_SECTORS = 256

Your “partition” contains NUM_SECTORS sectors and each sector contains SECTOR_SIZE bytes. The first sector in your partition should be the superblock. The format of the superblock is as follows:
  • A 4-byte “magic number” – you may choose any 4-byte value you like to be your magic number.
  • A 4-byte number representing the size (in sectors) of the superblock – this should be 1.
  • A 4-byte number representing the number of sectors that contain inodes – this should be 9.
  • A 4-byte number representing the number of sectors that contain data blocks – this should be 246.
  • A 144-byte array telling which inodes are free – initially all 144 bytes should be zero since all inodes are free.
  • A 246-byte array telling which data blocks are free – initially all 246 bytes should be zero since all data blocks are free.

Inodes:
- There will be one inode for each file and directory. Inodes are 32 bytes long, and they contain:
  • A 2-byte number representing whether or not the inode is free (0x0000) or in use (0x1111)
  • A 2-byte number representing whether the inode belongs to a file (0x2222) or a directory (0x3333)
  • A 2-byte number representing the file or directory’s size in bytes
  • Twenty-six 1-byte numbers indexing the data block(s) containing the file or directory’s data

- Each inode block (512 bytes) can hold 16 inodes (32 bytes), and since there are nine inode blocks your file system can have at maximum of 144 inodes (and therefore a maximum of 144 total files and directories). Also note that since a file/directory can have at most 26 (512 bytes) data blocks, no file or directory can exceed 13KB in size.
