package one.entropy.haf.app;

import java.time.Instant;

public class ImageDto {
    private String name;
    private Long size;
    private String tag;
    private String url;
    private Instant lastModified;

    public ImageDto(String name, Long size, String tag, String url, Instant lastModified) {
        this.name = name;
        this.size = size;
        this.tag = tag;
        this.url = url;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
}
