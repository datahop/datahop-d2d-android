package network.datahop.localsharing.ui.fragments.detail;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import network.datahop.localfirst.data.Content;
import network.datahop.localfirst.data.ContentDatabaseHandler;
import network.datahop.localsharing.BuildConfig;
import network.datahop.localsharing.R;
import network.datahop.localsharing.utils.Config;
import network.datahop.localsharing.utils.G;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ContentAdapter";
    private List<Content> mDataSet;
    private Context context;

    private static final int COMPLETED = 0;
    private static final int UNCOMPLETED = 1;

    ContentDatabaseHandler db;
    private final String group;
    DataViewHolder.Callbacks listener;

    public ContentAdapter(List<Content> ct, Context context, DataViewHolder.Callbacks listener, String group) {
        this.context = context;
        mDataSet = new ArrayList<>();
        int downloadedChunks = 1;
        db = new ContentDatabaseHandler(context);
        mDataSet = filterData(ct);
        this.group = group;
        this.listener = listener;
        setHasStableIds(true);
    }

    /*public void setContent(List<Content> ct) {
        mDataSet = ct;
    }*/

    private List<Content> filterData(List<Content> ct)
    {
        String last="";
        List<Content> dataSet = new ArrayList<>();
        if (ct.size() > 0) {
            Collections.sort(ct, new Comparator<Content>() {
                @Override
                public int compare(final Content object1, final Content object2) {
                    return object1.getName().compareTo(object2.getName());
                }
            });
        }
        //G.Log(TAG,"new adapter "+ct.size());
        for(Content content : ct)
        {
            //G.Log(TAG,"Content "+content.getName());
            /*if(last.equals(content.getName()))downloadedChunks++;
            if(downloadedChunks==content.getTotal())
            {
                mDataSet.add(content);
                downloadedChunks = 1;
            }*/
            if(!last.equals(content.getName()))
            {
                dataSet.add(content);
            }
            last = content.getName();
        }

        return dataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v;

        switch(viewType) {
            case COMPLETED:
                // Inflate the first view type
                G.Log(TAG,"Layout completed");
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_stream_item, viewGroup, false);
                return new DataViewHolder(v,mDataSet,context,listener, group);

            case UNCOMPLETED:
                // inflate the second view type
                G.Log(TAG,"Layout");
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_progress, viewGroup, false);
                return new CompletingViewHolder(v,mDataSet,context);
        }


        G.Log(TAG,"Default Layout");
        v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_stream_item, viewGroup, false);


        return new DataViewHolder(v,mDataSet,context,listener,group);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder v, final int position) {
        G.Log(TAG, "Element " + position + " set " + mDataSet.get(position).getText() + " " + mDataSet.get(position).getName() + " " + mDataSet.get(position).getUrl());

        if(v instanceof DataViewHolder) {
            DataViewHolder viewHolder = (DataViewHolder) v;
            viewHolder.getTextView().setText(mDataSet.get(position).getName());
            viewHolder.showImage(mDataSet.get(position).getName());
            viewHolder.setOnItemClickListener(new DataViewHolder.ClickListener() {
                @Override
                public void onItemClick(int position, View v, Content ct) {
                    G.Log(TAG, "onItemClick position: " + position);

                    File f = new File(context.getExternalFilesDir(Config.FOLDER) + "/" + ct.getName());
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", f);
                    newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    newIntent.setData(uri);
                    try {
                        context.startActivity(newIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onItemLongClick(int position, View v, Content ct) {
                    G.Log(TAG, "onItemLongClick = " + ct.getName() + " " + position);
                }
            });
        } else {
            G.Log(TAG,"Received "+db.getReceived(mDataSet.get(position).getName(),group)+" "+mDataSet.get(position).getTotal());
            CompletingViewHolder viewHolder = (CompletingViewHolder) v;
            viewHolder.getTextView().setText(mDataSet.get(position).getName());
            viewHolder.getBar().setProgress(db.getReceived(mDataSet.get(position).getName(),group)*100/(mDataSet.get(position).getTotal()));
            viewHolder.getCompleted().setText(String.valueOf(db.getReceived(mDataSet.get(position).getName(),group))+"/"+String.valueOf(mDataSet.get(position).getTotal()));


            if(db.getReceived(mDataSet.get(position).getName(),group) == mDataSet.get(position).getTotal())
                viewHolder.setCompleted();

        }
        /*try {
            G.Log(TAG, "Imgfile " + imgFile.getAbsolutePath() + " " + imgFile.toURI() + " " + imgFile.getCanonicalPath());
        } catch (Exception e) {
        }*/

    }

    @Override
    public int getItemCount() {
        return mDataSet == null? 0: mDataSet.size();
    }


    @Override
    public int getItemViewType(int position) {
        Content content = mDataSet.get(position);
        G.Log(TAG,"Get item type "+db.getReceived(content.getName(),group)+" "+content.getTotal()+" "+db.isContentJoined(content.getName(),group));
        if(db.isContentJoined(content.getName(),group))
            return COMPLETED;
        else
            return UNCOMPLETED;
    }

    public  void refreshAdapter(List<Content> dataitems) {
        G.Log(TAG,"Refresh");
        mDataSet.clear();
        mDataSet.addAll(filterData(dataitems));
        notifyDataSetChanged();
        //notifyItemChanged(0);
    }


}
