import java.io.IOException;

public class dir
{
  
  /**
   * Creates a new directory file, a corresponding inode, and updates the super block.
   * 
   * @param directory - string representing the directory pathname
   * @return - -1 on failure, 0 on success
   * @throws IOException
   */
  public int Dir_Create(String directory) throws IOException
  {
    int result = 0;
    
    String[] temp;
    String test;
    int sizeTemp = 0;
    
    if(util.iNodeList.size() > 1)
    {
      temp = directory.split("/");
      test = temp[temp.length-1];
      sizeTemp = test.length() + 1;
    }

    
    String tempString = directory.substring(0, directory.length() - sizeTemp);

    int loc = 0;
    
    boolean pathCheck = false;
    if(util.openFileTable.size() == 0 && directory.equals("/"))
    {
      pathCheck = true;
    }
    else if(util.openFileTableList.size() == 1)
    {
      Inode i = util.openFileTableList.get(0);
      loc = i.getDataStartPoint();
      pathCheck = true;
    }
    else
    {
      for(Inode i : util.openFileTableList)
      {
        if(i.getPath().equals(tempString))
        {
          loc = i.getDataStartPoint();
          pathCheck = true;
        }
        else
        {
          pathCheck = true;
        }
      }
    }
    
    if(pathCheck)
    {
      if(util.iNodeCount < 160)
      {
        Inode newDir = new Inode(directory, "dir", util.raf, util.iNodeCount);        
        if(newDir.getPath().length() > 16)
        {
          //System.out.println("Dir name " + directory + " is too long, did not create dir.");
          result = -1;
        }
        else
        {
          util.iNodeList.add(newDir);

          if(util.iNodeList.size() > 1)
          {
            //update parent directory
            byte[] be = new byte[512];
            util.raf.seek(loc);
            util.raf.read(be, 0, be.length);
            for(int i = 0; i < be.length; i += 20)
            {
              if(be[i] == -1)
              {
                util.raf.seek(loc + i);
                util.raf.writeBytes(newDir.getPath());
                util.raf.seek(loc + i + 16);
                util.raf.writeInt(newDir.getiNodeNumber());
                util.raf.writeByte(-1);
                break;
              }
            }
          }   
          
          int t = 0;
          
          util.raf.seek(160);
          byte[] b = new byte[246];
          util.raf.read(b, 0, b.length);
          for(int i = 0; i < b.length; i++)
          {
            if(b[i] == -1)
            {
              t = i;
              util.raf.seek(160 + i);
              util.raf.writeByte(3);
              util.raf.writeByte(-1);
              break;
            }
          }
          
          int l = 5120 + (t * 512);
          util.raf.seek(l);
          util.raf.writeByte(-1);
          
          
          //System.out.println("Dir: " + directory + " created.");
          result = 0;
          util.iNodeCount++;
        }   
      }
      else
      {
        System.out.println("Inode limit reached, did not create dir " + directory);
        result = -1;
      }
    }
    else
    {
      System.out.println("Incorrect path, did not create directory " + directory);
      result = -1;
    }
    return result;
  }

  /**
   * 
   * @param directory - string representing the directory pathname
   * @return - the size of the directory, -1 if the directory does not exist
   * @throws IOException
   */
  public byte Dir_Size(String directory) throws IOException
  {
    short result = 0;
    boolean exists = false;
    for(Inode i : util.iNodeList)
    {
      if(i.getPath() == directory)
      {
        exists = true;
        int num = i.getiNodeNumber();
        
        int loc = 512 + (num * 32);
        loc += 4;
        util.raf.seek(loc);
        result = util.raf.readShort();
      }
    }

    if(exists)
    {
      result = -1;
      //System.out.println("Dir " + directory + " does not exits, cannot get dir size.");
    } 
    result = (short) (result * 20);
    return (byte) result;
  }

  /**
   * Reads the specified directory, filling the buf with the corresponding entries. Each entry
   *  is 20 bytes. The first 16 are the directories and files inside the corresponding directory,
   *  followed by the 4 byte integer inode number.
   *   
   * @param directory - string representing the directory pathname
   * @param buf - the buffer to read the data into
   * @param bufferSize - the size of the buffer in 20 byte entries
   * @return - if bufferSize is not big enough to contain all of the entries, -1, else the number of
   *          directory entries that are in the directory
   * @throws IOException
   */
  public int Dir_Read(String directory, byte[] buf, int bufferSize) throws IOException
  {
    int result = bufferSize / 20;
    
    if(result == 0)
    {
      //System.out.println("Dir " + directory + " does not exisit, cannot read dir.");
      result = -1;
    }
    return result;
  }

  /**
   * Removes the corresponding directory, frees the related inode and data blocks, and removes
   *  its entry in the parents directory.
   * 
   * @param directory - string representing the directory pathname
   * @return - if there are files within the directory, -1, else 0 on success
   * @throws IOException
   */
  public int Dir_Unlink(String directory) throws IOException
  {
    int result = 0;
    
    for(Inode i : util.iNodeList)
    {
      if(i.getPath() == directory)
      {
        if(directory == "/")
        {
          //System.out.println("Connot unlink root directory.");
          result = -1;
        }
        
        if(i.getSize() != 0)
        {
          //System.out.println("Dir " + directory + " is not empty, cannot unlink dir.");
          result = -1;
          break;
        }
        
        int loc = i.getDataStartPoint();
        util.raf.seek(loc);
        byte[] b = new byte[512];
        for(int n = 0; n < b.length; n++)
        {
          b[n] = 0;
        }
        util.raf.write(b, 0, b.length);
        
        for(Inode x : util.iNodeList)
        {
          if(x.getPath().equals(directory))
          {
            int goTo = 512 + (32 * i.getiNodeNumber());
            
            util.raf.seek(goTo);
            byte[] be = new byte[32];
            for(int xx = 0; xx < b.length; xx++)
            {
              b[xx] = 0;
            }
            util.raf.write(be, 0, be.length);
          }
        }
      }
    }
    return result;
  }
}
