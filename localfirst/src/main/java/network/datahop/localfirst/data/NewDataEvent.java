package network.datahop.localfirst.data;

public class NewDataEvent {
    private final String netName;

    public NewDataEvent(String netName) {
        this.netName = netName;
    }

    public String getNetworkName() {
        return netName;
    }
}