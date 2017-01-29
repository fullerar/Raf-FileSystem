import java.io.IOException;
import java.util.Set;


public class file
{

  

  
  /**
   * Creates a new file and corresponding Inode for it.
   * 
   * @param file - string representing the absolute pathname to the file
   * @return - if no inodes are available, something wrong with pathname, or already 
   *            a file/directory with the given name, return -1. Else return 0.
   * @throws IOException
   */
  public int File_Create(String file) throws IOException
  {
    int result = existsCheck(file);

    if(result != -1)
    {
      int loc = 0;
      boolean pathCheck = false;
      
      // find the parent directory
      if(util.openFileTableList.size() == 1)
      {
        Inode i = util.openFileTableList.get(0);
        loc = i.getDataStartPoint();      
        pathCheck = true;
      }
      else
      {
        for(Inode i : util.openFileTableList)
        {
          //System.out.println("**** " + i.getPath());
          String parent = getParent(file);
          if(i.getPath().equals(parent))
          {
            loc = i.getDataStartPoint();
            pathCheck = true;
            break;
          }
          else
          {
            result = -1;
            pathCheck = false;
          }
        }
      }



      if(pathCheck)
      {
        if(util.iNodeCount < 160)
        {
          // create new inode
          Inode newFile = new Inode(file, "file", util.raf, util.iNodeCount);
          
          // if file name is too long, do not allow it
          if(newFile.getPath().length() > (byte) 16)
          {
            //System.out.println("File name " + file + " is too long, did not create file.");
            result = -1;
          }
          else
          {
            util.iNodeList.add(newFile);

            //update parent directory
            byte[] be = new byte[512];
            util.raf.seek(loc);
            util.raf.read(be, 0, be.length);
            for(int i = 0; i < be.length; i += 20)
            {
              if(be[i] == -1)
              {
                util.raf.seek(loc + i);
                util.raf.writeChars(newFile.getPath());
                util.raf.seek(loc + i + 16);
                util.raf.writeInt(newFile.getiNodeNumber());
                util.raf.writeByte(-1);
                break;
              }
            }




            //update root inode array
            util.raf.seek(160);
            byte[] b = new byte[246];
            util.raf.read(b, 0, 246);
            for(int i = 0; i < b.length; i++)
            {
              if(b[i] == -1)
              {
                util.raf.seek(160 + i);
                util.raf.writeByte(2);
                util.raf.writeByte(-1);
                break;
              }
            }

            //System.out.println("File: " + file + " created.");
            result = 0;
            util.iNodeCount++; 



            //add 20 to parent size
            int parentFD = 0;
            Set<Integer> set = util.openFileTable.keySet();
            for(Integer i : set)
            {
              String p = util.openFileTable.get(i).getPath();
              String parent = getParent(file);
              if(p.equals(parent))
              {
                parentFD = i;
              }
            }

            int num = util.openFileTable.get(parentFD).getiNodeNumber();

            int goTo = 512 + (32 * num);
            goTo += 4;

            short add = (short) (0);
            util.openFileTable.get(parentFD).setSize((short) (util.openFileTable.get(parentFD).getSize() + 1));

            util.raf.seek(goTo);
            util.raf.writeShort(add);
            
            goTo += 2;
            byte[] t = new byte[20];
            util.raf.read(t, 0, t.length);
            for(int tt = 0; tt < t.length; tt++)
            {
              if(t[tt] == -1)
              {
                util.raf.seek(goTo + tt);
                int si = newFile.getDataStartPoint() / 512;
                util.raf.writeByte(si);
                util.raf.writeByte(-1);
                break;
              }
            }  
          } 
        }
        else
        {
          //System.out.println("Inode limit reached, did not create file " + file);
          result = -1;
        }
      }
      else
      {
        //System.out.println("Incorrect path, did not create file " + file);
      }
    }
    return result;
  }

  /**
   * Opens the corresponding file so the user can read or write to it.
   * 
   * @param file - string representing the pathname to the file
   * @return - if file does not exist return -1, else return a file descriptor number to
   *            use in the open file table.
   * @throws IOException
   */
  public int File_Open(String file) throws IOException
  {
    boolean exists = false;
    int result;

    Inode temp = null;

    for(Inode i : util.iNodeList)
    {
      if(i.getPath().equals(file))
      {
        util.openFileTableList.add(i);
        temp = i;
        exists = true;
      }
    }

    if(exists)
    {
      result = util.openFileTableList.size() - 1;
      util.openFileTable.put(result, temp);
      //System.out.println("File " + file + " opened.");
    }
    else
    {
      //System.out.println("File " + file + " does not exist.");
      result = -1;
    }
    return result;
  }

  /**
   * Reads from an open file. Note, a file must be in the open file table 
   *  for it to be read. All reads should begin at the file pointer indicated
   *  by the corresponding inode. File pointer should be updated after the read.
   * 
   * @param fd - file descriptor number of opened file
   * @param buf - pointer to a buffer which stores read bytes
   * @param bytes - the number of bytes to read
   * @return - if file is not open, -1, else the number of bytes read
   * @throws IOException
   */
  public int File_Read(int fd, byte[] buf, int bytes) throws IOException
  {
    int result = 0;

    Inode temp = util.openFileTable.get(fd);

    if(temp == null)
    {
      result = -1;
    }
    else
    {
      int num = temp.getDataStartPoint();

      byte[] b = new byte[bytes];
      util.raf.seek(num);
      util.raf.read(b, 0, bytes);

      util.openFileTable.get(fd).setFilePointer(util.openFileTable.get(fd).getFilePointer() + bytes);
      result = bytes;
    }
    return result;
  }

  /**
   * Writes to an open file. Note, a file must be in the open file table 
   *  for it to be written to. All writes should begin at the file pointer indicated
   *  by the corresponding inode. File pointer should be updated after the write.
   *  
   * @param fd - file descriptor number of opened file
   * @param buf - the bytes to be written
   * @param bytes - the number of bytes to write
   * @return - if file is not open, -1, else the number of bytes written
   * @throws IOException
   */
  public int File_Write(int fd, byte[] buf, int bytes) throws IOException
  {
    int result = 0;

    Inode temp = util.openFileTable.get(fd);

    if(temp == null)
    {
      //System.out.println("The file with file descriptor " + fd + " is not open. Cannot write to file.");
      result = -1;
    }
    else
    {
      util.raf.seek(temp.getDataStartPoint() + temp.getFilePointer());
      util.raf.write(buf, 0, bytes);
      util.openFileTable.get(fd).setFilePointer(temp.getFilePointer() + bytes);
      result = bytes;
      int num = util.openFileTable.get(fd).getiNodeNumber();

      int loc = 512 + (32 * num + 4);
      util.raf.seek(loc);
      short up = (short) util.openFileTable.get(fd).getFilePointer();
      util.raf.writeShort(up);

      String path = temp.getPath();

      String[] tem = path.split("/");
      String test = tem[tem.length-1];
      int sizeTemp = test.length() + 1;

      String tempString = path.substring(0, path.length() - sizeTemp);
      
      if(tempString.equals(""))
      {
        short j = 512 + 4;
        util.raf.seek(j);
        short m = util.raf.readShort();
        util.raf.seek(j);
        util.raf.writeShort(up + m);
      }
      
      for(Inode i : util.iNodeList)
      {
        if(i.getPath().equals(tempString))
        {
          int d = i.getiNodeNumber();
          int l = 512 + (32 * d + 4);
          util.raf.seek(l);
          short u = util.raf.readShort();
          util.raf.seek(l);
          util.raf.writeShort(up + u);
        }
      }
    }
    return result;
  }

  /**
   * Should update the file pointer (in the open file table) for the specified file.
   * 
   * @param fd - file descriptor number of opened file
   * @param offset - location from the start of a file
   * @return - if the offset is larger than the size of the file or negative, -1, else
   *            the new location of the file pointer
   */
  public int File_Seek(int fd, int offset)
  {
    int result = 0;

    Inode temp = util.openFileTable.get(fd);

    if(temp == null)
    {
      //System.out.println("The file with file descriptor " + fd + " is not open. Cannot seek through file.");
      result = -1;
    }
    else
    {
      util.openFileTable.get(fd).setFilePointer(offset);
      result = util.openFileTable.get(fd).getFilePointer();
    }

    return result;
  }

  /**
   * Removes the specified file from the open file table.
   * 
   * @param fd - file descriptor number of opened file
   * @return - if the file is not currently open, -1, else 0 on success
   * @throws IOException
   */
  public int File_Close(int fd) throws IOException
  {
    int result = 0;

    Inode temp = util.openFileTable.get(fd);

    if(temp == null)
    {
      //System.out.println("The file with file descriptor " + fd + " is not open. Cannot close the file.");
      result = -1;
    }
    else
    {
      util.openFileTable.remove(fd);
      result = 0;
    }
    return result;
  }

  /**
   * Removes a file from the directory it is in, and frees the corresponding inode and data blocks.
   * 
   * @param path - string representing a pathname to a file
   * @return - if the file does not exist, -1, else 0 on success
   * @throws IOException
   */
  public int File_Unlink(String path) throws IOException
  {
    int result = 0;
    boolean check = true;


    for(Inode i : util.iNodeList)
    {
      if(i.getPath().equals(path))
      {
        check = true;
        result = 0;
        break;
      }
      else
      {
        check = false;
        result = 1;
      }
    }

    if(result == 0)
    {
      Set<Integer> set = util.openFileTable.keySet();
      for(Integer i : set)
      {
        String p = util.openFileTable.get(i).getPath();
        if(p.equals(path))
        {
          result = -1;
          check = false;
          break;
        }
      }
    }

    if(check)
    {
      String tempString = getParent(path);

      for(Inode i : util.iNodeList)
      {        
        if(i.getPath().equals(path))
        {
          util.raf.seek(i.getDataStartPoint());
          byte[] b = new byte[512];
          for(int x = 0; x < b.length; x++)
          {
            b[x] = 0;
          }
          util.raf.write(b, 0, b.length);
          break;
        }
      }

      for(Inode i : util.iNodeList)
      {
        if(i.getPath().equals(path))
        {
          
          for(Inode n : util.iNodeList)
          {
            if(n.getPath().equals(tempString))
            {
              int nug = n.getiNodeNumber();
              int go = 512 + (nug * 32 + 7);
              util.raf.seek(go);
              byte[] b = new byte[20];
              util.raf.read(b, 0, b.length);
              for(int x = 0; x < b.length; x++)
              {
                int te = b[x] * 512;
                if(te == i.getDataStartPoint());
                {
                  byte[] e = new byte[20];
                  for(int p = 0; p < e.length; p++)
                  {
                    e[p] = 0;
                  }
                  
                  int o = x * 20;
                  
                  util.raf.seek(n.getDataStartPoint() + o);
                  util.raf.write(e, 0, e.length);
                }
              }
            }
          }
          int goTo = 512 + (32 * i.getiNodeNumber());

          util.raf.seek(goTo);
          byte[] b = new byte[32];
          for(int x = 0; x < b.length; x++)
          {
            b[x] = 0;
          }
          util.raf.write(b, 0, b.length);
          result = 0;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Helper method to check if file/directory already exists.
   * 
   * @param file - the pathname to check for
   * @return - 
   */
  public int existsCheck(String file)
  {
    int result = 0;
    
    for(Inode i : util.iNodeList)
    {
      if(i.getPath() == file)
      {
        //System.out.println("File " + file + " already exists, cannot create file.");
        result = -1;
        break;
      }
    }
    return result;
  }
  
  /**
   * Helper method to get the parents pathname.
   * 
   * @param file - the file name to be manipulated
   * @return - the parents pathname
   */
  public String getParent(String file)
  {
    String[] temp = file.split("/");
    String test = temp[temp.length-1];
    int sizeTemp = test.length() + 1;

    String tempString = file.substring(0, file.length() - sizeTemp);

    return tempString;
  }

  public static void main(String[]args) throws IOException
  {

  }

}
