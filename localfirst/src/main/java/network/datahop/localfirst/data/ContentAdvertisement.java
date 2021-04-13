/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import network.datahop.localfirst.utils.G;

/**
 *
 * A class representing the bloomfilter advertised by users containing the local videos*/

public class ContentAdvertisement {

    private BloomFilter bloomFilter;

    private static final String TAG = "ContentAdvertisement";
    // Empty constructor
    public ContentAdvertisement(){
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 50,0.03);
    }

    public ContentAdvertisement(String filter){
        ByteArrayInputStream in = new ByteArrayInputStream(filter.getBytes());
        try {
           // G.Log(TAG,"Filter "+filter);
            bloomFilter = BloomFilter.readFrom(in, Funnels.stringFunnel(Charset.defaultCharset()));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bloomFilter.writeTo(out);
            //G.Log(TAG,"New filter "+out.size()+" "+out.toString());
        } catch (Exception e){G.Log("Exception "+e);}

    }

    public ContentAdvertisement(byte[] filter){
        ByteArrayInputStream in = new ByteArrayInputStream(filter);
        try {
           // G.Log(TAG,"Filter "+filter);
            bloomFilter = BloomFilter.readFrom(in, Funnels.stringFunnel(Charset.defaultCharset()));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bloomFilter.writeTo(out);
            //G.Log(TAG,"New filter "+out.size()+" "+out.toString());
        } catch (Exception e){G.Log("Exception "+e);}

    }

    public void addElement(String str)
    {
        G.Log(TAG,"Add element "+str);
        bloomFilter.put(str);

        // G.Log(TAG,"Contains elemment "+bloomFilter.mightContain("/source".getBytes()));
    }

    public boolean checkElement(String str)
    {
        return bloomFilter.mightContain(str);

    }

    public String getFilter()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try{
            bloomFilter.writeTo(out);

            G.Log(TAG,"BF size "+out.size());
        } catch (IOException e)
        {
            G.Log(TAG,"Exception "+e);
        }

        return out.toString();
    }

    public byte[] getFilterBytes()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try{
            bloomFilter.writeTo(out);

            G.Log(TAG,"BF size "+out.size());
        } catch (IOException e)
        {
            G.Log(TAG,"Exception "+e);
        }

        return out.toByteArray();
    }

    public BloomFilter getBloomFilter()
    {
        return bloomFilter;
    }


    public boolean compareFilters(String filter){


        return connectFilter(filter);
    }

    public boolean connectFilter(String filter){
        /*ContentAdvertisement ca = new ContentAdvertisement(filter);

         G.Log(TAG,"BF received size "+ca.getFilter());
        G.Log(TAG,"Hash "+ ca.getBloomFilter().hashCode() +" "+ca.checkElement("/source"));
        G.Log(TAG,"Hash "+bloomFilter.hashCode());

        if(filter.equals(getFilter()))return false;
       //if(source.equals("source")||id<)
        if(ca.checkElement("/source")) {
            try {
                ContentAdvertisement ca2 = (ContentAdvertisement) this.clone();
                ca2.addElement("/source");
                return !ca2.getFilter().equals(filter);
            } catch (Exception e) {
            }

           // return true;
        }else
            return ca.getBloomFilter().hashCode() < bloomFilter.hashCode();
*/      G.Log(TAG,"BF received size "+getFilter());
        return !getFilter().equals(filter);
        //return false;
    }

    public boolean connectFilter(byte[] filter){
        ContentAdvertisement ca = new ContentAdvertisement(filter);

        //G.Log(TAG,"BF received size "+ca.getFilter() +" "+getFilter());
        //G.Log(TAG,"Hash "+ ca.getBloomFilter().hashCode() +" "+ca.checkElement("/source"));
        //G.Log(TAG,"Hash "+getFilter()+" "+ca.getFilter()+" "+getFilter().equals(ca.getFilter()));

        return !getFilter().equals(ca.getFilter());
        //if(source.equals("source")||id<)
        /*if(ca.checkElement("/source")) {
            try {
                ContentAdvertisement ca2 = (ContentAdvertisement) this.clone();
                ca2.addElement("/source");
                return !ca2.getFilter().equals(filter);
            } catch (Exception e) {
            }

           // return true;
        }else
            return ca.getBloomFilter().hashCode() < bloomFilter.hashCode();*/
        //G.Log(TAG,"BF2 received size "+new String(filter) +" "+getFilter());
        //return !getFilter().equals(new String(filter));
        //return false;
    }

    //Putting elements into the filter
    //A BigInteger representing a key of some sort
    //Testing for element in set
}