package network.datahop.localsharing.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import network.datahop.localfirst.LocalFirstSDK;
import network.datahop.localsharing.ui.GroupActivity;
import network.datahop.localsharing.utils.Config;
import network.datahop.localsharing.utils.G;
import network.datahop.localsharing.utils.PathUtil;

//import static network.datahop.localsharing.MainActivity.RESTART;

public class AddFile extends AsyncTask<Uri, Void, String> {

    public static final String TAG = "AddFile";

    Context context;
    ContentDatabaseHandler db;
    boolean finished = false;
    ProgressDialog mProgress;
    private static GroupActivity parent;
    String group;

    public AddFile(Context context, GroupActivity parent,String group)
    {
        this.context =  context;
        db = new ContentDatabaseHandler(context);
        //this.listener = listener;
        this.parent = parent;
        this.group = group;
    }

    @Override
    protected String doInBackground(Uri... params) {

        Uri path = params[0];
        parent.runOnUiThread(new Runnable() {
                                public void run() {
                                    mProgress = ProgressDialog.show(context, "", "Loading file..", true);
                                }
                            });
        return saveFile(path);
    }

    @Override
    protected void onPostExecute(String name) {

        G.Log(TAG, "Post " + name);
        parent.runOnUiThread(new Runnable() {
            public void run() {
                mProgress.dismiss();
            }
        });

        try {
            int total = db.getContent(name, group).get(0).getTotal();
            G.Log(TAG, "Total " + total);
            for (int i = 1; i <= total; i++) {
                G.Log(TAG, "Set content downloaded " + i + " " + total);
                db.setContentDownloaded(name, group, i);
                db.setContentJoined(name, group);
            }
            parent.refreshAdapter();

        } catch (Exception e){
            G.Log(TAG,"File not added correctly "+e);
        }

    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    private String saveFile(Uri path) {
        try {
            G.Log(TAG, "Uri " + path.toString() + " " + path.getLastPathSegment());
            String filename = "";
            if (path.toString().startsWith("content")) {
                filename = getFileName(path);
                G.Log(TAG, "Content " + filename);

            } else {
                String file = PathUtil.getPath(context, path);
                filename = file.substring(file.lastIndexOf("/") + 1);
                G.Log(TAG, "else " + filename);
            }
            G.Log(TAG, "Filename " + filename+" "+group);
            File mFile = new File(context.getExternalFilesDir(Config.FOLDER), filename);
            storeFile(path, mFile);
            LocalFirstSDK.addFile(context,filename, mFile.getPath(),group);
            /*G.Log(TAG, "Filename " + filename+" "+ Chunking.getTotalParts(mFile.getAbsolutePath())+" "+group);

            try {
                Chunking.split(mFile.getPath(), group, context);
            } catch (IOException f) {
                G.Log(TAG, "IO error " + f);
            }*/

            //Intent broadcast = new Intent(RESTART);
            //LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
            return mFile.getName();

        }catch (Exception e){
            G.Log("Exception "+e);

        }

        return null;

    }

    public String getFileName(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null, null);
        String displayName = "";
        try {

            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                G.Log(TAG, "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                G.Log(TAG, "Size: " + size);
            }
        } finally {
            cursor.close();
        }

        return displayName;
    }


    public void storeFile(Uri mName, File mFile) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(context.getContentResolver().openInputStream(mName));
            bos = new BufferedOutputStream(new FileOutputStream(mFile));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {

                if (bis != null) bis.close();
                if (bos != null) bos.close();
                finished = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
