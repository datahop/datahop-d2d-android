package network.datahop.localsharing.ui.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import network.datahop.localfirst.data.Group;
import network.datahop.localsharing.R;
import network.datahop.localsharing.data.ContentDatabaseHandler;
import network.datahop.localsharing.ui.fragments.detail.ItemDecoration;

import network.datahop.localsharing.ui.fragments.detail.GroupsListAdapter;
import network.datahop.localsharing.ui.fragments.detail.OnGroupClickListener;
import network.datahop.localsharing.utils.G;

import java.util.List;


public class GroupsListFragment extends Fragment {
    private static final String TAG = GroupsListFragment.class.getName();

    //private GroupsSyncronizer chatGroupsSynchronizer;
    private OnGroupClickListener onChatGroupClickListener;

    // contacts list recyclerview
    private RecyclerView recyclerViewChatGroups;
    private LinearLayoutManager lmRvChatGroups;
    private GroupsListAdapter chatGroupsListAdapter;

    // no contacts layout
    private RelativeLayout noChatGroupsLayout;

    public static Fragment newInstance() {
        Fragment mFragment = new GroupsListFragment();
        return mFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //chatGroupsSynchronizer = ChatManager.getInstance().getGroupsSyncronizer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_groups_list, container, false);

        // init RecyclerView
        recyclerViewChatGroups = view.findViewById(R.id.chat_groups_list);
        recyclerViewChatGroups.addItemDecoration(new ItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL,
                getResources().getDrawable(R.drawable.decorator_activity_my_groups_list)));
        lmRvChatGroups = new LinearLayoutManager(getActivity());
        recyclerViewChatGroups.setLayoutManager(lmRvChatGroups);
        ContentDatabaseHandler db = new ContentDatabaseHandler(getContext());
        for(Group group : db.getGroups())
        {
            G.Log(TAG,"Group "+group.getGroupId()+" "+group.getName());
        }
        updateChatGroupsListAdapter(db.getGroups());
        //updateChatGroupsListAdapter(chatGroupsSynchronizer.getChatGroups());

        // no contacts layout
        //noChatGroupsLayout = view.findViewById(R.id.layout_no_groups);
        //toggleNoContactsLayoutVisibility(chatGroupsListAdapter.getItemCount());

        //chatGroupsSynchronizer.addGroupsListener(this);
        //chatGroupsSynchronizer.connect();

        return view;
    }

    public void updateChatGroupsListAdapter(List<Group> list) {
        if (chatGroupsListAdapter == null) {
            // init RecyclerView adapter
            chatGroupsListAdapter = new GroupsListAdapter(getActivity(), list);
            if (getOnChatGroupClickListener() != null)
                chatGroupsListAdapter.setOnGroupClickListener(getOnChatGroupClickListener());
            recyclerViewChatGroups.setAdapter(chatGroupsListAdapter);
        } else {
            chatGroupsListAdapter.setList(list);
            chatGroupsListAdapter.notifyDataSetChanged();
        }
    }

    // toggle the no contacts layout visibilty.
    // if there are items show the list of item, otherwise show a placeholder layout
    /*private void toggleNoContactsLayoutVisibility(int itemCount) {
        if (itemCount > 0) {
            // show the item list
            recyclerViewChatGroups.setVisibility(View.VISIBLE);
            noChatGroupsLayout.setVisibility(View.GONE);
        } else {
            // show the placeholder layout
            recyclerViewChatGroups.setVisibility(View.GONE);
            noChatGroupsLayout.setVisibility(View.VISIBLE);
        }
    }*/

    public void setOnChatGroupClickListener(OnGroupClickListener onChatGroupClickListener) {
        this.onChatGroupClickListener = onChatGroupClickListener;
    }

    public OnGroupClickListener getOnChatGroupClickListener() {
        return onChatGroupClickListener;
    }

    public void onGroupAdded(Group chatGroup, RuntimeException e) {
        if (e == null) {
            chatGroupsListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "ChatGroupsListFragment.onGroupAdded: e == " + e.toString());
        }
    }

    public void onGroupChanged(Group chatGroup, RuntimeException e) {
        if (e == null) {
            chatGroupsListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "ChatGroupsListFragment.onGroupChanged: e == " + e.toString());
        }

    }

    public void onGroupRemoved(RuntimeException e) {
        if (e == null) {
            chatGroupsListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "ChatGroupsListFragment.onGroupRemoved: e == " + e.toString());
        }

    }


}