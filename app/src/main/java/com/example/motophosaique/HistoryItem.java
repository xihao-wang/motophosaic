package com.example.motophosaique;

public class HistoryItem {
    private final String imagePath;
    private final String algo;
    private final String type;
    private final float timeSec;
    private final String originalUri;
    public HistoryItem(String imagePath,String originalUri, String type, String algo, float timeSec) {
        this.imagePath = imagePath;
        this.type = type;
        this.algo = algo;
        this.timeSec = timeSec;
        this.originalUri = originalUri;
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
    public String getOriginalUri() {return originalUri;}
}
