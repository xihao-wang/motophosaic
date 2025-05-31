package com.example.motophosaique;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


public class GridOverlayView extends View {
    private int imagePxWidth = 0;
    private int imagePxHeight = 0;
    private int blockSize = 16;

    private Paint dashPaint;

    public GridOverlayView(Context context) {
        super(context);
        init();
    }
    public GridOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GridOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setColor(0xFF000000); // 默认黑色虚线，可在外部修改
        dashPaint.setStrokeWidth(1f);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
    }

    /**
     * 让调用方传入原始图片的像素宽高，方便我们计算“从图片像素坐标到 View 坐标”的缩放比例。
     */
    public void setImageInfo(int origWidth, int origHeight) {
        this.imagePxWidth = origWidth;
        this.imagePxHeight = origHeight;
        invalidate();
    }

    /**
     * 当滑块（blockSize）改变时调用，告诉网格当前每个小方块在【原始图片】上的像素大小。
     */
    public void setBlockSize(int blockSizePx) {
        this.blockSize = blockSizePx;
        invalidate();
    }

    /**
     * 如果想动态调整虚线颜色，可以提供一个 setter：
     */
    public void setDashColor(int color) {
        dashPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (imagePxWidth <= 0 || imagePxHeight <= 0 || blockSize <= 0) {
            return;
        }

        int viewW = getWidth();
        int viewH = getHeight();
        if (viewW == 0 || viewH == 0) return;

        float scaleX = (float) viewW / (float) imagePxWidth;
        float scaleY = (float) viewH / (float) imagePxHeight;
        float scale = Math.min(scaleX, scaleY);

        float dispImgW = imagePxWidth * scale;
        float dispImgH = imagePxHeight * scale;

        float offsetX = (viewW - dispImgW) / 2f;
        float offsetY = (viewH - dispImgH) / 2f;

        float cellSizeOnView = blockSize * scale;
        if (cellSizeOnView < 1f) return;

        int numCols = (int) Math.ceil(dispImgW / cellSizeOnView);
        int numRows = (int) Math.ceil(dispImgH / cellSizeOnView);

        for (int i = 0; i <= numCols; i++) {
            float x = offsetX + i * cellSizeOnView;
            canvas.drawLine(x, offsetY, x, offsetY + dispImgH, dashPaint);
        }
        for (int j = 0; j <= numRows; j++) {
            float y = offsetY + j * cellSizeOnView;
            canvas.drawLine(offsetX, y, offsetX + dispImgW, y, dashPaint);
        }
    }
}

