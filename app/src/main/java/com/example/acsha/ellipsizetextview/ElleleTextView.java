package com.example.acsha.ellipsizetextview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dong.min.shin on 2017. 1. 20..
 */

public class ElleleTextView extends TextView {

    private static final String ELLIPSIZE_STRING = "...";

    private final List<String> lineBuildList = Collections.synchronizedList(new ArrayList<String>());

    private int maxLine;
    private TextPaint textPaint;
    private String text;

    private int ascent;
    private int lineSpacing;


    public ElleleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ElleleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ElleleTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        maxLine = getMaxLines();
        textPaint = getPaint();
        text = (String) getText();
        lineSpacing = (int) getLineSpacingExtra();
        ascent = (int) textPaint.ascent();
    }

    private int getAvailableWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private void buildLineBreakIndexes() {
        init();

        lineBuildList.clear();

        final int totalLength = text.length();
        int startIndex = 0;
        String extractText = text;

        while (true) {
            int endIndex = textPaint.breakText(extractText, true, getAvailableWidth(), null) + startIndex;
            String sliceText = text.substring(startIndex, endIndex);

            // LineBreak 한 두 번째 문장 부터 앞글자에 공백이 존재한다면 제거한다.
            if (startIndex > 0) {
                int changedStartIndex = startIndex;
                for (int i = 0; i < sliceText.length(); i++) {
                }
            }

            lineBuildList.add(sliceText);

            if (endIndex >= totalLength) {
                break;
            }

            startIndex = endIndex;
            extractText = text.substring(startIndex, totalLength);
        }

        buildEllipsize();
    }

    private void buildEllipsize() {
        if (maxLine == Integer.MAX_VALUE) {
            return;
        }

        if (maxLine >= lineBuildList.size()) {
            return;
        }

        // 한글자, 두글자, 세글자, 네글자를 지워보면서 최대한 ... 을 붙일 수 있는 크기를 구해본다.
        String targetEllipsizedString = lineBuildList.get(maxLine - 1);
        String resultEllipsizedString = recursiveEllipsize(targetEllipsizedString);
        lineBuildList.set(maxLine - 1, resultEllipsizedString);
    }

    private String recursiveEllipsize(final String text) {
        String ellipsizeText = text + ELLIPSIZE_STRING;
        int totalLength = ellipsizeText.length();
        int breakIndex = textPaint.breakText(ellipsizeText, true, getAvailableWidth(), null);

        if (breakIndex >= totalLength) {
            return ellipsizeText;

        } else {
            String removeLastCharacterText = text.substring(0, text.length() - 1);
            return recursiveEllipsize(removeLastCharacterText);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        buildLineBreakIndexes();

        float x = getPaddingLeft();
        float y = getPaddingTop() - ascent;

        int size = lineBuildList.size();
        for (int i = 0; i < size; i++) {
            String text = lineBuildList.get(i);

            canvas.drawText(text, 0, text.length(), x, y, textPaint);

            y += (-ascent + textPaint.descent()) + lineSpacing;
            if (y > canvas.getHeight()) {
                break;
            }

            if (i == maxLine - 1) {
                break;
            }
        }
    }

}




























