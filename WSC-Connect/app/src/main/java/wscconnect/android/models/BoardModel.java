package wscconnect.android.models;

import wscconnect.android.R;

/**
 * Created by chris on 18.07.17.
 */

public class BoardModel {
    private int boardID;
    private String title;
    private int unreadThreads;
    private String description;
    private String link;
    private int type;
    private int parentBoardID;
    private int depth;

    public static final int TYPE_BOARD = 0;
    public static final int TYPE_CATEGORY = 1;
    public static final int TYPE_LINK = 2;

    public Integer getIcon() {
        switch (type) {
            case TYPE_BOARD:
                return R.drawable.ic_folder_black_24dp;
            case TYPE_CATEGORY:
                return R.drawable.ic_keyboard_arrow_down_black_24dp;
            case TYPE_LINK:
                return R.drawable.ic_public_black_24dp;
            default:
                return null;
        }
    }

    public boolean isBoard() {
        return type == TYPE_BOARD;
    }

    public boolean isCategory() {
        return type == TYPE_CATEGORY;
    }

    public boolean isLink() {
        return type == TYPE_LINK;
    }

    public int getBoardID() {
        return boardID;
    }

    public void setBoardID(int boardID) {
        this.boardID = boardID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getParentBoardID() {
        return parentBoardID;
    }

    public void setParentBoardID(int parentBoardID) {
        this.parentBoardID = parentBoardID;
    }

    public int getDepth() {
        return depth;
    }

    public int getFixedDepth() {
        return (depth > 4) ? 4 : depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getUnreadThreads() {
        return unreadThreads;
    }

    public void setUnreadThreads(int unreadThreads) {
        this.unreadThreads = unreadThreads;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
