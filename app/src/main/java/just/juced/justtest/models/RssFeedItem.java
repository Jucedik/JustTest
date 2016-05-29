package just.juced.justtest.models;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by juced on 28.05.2016.
 */
public class RssFeedItem extends RealmObject {

    @PrimaryKey
    private String guid; // equal link

    private String providerId;
    private String title;
    private String link;
    private String description;
    private String pubDate;
    private String imageUrl;
    private String categoryName;

    public RssFeedItem() {
    }

    public RssFeedItem(String guid, String providerId, String title, String link, String description, String pubDate, String imageUrl, String categoryName) {
        this.guid = guid;
        this.providerId = providerId;
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
        this.imageUrl = imageUrl;
        this.categoryName = categoryName;
    }

    // getters and setters -------------------------------------------------------------------------
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    // ---------------------------------------------------------------------------------------------

}
