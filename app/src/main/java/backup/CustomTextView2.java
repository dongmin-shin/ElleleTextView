package backup;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dong.min.shin on 2017. 1. 20..
 */

public class CustomTextView2 extends TextView {

    private static final String ELLIPSIZE_STRING = "...";

    private int maxLine;
    private TextPaint textPaint;
    private String text;
    private int textAlignment;

    private int ascent;
    private int lineSpacing;


    public CustomTextView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomTextView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        maxLine = getMaxLines();
        textPaint = getPaint();
        text = (String) getText();
        textAlignment = getTextAlignment();
        lineSpacing = (int) getLineSpacingExtra();
        ascent = (int) textPaint.ascent();
    }

    private void print() {
        Log.d("TEST", "---------------- Print [Start] ---------------");
        Log.d("TEST", "maxLine: " + maxLine);
        Log.d("TEST", "textPaint: " + textPaint);
        Log.d("TEST", "text: " + text);
        Log.d("TEST", "textAlignment: " + textAlignment);
        Log.d("TEST", "ascent: " + ascent);
        Log.d("TEST", "---------------- Print [End] ---------------");
    }

    private List<String> lineBuildList = Collections.synchronizedList(new ArrayList<String>());

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int measureWidth = measureWidth(widthMeasureSpec);
//        int measureHeight = measureHeight(heightMeasureSpec);
//
//        setMeasuredDimension(measureWidth, measureHeight);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            breakWidth(specSize);
            return specSize;

        } else if (specMode == MeasureSpec.AT_MOST) {
            return Math.min(breakWidth(specSize), specSize);

        } else {
            return breakWidth(specSize);
        }
    }

    private int breakWidth(int availableWidth) {
        int maxWidth = availableWidth - getPaddingLeft() - getPaddingRight();

        int widthUsed;
        switch (maxLine) {
            case 1:
                widthUsed = (int) (textPaint.measureText(text) + 0.5f);
                break;
            case 0:
                widthUsed = 0;
                break;
            default:
                widthUsed = maxWidth;
                break;
        }

        return widthUsed + getPaddingLeft() + getPaddingRight();
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;

        } else {
            int textHeight = (int) (-ascent + textPaint.descent());
            result = getPaddingTop() + getPaddingBottom();
            if (maxLine == Integer.MAX_VALUE) {
                result += textHeight;
            } else {
                result += maxLine * textHeight + (maxLine - 1) * lineSpacing;
            }

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private void buildLineBreakIndexes() {
        lineBuildList.clear();

        final int totalLength = text.length();
        int startIndex = 0;
        String extractText = text;

        while (true) {
            int endIndex = textPaint.breakText(extractText, true, breakWidth(getWidth()), null) + startIndex;
            String sliceText = text.substring(startIndex, endIndex);
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
        int breakIndex = textPaint.breakText(ellipsizeText, true, breakWidth(getWidth()), null);

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
        }
    }

}
