package com.androidhive.openhourgoogle.api.Yelp;

/**
 * Yelp business attribute
 */
public class Business {
    private String name;
    private String mobileUrl;
    private String imageUrl;
    private String phone;

    /**
     * Constructor
     */
    public Business() {}

    public Business(String name, String mobileUrl, String imageUrl, String phone) {
        this.name = name;
        this.mobileUrl = mobileUrl;
        this.imageUrl = imageUrl;
        this.phone = phone;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getMobileUrl() {
        return mobileUrl;
    }
    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
