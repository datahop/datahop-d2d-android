package network.datahop.localsharing.ui.fragments.detail;


import network.datahop.localfirst.data.Group;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public interface OnGroupClickListener {
    void onGroupClicked(Group chatGroup, int position);
    void onGroupLongClicked(Group chatGroup,int position);
}
