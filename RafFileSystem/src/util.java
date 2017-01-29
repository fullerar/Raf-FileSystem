import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class util
{
  
  /*
  
  0-511       super block sector
  512-5119    9 iNode sectors
  5120-131072 160 dataBlock sectors
  
  */
  
  
  public static List<Inode>  openFileTableList = new ArrayList<Inode>();
  
  public static HashMap<Integer, Inode> openFileTable = new HashMap<Integer, Inode>();
  
  public static List<Inode> iNodeList = new ArrayList<Inode>();

  public static final int iNodeMax = 160;
  public static final int iNodeBlockMax = 10;
  public static final int SECTOR_SIZE  = 512;
  public static final int NUM_SECOTORS = 256;
  public static final int maxBytes = SECTOR_SIZE * NUM_SECOTORS;
  
  public static int iNodeCount = 0;
  public static int dataBlockCount = 0;
  public static int usedBytes = 0;

  public static RandomAccessFile raf;
  
  dir dir = new dir(); // for creating the root directory in format method

  
  /**
   * This will be the first method called in the main class. It creates the initial 
   * random access file and formats the super block at specific bytes. 
   * 
   * 
   * @param file - name of file that contains the partition
   * @return - 0 on success, -1 on failure
   * @throws IOException
   */
  public int Format(String file) throws IOException
  {
    int result;

    if(!file.equals(null))
    {
      raf = new RandomAccessFile(file, "rw");
      raf.setLength(maxBytes);
      System.out.println("Raf created.");

      raf.seek(0);

      //Magic number
      raf.writeInt(11); //0-3
      usedBytes += 4;

      //Size of super block
      raf.writeInt(1); //4-7
      usedBytes += 4;

      //number of sectors that contain inodes
      raf.writeInt(9); //8-11
      usedBytes += 4;

      //number of sectors that contain data blocks
      raf.writeInt(246); //12-15
      usedBytes += 4;

      //array telling which inodes are free
      byte[] iFree = new byte[144];
      for(int i = 0; i < iFree.length; i++)
      {
        iFree[i] = 0;
      }
      raf.write(iFree); //16-159
      usedBytes += 10;


      //array telling which data blocks are free
      raf.write(-1);
      byte[] bFree = new byte[245];
      for(int i = 0; i < bFree.length; i++)
      {
        bFree[i] = 0;
      }
      raf.write(bFree); //160-404

      //fill remaining bytes of super block sector with 0's
      byte[] filler = new byte[106];
      for(int i = 0; i < filler.length; i++)
      {
        filler[i] = 0;
      }
      raf.write(filler); //405-511
      
      dir.Dir_Create("/");
      
      result = 0;
    }
    else
    {
      result = 1;
      //System.out.println("Raf not created.");
    }
    return result;
  }

  
  /**
   * 
   * @param file - name of file that contains the partition
   * @return - -1 if the file does not exist or the magic number is wrong, otherwise 0
   * @throws IOException
   */
  public int Mount(String file) throws IOException
  {
    int result = 0;
    
    File f = new File(file);
    
    if(f.exists())
    {
      result = 0;
    }
    else
    {
      System.out.println("File partion does not exist.");
      result = -1;
    }
    
    
    byte[] buffer = new byte[4];
    InputStream is = new FileInputStream(file);
    is.read(buffer, 0, 4);
    
    if(buffer[3] == 11)
    {
      System.out.println("File " + file + " successfully mounted.");
      result = 0;
    }
    else
    {
      System.out.println("Invalid magic number.");
      result = -1;
    }
    
    return result;
  }
  
  
  
  /**
   * Currently not used. Created as a helper method to find free data sectors.
   * 
   * 
   * @return - sector location of next free data block
   * @throws IOException
   */
  public static int findFreeDataBlock() throws IOException
  {
    int result = 0;
    int temp = 0;
    
    byte[] b = new byte[246];
    raf.seek(160);
    raf.read(b, 0, b.length);
    
    temp = 0;
    for(int i = 0; i < b.length; i++)
    {
      if(b[i] == -1)
      {
        temp = i;
      }
    }
    
    result = 5120 + (512 * temp);

    return result;
  }
  
}
