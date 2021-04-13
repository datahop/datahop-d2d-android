/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.backend;

import java.util.Date;

public interface Log {

    void serviceStarted(Date date);
    void serviceStopped(Date stopTime);
    void fileSent(Date date, String fileName);
    void fileReceived(Date date, String fileName, String group, int id, int size, long transferTime);
   // void userDiscovered(Date date, String username);
    void serviceDiscovered(Date date, String serviceName, int success);
    void connectionStarted(Date date);
    void connectionCompleted(Date started, Date completed, int rssi, int speed, int freq);
    void connectionFailed(Date started, Date failed);

}
