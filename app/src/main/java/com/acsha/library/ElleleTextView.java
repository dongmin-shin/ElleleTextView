package com.acsha.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.text.TextUtils;
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
    private static final char SPACE_CHARACTER = ' ';

    private final List<CharSequence> lineBuildList = Collections.synchronizedList(new ArrayList<CharSequence>());

    private int maxLine;
    private TextPaint textPaint;
    private CharSequence text;

    private Drawable[] compoundDrawables;

    private int ascent;
    private int lineSpacing;

    private boolean isEnabledRemoveSpaceFrontOfText;

    public ElleleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        loadAttributes(attrs);
    }

    public ElleleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        loadAttributes(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ElleleTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        loadAttributes(attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onDrawCompoundDrawable(canvas);

        onDrawText(canvas);
    }

    private void onDrawText(Canvas canvas) {
        composeLineBreakWidthEllipsize();

        float x = getPaddingLeft() + getCompoundPaddingLeft();
        float y = getPaddingTop() - ascent;

        int size = lineBuildList.size();
        for (int i = 0; i < size; i++) {
            CharSequence text = lineBuildList.get(i);

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

    private void onDrawCompoundDrawable(Canvas canvas) {
        for (Drawable compoundDrawable : compoundDrawables) {
            if (compoundDrawable != null) {
                compoundDrawable.draw(canvas);
            }
        }
    }

    private void loadAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ElleleTextView);
        isEnabledRemoveSpaceFrontOfText = typedArray.getBoolean(R.styleable.ElleleTextView_removeSpaceFrontOfText, false);
        typedArray.recycle();
    }

    private void init() {
        maxLine = getMaxLines();
        textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        text = getText();
        lineSpacing = (int) getLineSpacingExtra();
        ascent = (int) textPaint.ascent();
        compoundDrawables = getCompoundDrawables();
    }

    private int getAvailableWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight() - getCompoundPaddingLeft() - getCompoundPaddingRight();
    }

    /**
     * 글자 단위로 LineBreak를 한 뒤, 말줄임표가 들어간 문장을 구성한다.
     */
    private void composeLineBreakWidthEllipsize() {
        init();

        lineBuildList.clear();

        if (TextUtils.isEmpty(text)) {
            return;
        }

        int startIndex = 0;
        CharSequence originalText = text;
        CharSequence extractText = originalText;

        while (true) {
            int endIndex = getLineBreakIndex(extractText) + startIndex;
            CharSequence sliceText = originalText.subSequence(startIndex, endIndex);

            // LineBreak한 두 번째 문장 부터 첫 글자 앞에 공백'들'이 존재한다면 제거한다.
            if (isEnabledRemoveSpaceFrontOfText && isLineBreakSentence(startIndex)) {
                String removedSpaceFrontOfSlicedText = getRemovedSpaceFrontOfText(sliceText);
                if (sliceText.length() != removedSpaceFrontOfSlicedText.length()) {
                    originalText = replaceOriginalText(startIndex, originalText, endIndex, removedSpaceFrontOfSlicedText);
                    extractText = originalText.subSequence(startIndex, originalText.length());
                    continue;
                }
            }

            lineBuildList.add(sliceText);
            if (endIndex >= originalText.length()) {
                break;
            }

            startIndex = endIndex;
            extractText = originalText.subSequence(startIndex, originalText.length());
        }

        composeEllipsize();
    }

    private int getLineBreakIndex(CharSequence text) {
        return textPaint.breakText(text, 0, text.length(), true, getAvailableWidth(), null);
    }

    @NonNull
    private String getRemovedSpaceFrontOfText(CharSequence sliceText) {
        String sliceTextByRemovedSpace = "";
        boolean foundLetter = false;
        for (int i = 0; i < sliceText.length(); i++) {
            char sliceCharacter = sliceText.charAt(i);

            if (!foundLetter) {
                if (sliceCharacter == SPACE_CHARACTER) {
                    continue;
                } else {
                    foundLetter = true;
                }
            }

            sliceTextByRemovedSpace += sliceCharacter;
        }

        return sliceTextByRemovedSpace;
    }

    @NonNull
    private String replaceOriginalText(int startIndex, CharSequence originalText, int endIndex, String sliceTextByRemovedSpace) {
        CharSequence frontBlock = originalText.subSequence(0, startIndex);
        CharSequence backBlock = originalText.subSequence(endIndex, originalText.length());

        return frontBlock + sliceTextByRemovedSpace + backBlock;
    }

    private boolean isLineBreakSentence(int startIndex) {
        return startIndex > 0;
    }

    /**
     * 마지막 문장에 말줄임을 구성해준다.
     */
    private void composeEllipsize() {
        if (maxLine == Integer.MAX_VALUE) {
            return;
        }

        if (maxLine >= lineBuildList.size()) {
            return;
        }

        CharSequence targetEllipsizedString = lineBuildList.get(maxLine - 1);
        CharSequence resultEllipsizedString = getEllipsizeSentenceByRecursive(targetEllipsizedString);
        lineBuildList.set(maxLine - 1, resultEllipsizedString);
    }

    /**
     * 재귀를 돌면서 마지막 문장에 말줄임표를 추가한 최대한의 문장 크기를 구한다.
     *
     * @param text - 말줄임을 하고자 하는 문장
     * @return - 파라메터로 넘어온 text에 현재 화면에 표시할 수 있는 가로 크기를 계산, 문장에 말줄임을 추가한 값을 반환
     */
    private String getEllipsizeSentenceByRecursive(final CharSequence text) {
        String ellipsizeText = text + ELLIPSIZE_STRING;
        int totalLength = ellipsizeText.length();
        int breakIndex = getLineBreakIndex(ellipsizeText);

        if (breakIndex >= totalLength) {
            return ellipsizeText;

        } else {
            CharSequence removeLastCharacterText = text.subSequence(0, text.length() - 1);
            return getEllipsizeSentenceByRecursive(removeLastCharacterText);
        }
    }

}