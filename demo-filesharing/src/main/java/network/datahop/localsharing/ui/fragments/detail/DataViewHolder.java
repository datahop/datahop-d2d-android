package network.datahop.localsharing.ui.fragments.detail;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.util.List;

import network.datahop.localfirst.data.Content;
import network.datahop.localfirst.data.ContentDatabaseHandler;

import network.datahop.localsharing.R;
import network.datahop.localsharing.utils.Config;
import network.datahop.localsharing.utils.G;



public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

    private static final String TAG = "DataViewHolder";
    private final TextView textView;
    private final ImageView imageView,deleteImageView;
    List<Content> ct;
    final Context context;

    private final String group;
    private static DataViewHolder.ClickListener clickListener;

    public interface Callbacks {
        void onNewContent();
    }

    public interface ClickListener {
        void onItemClick(int position, View v, Content ct);
        void onItemLongClick(int position, View v, Content ct);
    }

    public DataViewHolder(View v, List<Content> content, Context ctx,Callbacks listener, String group) {
        super(v);
        this.group = group;
        this.context = ctx;
        ct=content;
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        textView = v.findViewById(R.id.itemVideoTitleView);
        imageView = v.findViewById(R.id.itemThumbnailView);
        deleteImageView = v.findViewById(R.id.itemDelete);

       // G.Log(TAG,"Adapter "+getAdapterPosition());
        //
        //showImage(imgFile,this);

        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                G.Log(TAG,"On click delete");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Erase "+ct.get(getAdapterPosition()).getUri()+"?");

                // Set up the buttons
                builder.setMessage(R.string.action_delete_item);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(ct.get(getAdapterPosition()).getUri(),context);
                        listener.onNewContent();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    public TextView getTextView() {
        return textView;
    }

    private ImageView getImageView() {
        return imageView;
    }

    @Override
    public void onClick(View v) {
        clickListener.onItemClick(getAdapterPosition(), v,ct.get(getAdapterPosition()));
    }

    @Override
    public boolean onLongClick(View v) {
        clickListener.onItemLongClick(getAdapterPosition(), v,ct.get(getAdapterPosition()));
        return false;
    }


    public void setOnItemClickListener(DataViewHolder.ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void showImage(String name){

        String yourFilePath = context.getExternalFilesDir(Config.FOLDER)+ "/" + name;

        File imgFile = new File(yourFilePath);
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(fileExt(imgFile.getAbsolutePath()));
        G.Log(TAG,"Type "+mimeType);

        Bitmap bMap;
        if(mimeType!=null) {
            if (mimeType.contains("video")) {
                bMap = ThumbnailUtils.createVideoThumbnail(imgFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                this.getImageView().setImageBitmap(bMap);
            } else if (mimeType.contains("image")) {
                //bMap = ThumbnailUtils.createVideoThumbnail(imgFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                //this.getImageView().setImageBitmap(bMap);
                bMap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                this.getImageView().setImageBitmap(bMap);
                this.getImageView().setRotation(0);
            } else if (mimeType.contains("word"))
                this.getImageView().setImageDrawable(context.getResources().getDrawable(R.drawable.ic_word));
            else if (mimeType.contains("power") || mimeType.contains("presentation"))
                this.getImageView().setImageDrawable(context.getResources().getDrawable(R.drawable.ic_powerpoint));
            else if (mimeType.contains("excel") || mimeType.contains("spreadsheet"))
                this.getImageView().setImageDrawable(context.getResources().getDrawable(R.drawable.ic_excel));
            else if (mimeType.contains("pdf"))
                this.getImageView().setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pdf));
            else
                this.getImageView().setImageDrawable(context.getResources().getDrawable(R.drawable.dummy_thumbnail));
        } else {
            this.getImageView().setImageDrawable(context.getResources().getDrawable(R.drawable.dummy_thumbnail));
        }
    }


    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private void deleteItem(String fname, Context context){
        G.Log(TAG,"Delete "+fname+" "+group);
        File dir = context.getExternalFilesDir(Config.FOLDER);
        File[] subFiles = dir.listFiles();
        ContentDatabaseHandler db = new ContentDatabaseHandler(context);
        List<Content> content = db.getContent(fname,group);
        //G.Log(TAG,"Files " +subFiles.length+" "+content.size());
        for (Content cn : content) {
            // Writing Contacts to log
            String name = fname;
            if(cn.getId()>0)name+="."+cn.getId();
            //G.Log(TAG,"Name:"+ name+ " desc:" + cn.getText() + " url:"+cn.getUrl());
            if (subFiles != null) {
                //G.Log("Files " +subFiles);
                for (File file : subFiles) {
                    // G.Log("Filename " + cn.getName() + " " +file.getAbsolutePath()+" "+file.getName()+" "+file.length());
                    if (file.getName().equals(name)||file.getName().equals(fname)) file.delete();
                }
            }
            db.rmContent(cn.getUri(),group);

        }
        //Intent broadcast = new Intent(CLEAR_VIDEOS);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

}