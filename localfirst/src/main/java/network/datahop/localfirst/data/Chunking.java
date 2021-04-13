/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

public class Chunking {

    private static final long chunkSize = (long)(5 * 1024 * 1024);
    private static final String TAG="Chunking";

    public static boolean exists(String filename)
    {
        File file = new File(filename);
        return file.exists();
    }
    public static void split(String filename, String group, Context context) throws IOException
    {
        G.Log(TAG,"Splitting "+filename);
        // open the file
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));

        // get the file length
        File f = new File(filename);
        long fileSize = f.length();

        int total = (int)(fileSize / chunkSize);
        if(fileSize%chunkSize!=0)total++;
        ContentDatabaseHandler db = new ContentDatabaseHandler(context);
        // loop for each full chunk
        int subfile;
        for (subfile = 1; subfile <= fileSize / chunkSize; subfile++)
        {
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));

            // write the right amount of bytes
            for (int currentByte = 0; currentByte < chunkSize; currentByte++)
            {
                // load one byte from the input file and write it to the output file
                out.write(in.read());
            }

            // close the file
            out.close();

            String name = filename.substring(filename.lastIndexOf("/") + 1);
            G.Log(TAG,"Add content "+name+" "+subfile+" "+total+" "+group);
            Content c = new Content(name, name,"","",subfile,total,"",group);
            db.addContent(c);
            G.Log(TAG,"chunk "+name+" "+subfile+" "+total);
            db.setContentDownloaded(name,group,subfile);
        }

        // loop for the last chunk (which may be smaller than the chunk size)
        if (fileSize != chunkSize * (subfile))
        {
            G.Log(TAG,"chunk "+subfile);

            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));

            // write the rest of the file
            int b;
            while ((b = in.read()) != -1)
                out.write(b);

            // close the file
            out.close();

            String name = filename.substring(filename.lastIndexOf("/") + 1);
            G.Log(TAG,"Add content "+name+" "+subfile+" "+total+" "+group);
            Content c = new Content(name, name,"","",subfile,total,"",group);
            db.addContent(c);
            db.setContentDownloaded(name,group,subfile);
        }

        // close the file
        in.close();
    }

    public static void join(String name,Context context,String group) throws IOException
    {
        //int numberParts = getNumberParts(baseFilename);
        ContentDatabaseHandler db = new ContentDatabaseHandler(context);
        int numberParts = db.getReceived(name,group);
        String baseFilename = context.getExternalFilesDir(Config.FOLDER)+"/"+name;
        G.Log(TAG,"Join "+numberParts+" "+baseFilename+" "+db.getReceived(baseFilename,group));
        // now, assume that the files are correctly numbered in order (that some joker didn't delete any part)
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(baseFilename));
        for (int part = 1; part <= numberParts; part++)
        {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(baseFilename + "." + part));

            int b;
            while ( (b = in.read()) != -1 )
                out.write(b);

            in.close();
        }
        out.close();
    }

    public static int getTotalParts(String baseFilename) throws IOException{
        // get the file length
        G.Log(TAG,"gettotalparts "+baseFilename);
        File f = new File(baseFilename);
        long fileSize = f.length();

        int total = (int)(fileSize / chunkSize);
        if(fileSize%chunkSize!=0)total++;
        return total;
    }

    public static int getNumberParts(String baseFilename) throws IOException
    {
        // list all files in the same directory
        //G.Log(TAG,"Get number parts "+baseFilename);
        File directory = new File(baseFilename).getAbsoluteFile().getParentFile();
        final String justFilename = new File(baseFilename).getName();
        String[] matchingFiles = directory.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(justFilename) && name.substring(justFilename.length()).matches("^\\.\\d+$");
            }
        });
        return matchingFiles.length;
    }
}
