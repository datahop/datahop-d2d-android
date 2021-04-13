/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/


package network.datahop.localfirst.net.wifi;

public interface HotspotListener {
    void setNetwork(String network, String password);
    void connected();
    void disconnected();
}
