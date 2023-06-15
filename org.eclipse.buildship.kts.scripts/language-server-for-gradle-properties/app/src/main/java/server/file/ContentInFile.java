package server.file;

public class ContentInFile {

  private String content;
  private int version;

  public ContentInFile(String content, int version) {
    this.content = content;
    this.version = version;
  }

  public void updateFile(String newContent, int newVersion) {
    content = newContent;
    version = newVersion;
  }

  public String getContent() {
    return content;
  }

  public int getVersion() {
    return version;
  }
}
