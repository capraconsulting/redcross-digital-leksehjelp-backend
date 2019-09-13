package no.capraconsulting.globalclasses;


public class File {
    private String share;
    private String directory;
    private String fileName;
    private String fileUrl;

    public File(String share, String directory, String fileName, String fileUrl) {
        this.share = share;
        this.directory = directory;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
