import java.io.IOException;
import java.io.RandomAccessFile;


public class Inode
{
  
  private short freedom;
  private short type;
  private short size;
  private byte[] index;
  
  private String path;
  private String fileType;
  private int iNodeNumber;
  private int dataStartPoint;
  private int filePointer;
    
  /**
   * Creates a new Inode object whenever a new directory or file is made.
   * 
   * @param path - the absolute path of the file
   * @param fileType - directory or file
   * @param raf - the random access file object
   * @param iNodeNumber - 
   * @throws IOException
   */
  public Inode(String path, String fileType, RandomAccessFile raf, int iNodeNumber) throws IOException
  { 
    int goTo = 0;
    
    int t = util.iNodeCount;
    
    // find next free Inode slot in the super block
    if(t == 0)
    {
      goTo = 512;
    }
    else
    {
      goTo = 512 + (32 * t);
    }   
    
    raf.seek(goTo);
            
    setFilePointer(0);
    this.setiNodeNumber(iNodeNumber);
    this.setPath(path);
    this.setFileType(fileType);
    
    // set the start point for the data that relates to the Inode
    setDataStartPoint(5120 + (t * 512)); 
    
    // indicate that this Inode slot is now taken
    setFreedom((short) 1);
    raf.writeShort(getFreedom());

    // set this Inodes's file type
    if(fileType.equals("file"))
    {
      this.setType((short) 2);
      raf.writeShort(getType());
    }
    else if(fileType.equals("dir"))
    {
      this.setType((short) 3);
      raf.writeShort(getType());
    }
  
    // set initial size of Inode to 0
    setSize((short) 0);
    raf.writeShort(getSize());

    // set rest of Inode to 0's (because it is empty)
    setIndex(new byte[26]);
    for(int i = 0; i < index.length; i++)
    {
      if(i == 0)
      {
        index[i] = -1;
      }
      else
      {
        index[i] = (byte) 0;
      }
    }
    raf.write(getIndex());
    
    // indicate in the super block that the corresponding Inode slot is now taken
    util.raf.seek(16);
    byte[] b = new byte[144];
    util.raf.read(b, 0, b.length);
    for(int i = 0; i < b.length; i++)
    {
      if(b[i] == 0)
      {
        raf.seek(16 + i);
        raf.writeByte(1);
        break;
      }
    }
  }

  public static void main(String[] args)
  {

  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public String getFileType()
  {
    return fileType;
  }

  public void setFileType(String fileType)
  {
    this.fileType = fileType;
  }

  public short getFreedom()
  {
    return freedom;
  }

  public void setFreedom(short freedom)
  {
    this.freedom = freedom;
  }

  public short getType()
  {
    return type;
  }

  public void setType(short type)
  {
    this.type = type;
  }

  public byte[] getIndex()
  {
    return index;
  }

  public void setIndex(byte[] index)
  {
    this.index = index;
  }

  public short getSize()
  {
    return size;
  }

  public void setSize(short size)
  {
    this.size = size;
  }

  public int getiNodeNumber()
  {
    return iNodeNumber;
  }

  public void setiNodeNumber(int iNodeNumber)
  {
    this.iNodeNumber = iNodeNumber;
  }

  public int getDataStartPoint()
  {
    return dataStartPoint;
  }

  public void setDataStartPoint(int dataStartPoint)
  {
    this.dataStartPoint = dataStartPoint;
  }

  public int getFilePointer()
  {
    return filePointer;
  }

  public void setFilePointer(int filePointer)
  {
    this.filePointer = filePointer;
  }
}
