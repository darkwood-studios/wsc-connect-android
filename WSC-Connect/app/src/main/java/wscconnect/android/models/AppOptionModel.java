package wscconnect.android.models;

/**
 * Created by chris on 18.07.17.
 */

public class AppOptionModel {
    public final static String TYPE = "extraType";
    private String title;
    private int icon;
    private String iconUrl;
    private String type;
    private int moreIcon;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMoreIcon() {
        return moreIcon;
    }

    public void setMoreIcon(int moreIcon) {
        this.moreIcon = moreIcon;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
