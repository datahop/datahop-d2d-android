/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import androidx.core.util.Pair;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

import network.datahop.localfirst.utils.G;

public class StreamVolleyRequest extends Request<Pair<byte[],Map<String,String>>> {

    private final Response.Listener<Pair<byte[],Map<String,String>>> mListener;
    public Map<String,String> mParams, responseHeaders;
    private static String TAG = "StreamVolleyRequest";

    public StreamVolleyRequest(int method, String mUrl, Response.Listener<Pair<byte[],Map<String,String>>> listener, Response.ErrorListener errorListener,
                               HashMap<String,String> params){
        super(method, mUrl, errorListener);
        setShouldCache(false);
        mListener = listener;
        mParams = params;
        //Log.d(TAG, params.get("file0"));
    }

    @Override
    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return mParams;
    };

    @Override
    protected Response<Pair<byte[],Map<String,String>>> parseNetworkResponse(NetworkResponse response) {
        responseHeaders = response.headers;
        G.Log(TAG, "Network Response was called");
        Pair<byte[],Map<String,String>> resp = new Pair(response.data,responseHeaders);
        return Response.success(resp,HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Pair<byte[],Map<String,String>> response) {
        mListener.onResponse(response);
    }
}
