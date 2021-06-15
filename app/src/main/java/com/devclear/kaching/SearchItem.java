package com.devclear.kaching;

import android.widget.SearchView;

public class SearchItem {
    private String mImageResource;
    private String mUserName;
    private String mTags;
    private String mUserID;

    public SearchItem(String imageResource, String userName, String tags, String userID) {
        mImageResource = imageResource;
        mUserName = userName;
        mTags = tags;
        mUserID = userID;
    }

    public String getImageResource() {
        return mImageResource;
    }

    public String getUsername() {
        return mUserName;
    }

    public String getTags() {
        return mTags;
    }

    public String getuserID() {
        return  mUserID;
    }
}
