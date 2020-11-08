package com.elsner.VideoConverter.Pojo;

public class FilterModel {
    public int filterImgId;
    public String filterName;

    public FilterModel(int filterImgId, String filterName) {
        this.filterImgId = filterImgId;
        this.filterName = filterName;
    }

    public int getFilterImgId() {
        return filterImgId;
    }

    public void setFilterImgId(int filterImgId) {
        this.filterImgId = filterImgId;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }
}
