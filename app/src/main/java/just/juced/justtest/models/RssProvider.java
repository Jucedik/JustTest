package just.juced.justtest.models;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by juced on 27.05.2016.
 */
public class RssProvider extends RealmObject {

    @PrimaryKey
    private String id = UUID.randomUUID().toString();

    private String url;
    private String name;
    private String description;
    private String imageUrl;

    public RssProvider() {
    }

    public RssProvider(String url, String name, String description, String imageUrl) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // getters and setters -------------------------------------------------------------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    // ---------------------------------------------------------------------------------------------
}
