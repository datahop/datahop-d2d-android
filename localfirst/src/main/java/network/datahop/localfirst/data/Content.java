/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

public class Content {

    //private variables
    String _name;
    String _url;
    String _text;
    String _uri;
    String _crc;
    String _group;

    int _id;
    int _total;
    // Empty constructor
    public Content(){

    }
    // constructor
    public Content(String name, String uri, String text, String url, int id, int total, String crc, String group){
        this._id = id;
        this._name = name;
        this._uri = uri;
        this._text = text;
        this._url = url;
        this._crc = crc;
        this._total = total;
        this._group = group;
    }

    // getting name
    public String getName(){
        return this._name;
    }

    // setting name
    public void setName(String name){
        this._name = name;
    }

    // getting group
    public String getGroup(){
        return this._group;
    }

    // setting group
    public void setGroup(String group){
        this._group = group;
    }

    // getting description
    public String getText(){
        return this._text;
    }

    // setting description
    public void setText(String text){
        this._text = text;
    }

    // getting chunk id
    public int getId(){
        return this._id;
    }

    // setting chunk total
    public void setTotal(int total){
        this._total = total;
    }

    // getting chunk total
    public int getTotal(){
        return this._total;
    }

    // setting chunk id
    public void setId(int id){
        this._id = id;
    }


    // getting url
    public String getUrl(){
        return this._url;
    }

    // setting url
    public void setUrl(String url){
        this._url = url;
    }

    // getting crc
    public String getCrc(){
        return this._crc;
    }

    // setting crc
    public void setCrc(String crc){
        this._crc = crc;
    }

    // getting name
    public String getUri(){
        return this._uri;
    }


    // setting name
    public void setUri(String uri){
        this._uri = uri;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean isEqual= false;

        if (object != null && object instanceof Content)
        {
            isEqual = (this._name.equals (((Content) object)._name));
        }

        return isEqual;
    }

    @Override
    public String toString() {
        return "Content named:"+_name+" "+_text+" "+_uri+" "+_id+" "+_total+" "+_crc+" "+_url;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

}
