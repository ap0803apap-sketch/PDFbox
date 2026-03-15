package com.shejan.pdfbox_pdfeditor.model;

public class Tool {
    private final String id;
    private final String name;
    private final String description;
    private final int iconResId;

    public Tool(String id, String name, String description, int iconResId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
}
