package de.geko.application;

public class Category {

    private String name;
    private long id;
    private long parentId;

    public Category(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public Category(String name, long id, long parentId) {
        this.name = name;
        this.id = id;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }
}
