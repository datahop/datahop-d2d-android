package network.datahop.localsharing.ui.fragments.detail;

import android.content.Context;
import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import network.datahop.localfirst.data.Group;
import network.datahop.localsharing.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by stefano on 29/06/2017.
 */
public class GroupsListAdapter extends AbstractRecyclerAdapter<Group, GroupsListAdapter.ViewHolder> {

    private OnGroupClickListener onGroupClickListener;

    public OnGroupClickListener getOnGroupClickListener() {
        return onGroupClickListener;
    }

    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
        this.onGroupClickListener = onGroupClickListener;
    }

    public GroupsListAdapter(Context context, List<Group> mList) {
        super(context, mList);
        setList(mList);
    }

    private void sortItems(List<Group> mList) {
        // sort by descending timestamp (first the last created, than the oldest)
        Collections.sort(mList, new Comparator<Group>() {
            @Override
            public int compare(Group item1, Group item2) {
                return Long.compare(item2.getCreatedOn(), item1.getCreatedOn());
            }
        });
    }

    @Override
    public void setList(List<Group> mList) {
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
        Group chatGroup = getItem(position);

        setName(holder, chatGroup.getName(),chatGroup.pendingItems());

//        setCreatedOn(holder, chatGroup.getCreatedOnLong());

        setImage(holder, chatGroup.getName());

        setMembers(holder, chatGroup);

        setOnGroupClickListener(holder, chatGroup, position, getOnGroupClickListener());
    }


    private void setName(ViewHolder holder, String group, int pending) {

        holder.name.setText(group);
        Log.d("Viewholder",group+" pending "+pending);
        if(pending>0) {
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD_ITALIC);
            holder.pending.setVisibility(View.VISIBLE);
            holder.pending.setText(String.valueOf(pending));
        } else {holder.pending.setVisibility(View.INVISIBLE);}


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

    private void setMembers(ViewHolder holder, Group chatGroup) {

        String members;
        if (chatGroup.getMembersList() != null && chatGroup.getMembersList().size() > 0) {
            members = chatGroup.printMembersListWithSeparator(", ");
        } else {
            // if there are no members show the logged user as "you"
            members = holder.itemView.getContext().getString(R.string.activity_message_list_group_info_you_label);
        }

        //holder.members.setText(members);
    }

    private void setOnGroupClickListener(
            ViewHolder holder,
            final Group chatGroup,
            final int position,
            final OnGroupClickListener callback) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onGroupClicked(chatGroup, position);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                callback.onGroupLongClicked(chatGroup, position);
                return true;
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