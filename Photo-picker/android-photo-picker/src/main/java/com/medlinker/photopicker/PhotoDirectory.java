package com.medlinker.photopicker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaven7 on 2016/4/5.
 * @version 1.0
 */
public class PhotoDirectory<T extends IPhotoFileEntity> {

    private String id;
    private String name;
    private String path;
    private long date;
    private List<T> photos = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhotoDirectory)) return false;

        PhotoDirectory that = (PhotoDirectory) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public List<T> getPhotos() {
        return photos;
    }

    public void setPhotos(List<T> photos) {
        this.photos = photos;
    }

    public List<String> getPhotoPaths() {
        List<String> paths = new ArrayList<String>(photos.size());
        for (T photo : photos) {
            paths.add(photo.getPath());
        }
        return paths;
    }

    public void addPhoto(int id, String path) {
        photos.add((T) PhotoPickerFactory.getPhotoFileEntityFactory().create(id,path));
    }
}
