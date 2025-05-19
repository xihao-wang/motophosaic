package com.example.motophosaique;

public class HistoryItem {
    public String imagePath;
    public String algo;
    public float timeSec;

    public HistoryItem(String imagePath, String algo, float timeSec) {
        this.imagePath = imagePath;
        this.algo = algo;
        this.timeSec = timeSec;
    }
}
