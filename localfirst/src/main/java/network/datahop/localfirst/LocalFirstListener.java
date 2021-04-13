/*******************************************************
 * Copyright (C) 2017-2018 DataHop Network Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst;


public interface LocalFirstListener
{
	void newFileReceived(String name);
	void newUSerDiscovered(String name);
	void newDataDiscovered(String data);
}
