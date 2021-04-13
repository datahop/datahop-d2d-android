/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.backend;

//import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public abstract class DataHopBackend  {

    //OnLoginListener mLoginCallback;
    //OnFileListener mFileCallback;
    // Container Activity must implement this interface
    /*public interface OnLoginListener {
        void onLoginSuccessful(FirebaseUser user);
        void onLoginFailed(String message);
    }*/
    /*public interface OnFileListener {
        void newFileAssigned(String fileUrl, String name, int id, int total);
    }*/

    public abstract void setUserId(String user);
    //public void setLoginCallback(OnLoginListener listener){mLoginCallback = listener;}
    //public void setFilesCallback(OnFileListener listener){mFileCallback = listener;}
    //public abstract void getFiles(FirebaseUser user);
    //public abstract void getFiles(FirebaseUser user);
    //public abstract void signIn(String username);
    //public abstract void signOut();
    //public abstract boolean isSignedIn();
    public abstract void serviceStarted(Date date);
    public abstract void serviceStopped(Date stopTime);
    public abstract void fileSent(Date date, String fileName);
    public abstract void fileReceived(Date date, String fileName, String group, int id, int size, long transferTime);
    //public abstract void userDiscovered(Date date, String username);
    public abstract void serviceDiscovered(Date date, String serviceName, int success);
    public abstract void connectionStarted(Date date);
    public abstract void connectionCompleted(Date started, Date completed, int rssi, int speed, int freq);
    public abstract void connectionFailed(Date started, Date failed);

}
