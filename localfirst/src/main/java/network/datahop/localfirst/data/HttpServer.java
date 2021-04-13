/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import network.datahop.localfirst.utils.G;

//import org.nanohttpd.NanoHTTPD;


public class HttpServer extends NanoHTTPD {

    List<String> messages = Arrays.asList("/getFile","/sendFile","/getList","/getFiles","/upload-list","/compare-filters","/uploadFile");
    ContentDelivery cd;
    //List<String> pendingContent;
    private final static String TAG="HttpServer";
    boolean anyGroupSharing;
    Context context;
//    SettingsPreferences pref;
    public HttpServer(int port, ContentDelivery cd, Context context,boolean anyGroupSharing) {
        super(port);
        G.Log(TAG,"Http server started at ");
        this.cd = cd;
        this.context = context;
        this.anyGroupSharing=anyGroupSharing;
   //     pref = new SettingsPreferences(context);
        //pendingContent = new ArrayList<String>();
    }

    @Override
    public Response serve(IHTTPSession session) {
        G.Log(TAG,"Response");
        Map<String, String> parms = session.getParms();
        Map<String, String> files = new HashMap<String, String>();
        String uri = session.getUri();
        Method method = session.getMethod();
        Response response = newFixedLengthResponse("");
        Map<String, String> headers = session.getHeaders();

        if (Method.POST.equals(method)) {
            G.Log(TAG,"POST "+uri);
            if(messages.contains(uri)){
                switch (uri){
                    case "/upload-list":
                        try {
                            session.parseBody(files);
                            final JSONObject json = new JSONObject(files.get("postData"));
                            //todo JSONObject jsonResponse = cd.getVideoListJSON(json);
                            G.Log(TAG,"Json received "+json.toString());
                            JSONObject jsonResponse;
                            if(!anyGroupSharing)
                                jsonResponse= cd.getVideoListOnlyLocalJSON(json);
                            else
                                jsonResponse= cd.getVideoListJSON(json);

                            response = newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(jsonResponse.toString().getBytes()),jsonResponse.length());

                        } catch (JSONException je){
                            response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: JSONException: " + je.getMessage());
                        } catch (IOException ioe) {
                                response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                        } catch (ResponseException re) {
                                response = newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                        }

                        break;

                    case "/uploadFile":
                        try {
                            int streamLength = Integer.parseInt(session.getHeaders().get("content-length"));

                            byte[] fileContent = new byte[streamLength];
                            InputStream input = session.getInputStream();
                            int bytesRead = 0;
                            int iterations = 0;
                            while (bytesRead < streamLength) {
                                int thisRead = input.read(fileContent, bytesRead, streamLength-bytesRead);
                                bytesRead += thisRead;
                                iterations++;
                            }
                            G.Log(TAG,"Upload received "+headers.get("filename")+" "+headers.get("Name")+" "+headers.get("name")+" "+headers.get("id")+" "+headers.get("total")+" "+headers.get("group"));
                            cd.saveFile(headers.get("filename"),headers.get("name"),fileContent,Integer.parseInt(headers.get("id")),Integer.parseInt(headers.get("total")),headers.get("group"),System.currentTimeMillis()-Long.parseLong(headers.get("time")));

                            response = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT,"File received");
                        } catch (IOException ioe) {
                            response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                        }
                        break;

                    default:
                        G.Log(TAG,"Default API method");
                        return newFixedLengthResponse(Response.Status.NOT_FOUND,NanoHTTPD.MIME_PLAINTEXT,"Resource Not available\n" );
                }
            } else {
                G.Log(TAG,"Request does not exist");
                return newFixedLengthResponse(Response.Status.NOT_FOUND,NanoHTTPD.MIME_PLAINTEXT,"Resource Not available\n" );

            }

        } else if (Method.GET.equals(method))
        {
            G.Log(TAG,"GET "+uri);
            if(messages.contains(uri)){
                switch (uri){
                    case "/getList":
                        G.Log(TAG,"getList");
                        //todo JSONObject obj = cd.getVideoListJSON();
                        //todo response = newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(obj.toString().getBytes()),obj.length());
                        response.addHeader("Content-Disposition", "attachment; filename=\"fileList.json\"");
                        break;

                    case "/getFile":
                        G.Log(TAG,"getFile");
                        for (Map.Entry<String, String> entry : parms.entrySet())
                        {
                            G.Log(TAG,entry.getKey() + "/" + entry.getValue());

                            if(entry.getKey().equals("file"))
                            {
                                String filename = entry.getValue();
                                String id = filename.substring(filename.lastIndexOf(".") + 1);
                                String cName = filename.substring(0,filename.lastIndexOf("."));
                                G.Log(TAG,"Get file "+cName);
                                Content c = cd.getContent(cName);
                                G.Log(TAG,"Id "+id+" file "+cName+" "+c.getTotal());

                                byte[] file = cd.readFile(entry.getValue());
                                response = newFixedLengthResponse(Response.Status.OK, "application/file", new ByteArrayInputStream(file),file.length);
                                response.addHeader("Content-Disposition", "attachment; filename=\""+entry.getValue()+"\"");
                                response.addHeader("Content-Type", "file");
                                response.addHeader("Content-Length", String.valueOf(file.length));
                                response.addHeader("Id",id);
                                response.addHeader("group",c.getGroup());
                                response.addHeader("Total",String.valueOf(c.getTotal()));
                                response.addHeader("Name",cName);
                                response.addHeader("time",String.valueOf(System.currentTimeMillis()));
                                //response.addHeader("address",pref.getAddress());
                                G.Log(TAG,"Headers "+response);
                            }
                        }
                        break;

                    /*case "/getFiles":
                        G.Log(TAG,"getFiles");
                        response = newFixedLengthResponse( msg + "</body></html>\n" );
                        break;*/

                    default:
                        G.Log(TAG,"Default API method");
                        return newFixedLengthResponse(Response.Status.NOT_FOUND,NanoHTTPD.MIME_PLAINTEXT,"Resource Not available\n" );
                }
            } else {
                G.Log(TAG,"Request does not exist");
                return newFixedLengthResponse(Response.Status.NOT_FOUND,NanoHTTPD.MIME_PLAINTEXT,"Resource Not available\n" );

            }
        }
        return response;
    }
}
