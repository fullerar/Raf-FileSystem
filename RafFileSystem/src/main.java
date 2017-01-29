import java.io.IOException;


public class main
{
  public static void main(String[] args) throws IOException
  {
    util u = new util();
    file f = new file();
    dir d = new dir();
    
    /* Format Example */
    int formatCheck = u.Format("/Users/andrew/Desktop/partition.out");
    //also creates root with Dir_Create, see other example below
    System.out.println("Format check: " + formatCheck);
    
    /* Mount Example */
    int mountCheck = u.Mount("/Users/andrew/Desktop/partition.out");
    System.out.println("Mount check: " + mountCheck);  
    
    /* File (root) Open Example */
    int rootFD = f.File_Open("/");
    System.out.println("File / open check: " + rootFD);
    
    /* File Create Example */
    int fileCreateCheck1 = f.File_Create("/a");
    System.out.println("File /a create check:" + fileCreateCheck1);
    
    /* File Create Example */
    int fileCreateCheck2 = f.File_Create("/b");
    System.out.println("File /b create check: " + fileCreateCheck2);
    
    /* File Open Example */
    int fileDfd = f.File_Open("/b");
    System.out.println("File /b open check: " + fileDfd);
    
    /* File Write Example */
    byte[] t = new byte[4];
    t[0] = 'K';
    t[1] = 'I';
    t[2] = 'N';
    t[3] = 'G';
    int ret2 = f.File_Write(fileDfd, t, t.length);
    System.out.println("File /b write check: " + ret2);

    /*File Write Example */
    byte[] b = new byte[6];
    b[0] = ' ';
    b[1] = 'E';
    b[2] = 'L';
    b[3] = 'V';
    b[4] = 'I';
    b[5] = 'S';
    int ret = f.File_Write(fileDfd, b, b.length);
    System.out.println("File /b write check: " + ret);
    
    /* Dir Create Example */
    int dirCreate1 = d.Dir_Create("/c");
    System.out.println("Dir /c create check: " + dirCreate1);
    
    /*Invalid File Create Example */
    int invCreate = f.File_Create("/c/d");
    System.out.println("Invalid file /c/d create check: " + invCreate);
    
    /* File (dir /c) open example */
    int fileCfd = f.File_Open("/c");
    System.out.println("Dir /c open check: " + fileCfd);

    /*Valid File Create Example */
    int vCreate = f.File_Create("/c/d");
    System.out.println("Valid file /c/d create check: " + vCreate);

    /*Invalid File Create Example 2 */
    int invCreate2 = f.File_Create("/c/d");
    System.out.println("Invalid file /c/d create check: " + invCreate2);

    /*File Create Example */
    f.File_Create("/c/e");

    /*File Open Example */
    int fileCEfd = f.File_Open("/c/e");
    System.out.println("File /c/e open check: " + fileCEfd);
    
    /*File Write Example */
    byte[] be = new byte[5];
    be[0] = 'D';
    be[1] = 'U';
    be[2] = 'K';
    be[3] = 'E';
    be[4] = 'S';
    int r = f.File_Write(fileCEfd, be, be.length);
    System.out.println("File /c/e write check: " + r);
    
    /* Dir Create Example */
    int dirCreateCheck = d.Dir_Create("/c/f");
    System.out.println("Dir /c/f create check: " + dirCreateCheck);
    
    /* File (dir /c/f) Open Example */
    int fileCFfd = f.File_Open("/c/f");
    System.out.println("Dir /c/f open check: " + fileCFfd);
    
    /* File Create Example */
    int fileCFG = f.File_Create("/c/f/g");
    System.out.println("File /c/f/g create check: " + fileCFG);
    
    /* File Open Example */
    int fileCFGfd = f.File_Open("/c/f/g");
    System.out.println("File /c/f/g open check " + fileCFGfd);
    
    /* File Write Example */
    byte[] bee = new byte[6];
    bee[0] = 'B';
    bee[1] = 'a';
    bee[2] = 'T';
    bee[3] = 'm';
    bee[4] = 'A';
    bee[5] = 'n';
    int q = f.File_Write(fileCFGfd, bee, bee.length);
    System.out.println("File /c/f/g write check: " + q);
    
    /* File Read Example */
    byte[] fr = new byte[20];
    int fileRead = f.File_Read(fileCFGfd, fr, fr.length);
    System.out.println("File /c/f/g read check: " + fileRead);
    
    /*Directory Size Example */
    int size = d.Dir_Size("/c");
    System.out.println("Dir /c size check: " + size);
    
    /* Directory Read Example */
    byte[] buf = new byte[512];
    int read = d.Dir_Read("/c", buf, size);
    System.out.println("Dir read /c size: " + read);
    
    /*Directory Size Example */
    int size2 = d.Dir_Size("/c/f");
    System.out.println("Dir /c/f size check: " + size2);
    
    /* Directory Read Example */
    byte[] buf2 = new byte[512];
    int read2 = d.Dir_Read("/c/f", buf2, size2);
    System.out.println("Dir read /c/f size: " + read2);
    
    /* File Seek Example */
    System.out.println("File /c/f/g seek current file pointer check: " + util.openFileTable.get(fileCFGfd).getFilePointer());
    int newFilePointer = f.File_Seek(fileCFGfd, 20);
    System.out.println("File /c/f/g seek new file pointer: " + newFilePointer);
    
    /* File Close Example */
    int fileClose = f.File_Close(fileCFGfd);
    System.out.println("File /c/f/g close check: " + fileClose);
    
    /* Invalid File Close Example */
    int invFileClose = f.File_Close(100); //uses a file descriptor that does not exist (so not open)
    System.out.println("Invlaid file close with invalid file descriptor " + 100 + " check: " + invFileClose);

    /* File Open Example */
    int fileAfd = f.File_Open("/a");
    System.out.println("File /a open check: " + fileAfd);
    
    /* File Write Example */
    byte[] al = new byte[15];
    al[0] = 'B';
    al[1] = 'a';
    al[2] = 'T';
    al[3] = 'm';
    al[4] = 'A';
    al[5] = 'n';
    al[6] = ' ';
    al[7] = 'W';
    al[8] = 'a';
    al[9] = 'S';
    al[10] = ' ';
    al[11] = 'h';
    al[12] = 'E';
    al[13] = 'r';
    al[14] = 'E';
    int all = f.File_Write(fileAfd, al, al.length);
    System.out.println("File /b write check: " + all);
    
    
//    /* File Unlink Example */
//    int un2 = f.File_Unlink("/c/f/g");
//    System.out.println("File unlink: " + un2);
    
    /* Dir Create Example */
    int dirCreate3 = d.Dir_Create("/c/f/h");
    System.out.println("Dir /c/f/h create check: " + dirCreate3);
    
    /* Invalid Dir Unlink Example */
    int invDirUn = d.Dir_Unlink("/c");
    System.out.println("Invalid dir /c unlink check: " + invDirUn);
    
//    /* Valid Dir Unlink Example */
//    int vDirUn = d.Dir_Unlink("/c/f/h");
//    System.out.println("Valid dir /c/f/h unlink check: " + vDirUn);
    

  }
}
