package network.datahop.localsharing.ui.fragments.detail;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import network.datahop.localfirst.data.Content;
import network.datahop.localsharing.R;

public class CompletingViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "DataViewHolder";
    List<Content> ct;
    final Context context;
    ProgressBar bar;
    private final TextView textView,completedView;


    public CompletingViewHolder(View v, List<Content> content, Context ctx) {
        super(v);
        this.context = ctx;
        ct = content;
        textView = v.findViewById(R.id.itemVideoTitleView);
        bar = v.findViewById(R.id.progressbar);
        bar.setIndeterminate(false);
        completedView = v.findViewById(R.id.itemAdditionalDetails);
    }

    public TextView getTextView() {
        return textView;
    }

    public ProgressBar getBar(){return bar;}

    public TextView getCompleted() { return completedView;}

    public void setCompleted() {
        completedView.setText("Processing file...");
        //bar.setProgress(0);
        //bar.setIndeterminate(true);
        Log.d("ViewHolder","set completed "+bar.getProgress()+" "+bar.isIndeterminate());

    }

}
