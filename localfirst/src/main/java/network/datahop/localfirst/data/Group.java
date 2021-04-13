/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Group  {

    private int groupId;
    private Long createdOn;
    private String iconURL = "NOICON";
    private Map<String, Integer> members;
    private String name;
    private String owner;
    private int pending;

    public Group() {
        members = new HashMap<>();
        //groupId++;
    }

//    public ChatGroup(String name, String owner) {
//        this.name = name;
//        this.owner = owner;
//        members = new HashMap<>();
//    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setTimestamp(Long createdOn) {
        this.createdOn = createdOn;
    }


    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public Map<String, Integer> getMembers() {
        return members;
    }


    public List<String> getMembersList() {

        return null;
    }

    public int pendingItems()
    {
        return pending;
    }

    public void setPending(int pending)
    {
        this.pending = pending;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




    public String printMembersListWithSeparator(String separator) {
        String delimitedList = "";
        String usersWithoutDisplayName = "";

        if (getMembersList() != null && getMembersList().size() > 0) {
            // append chat users
            Iterator<String> it = getMembersList().iterator();

            while (it.hasNext()) {
                String usr = it.next();
                delimitedList += separator + usr;

            }

            // append the list of the user without the fullName to the list of the user with a
            // valid fullName
            delimitedList = delimitedList + usersWithoutDisplayName;

            // if the string starts with separator remove it
            if (delimitedList.startsWith(separator)) {
                delimitedList = delimitedList.replaceFirst("^" + separator, "");
            }
        }

        return delimitedList;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean isEqual= false;

        if (object != null && object instanceof Group)
        {
            isEqual = (this.name.equals (((Group) object).name));
        }

        return isEqual;
    }


    @Override
    public String toString() {
        return "ChatGroup{" +
                "groupId='" + groupId + '\'' +
                ", createdOn=" + createdOn +
                ", iconURL='" + iconURL + '\'' +
                ", members=" + members +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
