package com.example.motophosaique;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlgoViewModel extends ViewModel {
    public final MutableLiveData<String> selectedAlgo = new MutableLiveData<>("average");
    public final MutableLiveData<Boolean> withRep = new MutableLiveData<>(false);
    public final MutableLiveData<List<HistoryItem>> generatedImages = new MutableLiveData<>(new ArrayList<>());

    /**
     * 添加生成记录，如果上一条记录与当前完全一致（type + algo），则跳过
     */
    public void generateImage(String imagePath, String type, String algo, float timeSec) {
        List<HistoryItem> current = generatedImages.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }

        // 避免重复添加相同算法与类型的连续记录
        if (!current.isEmpty()) {
            HistoryItem last = current.get(current.size() - 1);
            if (last.getAlgo().equals(algo) && last.getType().equals(type)) {
                return;
            }
        }

        HistoryItem item = new HistoryItem(imagePath, type, algo, timeSec);
        List<HistoryItem> updated = new ArrayList<>(current);
        updated.add(item);
        generatedImages.setValue(updated);
    }
}
