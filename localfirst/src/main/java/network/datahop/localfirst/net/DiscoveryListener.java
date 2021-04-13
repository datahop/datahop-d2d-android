/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.net;

public interface DiscoveryListener {
    void onNewContent(String msg, String group, int size, int latency);
    void onUserDiscovered(String user, String address);
}