package network.datahop.localsharing.data;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by stefanodp91 on 16/01/17.
 */

public class User {

    private Long last;
    private String name;
    private String address;


    public Long getCreatedOn() {
        return last;
    }

    public void setTimestamp(Long createdOn) {
        this.last = createdOn;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean isEqual= false;

        if (object != null && object instanceof User)
        {
            isEqual = (this.name.equals (((User) object).name));
        }

        return isEqual;
    }

}
