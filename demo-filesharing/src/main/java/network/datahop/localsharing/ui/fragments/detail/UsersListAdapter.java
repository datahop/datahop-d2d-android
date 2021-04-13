package network.datahop.localsharing.ui.fragments.detail;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import network.datahop.localsharing.R;
import network.datahop.localsharing.data.User;

/**
 * Created by stefano on 29/06/2017.
 */
public class UsersListAdapter extends AbstractRecyclerAdapter<User, UsersListAdapter.ViewHolder> {

    private OnUserClickListener onUserClickListener;

    public OnUserClickListener getOnUserClickListener() {
        return onUserClickListener;
    }

    public void setOnUserClickListener(OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    public UsersListAdapter(Context context, List<User> mList) {
        super(context, mList);
        setList(mList);
    }

    private void sortItems(List<User> mList) {
        // sort by descending timestamp (first the last created, than the oldest)
        Collections.sort(mList, new Comparator<User>() {
            @Override
            public int compare(User item1, User item2) {
                return Long.compare(item2.getCreatedOn(), item1.getCreatedOn());
            }
        });
    }

    @Override
    public void setList(List<User> mList) {
        sortItems(mList);
        super.setList(mList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        User chatGroup = getItem(position);
        holder.pending.setVisibility(View.INVISIBLE);

        setName(holder, chatGroup.getName());

//        setCreatedOn(holder, chatGroup.getCreatedOnLong());

        setImage(holder, chatGroup.getName());

        setMembers(holder, chatGroup);

        setOnUserClickListener(holder, chatGroup, position, getOnUserClickListener());
    }


    private void setName(ViewHolder holder, String name) {
        holder.name.setText(name);
    }

//    private void setCreatedOn(MyGroupsListAdapter.ViewHolder holder, long createdOn) {
//
//        // parse the timestamp in a nice formal
//        String timestampStr = TimeUtils.getFormattedTimestamp(createdOn);
//
//        // set it
//        holder.createdOn.setText(timestampStr);
//    }

    private void setImage(ViewHolder holder, String groupName) {
        /*Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_group_avatar)
                .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                .into(holder.image);*/

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(groupName.substring(0,1).toUpperCase(), generator.getColor(groupName));
        holder.image.setImageDrawable(drawable);
    }

    private void setMembers(ViewHolder holder, User chatGroup) {


        holder.members.setText((new Date(chatGroup.getCreatedOn())).toString());
    }

    private void setOnUserClickListener(
            ViewHolder holder,
            final User chatGroup,
            final int position,
            final OnUserClickListener callback) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onUserClicked(chatGroup, position);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        //        private final TextView createdOn;
        private final ImageView image;
        private final TextView members;
        private final TextView pending;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.recipient_display_name);
//            createdOn = (TextView) itemView.findViewById(R.id.created_on);
            image = (ImageView) itemView.findViewById(R.id.recipient_picture);
            members = (TextView) itemView.findViewById(R.id.members);
            pending = (TextView) itemView.findViewById(R.id.pending);
        }
    }
}