/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

import network.datahop.localfirst.utils.G;

public class UploadFileRequest extends Request<String> {

    /** Content type for request. */
    private final String PROTOCOL_CONTENT_TYPE  = String.format("application/octet-stream");;

    private final Response.Listener<String> mListener;
    public Map<String,String> mParams, responseHeaders;
    private static String TAG = "StreamVolleyRequest";

    private final byte[] file;

    public UploadFileRequest(int method,
                               String mUrl,
                               @Nullable byte[] file,
                               Response.Listener<String> listener,
                               @Nullable Response.ErrorListener errorListener,
                               HashMap<String,String> params) {
        super(method, mUrl, errorListener);
        setShouldCache(false);
        mListener = listener;
        mParams = params;
        this.file = file;

    }

    @Override
    protected Map<String, String> getParams()
            throws AuthFailureError {
        return mParams;
    };

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        responseHeaders = response.headers;
        G.Log(TAG, "Network Response was called");
        return Response.success(response.data.toString(),HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
            return file;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        return mParams;
    }
}
