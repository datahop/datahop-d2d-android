package network.datahop.localfirst.data;

public class NewContentEvent {
    private final String fileName;

    public NewContentEvent(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}