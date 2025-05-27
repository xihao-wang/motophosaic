package com.example.motophosaique;

public class HistoryItem {
    private final String imagePath;
    private final String algo;
    private final String type;     // ➕ 新增字段
    private final float timeSec;

    public HistoryItem(String imagePath, String type, String algo, float timeSec) {
        this.imagePath = imagePath;
        this.type = type;
        this.algo = algo;
        this.timeSec = timeSec;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getType() {
        return type;
    }

    public String getAlgo() {
        return algo;
    }

    public float getTimeSec() {
        return timeSec;
    }
}
