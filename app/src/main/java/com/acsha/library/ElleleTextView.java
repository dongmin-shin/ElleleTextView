package com.acsha.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dong.min.shin on 2017. 1. 20..
 */

public class ElleleTextView extends AppCompatTextView {

    private static final int LEFT_DRAWABLE = 0;
    private static final int TOP_DRAWABLE = 1;
    private static final int RIGHT_DRAWABLE = 2;
    private static final int BOTTOM_DRAWABLE = 3;

    private static final String ELLIPSIZE_STRING = "...";
    private static final char SPACE_CHARACTER = ' ';
    private static final char LINE_FEED = '\n';
    private static final int INVALID_INDEX = -1;

    private final List<CharSequence> lineBuildList = Collections.synchronizedList(new ArrayList<CharSequence>());

    private int maxLine;
    private TextPaint textPaint;
    private CharSequence text;

    private Drawable[] compoundDrawables;

    private int ascent;
    private int lineSpacing;

    // Custom Style Option
    private boolean isEnabledRemoveSpaceFrontOfText;

    private Drawable headDrawable;
    private float headDrawableInnerOffset;
    private float headDrawableInnerPaddingTop;
    private float headDrawableInnerPaddingBottom;
    private float headDrawableMarginRight;
    private float headDrawableResizeWidth;
    private float headDrawableResizeHeight;
    private int headDrawableVisibility;

    private boolean isEnabledEllipsize;

    private int prevMaxLine = -1;

    public ElleleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(attrs);

        initialize();
    }

    public ElleleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(attrs);

        initialize();
    }

    private void initialize() {
        maxLine = getMaxLines();
        if (maxLine == 1 && prevMaxLine != maxLine) {
            // 실제 라인 값이 변경 됐을 때, 오직 한 번만 호출돼야 한다.
            setSingleLine();
        }

        prevMaxLine = maxLine;
        textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        text = getText();
        lineSpacing = (int) getLineSpacingExtra();
        ascent = (int) textPaint.ascent();
        compoundDrawables = getCompoundDrawables();

        if (headDrawable != null) {
            headDrawableResizeHeight = getLineHeight() - headDrawableInnerOffset - lineSpacing;
            headDrawableResizeWidth = (headDrawable.getIntrinsicWidth() * headDrawableResizeHeight) / headDrawable.getIntrinsicHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        composeLineBreakWidthEllipsize();
        setMeasuredDimension(widthMeasureSpec, getReadjustmentMeasureHeight(heightMeasureSpec));
    }

    private int getReadjustmentMeasureHeight(int heightMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        ascent = (int) textPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;

        } else {
            int textHeight = (int) (-ascent + textPaint.descent());
            result = getPaddingTop() + getPaddingBottom();

            if (lineBuildList.isEmpty()) {
                result += textHeight;

            } else {
                int lineCount = Math.min(maxLine, lineBuildList.size());
                result += lineCount * textHeight + (lineCount - 1) * lineSpacing;

            }

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isTextChanged()) {
            composeLineBreakWidthEllipsize();
        }

        if (isShowHeadDrawable()) {
            onDrawHeadDrawable(canvas);
        }

        onDrawCompoundDrawable(canvas);

        onDrawText(canvas);
    }

    private void onDrawHeadDrawable(Canvas canvas) {
        int x = getCompoundPaddingLeft();
        int y = getPaddingTop();

        // Todo. HeadDrawable에 대한 Gravity 설정이 가능하도록 제공해야 한다.
        // Align Center
        int startY = y;
        if (headDrawableInnerOffset != 0) {
            startY = (int) (y + (getLineHeight() - headDrawableResizeHeight - lineSpacing) / 2);
        }

        if (headDrawableInnerPaddingTop != 0) {
            startY += headDrawableInnerPaddingTop;
        }

        if (headDrawableInnerPaddingBottom != 0) {
            startY -= headDrawableInnerPaddingBottom;
        }

        headDrawable.setBounds(x, startY, (int) (x + headDrawableResizeWidth), (int) (startY + headDrawableResizeHeight));
        headDrawable.draw(canvas);
    }

    private void onDrawText(Canvas canvas) {
        float x = getCompoundPaddingLeft();
        float y = getPaddingTop() - ascent;

        float headDrawableX = x;
        if (isShowHeadDrawable()) {
            headDrawableX += headDrawableResizeWidth + headDrawableMarginRight;
        }

        int size = lineBuildList.size();
        for (int i = 0; i < size; i++) {
            CharSequence text = lineBuildList.get(i);

            if (isShowHeadDrawable() && i == 0) {
                canvas.drawText(text, 0, text.length(), headDrawableX, y, textPaint);
            } else {
                canvas.drawText(text, 0, text.length(), x, y, textPaint);
            }

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
        // LeftDrawable Draw
        Drawable leftDrawable = compoundDrawables[LEFT_DRAWABLE];
        if (leftDrawable != null) {
            Rect boundRect = leftDrawable.getBounds();
            int correctionValue = getPaddingLeft();
            boundRect.set(boundRect.left + correctionValue, boundRect.top, boundRect.right + correctionValue, boundRect.bottom);
            leftDrawable.setBounds(boundRect);
            leftDrawable.draw(canvas);
        }

        Drawable rightDrawable = compoundDrawables[RIGHT_DRAWABLE];
        if (rightDrawable != null) {
            Rect boundRect = rightDrawable.getBounds();
            int correctionValue = getWidth() - boundRect.width();
            boundRect.set(boundRect.left + correctionValue, boundRect.top, boundRect.right + correctionValue, boundRect.bottom);
            rightDrawable.draw(canvas);
        }
    }

    private void loadAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ElleleTextView);
        isEnabledRemoveSpaceFrontOfText = typedArray.getBoolean(R.styleable.ElleleTextView_removeSpaceFrontOfText, false);
        headDrawable = typedArray.getDrawable(R.styleable.ElleleTextView_headDrawable);
        headDrawableInnerOffset = typedArray.getDimension(R.styleable.ElleleTextView_headDrawableInnerOffset, 0);
        headDrawableInnerPaddingTop = typedArray.getDimension(R.styleable.ElleleTextView_headDrawableInnerPaddingTop, 0);
        headDrawableInnerPaddingBottom = typedArray.getDimension(R.styleable.ElleleTextView_headDrawableInnerPaddingBottom, 0);
        headDrawableMarginRight = typedArray.getDimension(R.styleable.ElleleTextView_headDrawableMarginRight, 0);
        headDrawableVisibility = typedArray.getInt(R.styleable.ElleleTextView_headDrawableVisibility, View.VISIBLE);
        isEnabledEllipsize = typedArray.getBoolean(R.styleable.ElleleTextView_enableEllipsize, false);

        typedArray.recycle();
    }

    public void setHeadDrawableVisibility(int headDrawableVisibility) {
        this.headDrawableVisibility = headDrawableVisibility;
        invalidate();
    }

    public void setEnabledEllipsize(boolean enabledEllipsize) {
        isEnabledEllipsize = enabledEllipsize;
        invalidate();
    }

    private boolean isShowHeadDrawable() {
        return headDrawable != null && headDrawableVisibility == View.VISIBLE;
    }

    private int getAvailableWidth() {
        return getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
    }

    private int getAvailableWidthWithHeadDrawable() {
        return (int) (getAvailableWidth() - headDrawableResizeWidth - headDrawableMarginRight);
    }

    /**
     * 글자 단위로 LineBreak를 한 뒤, 말줄임표가 들어간 문장을 구성한다.
     */
    private void composeLineBreakWidthEllipsize() {
        initialize();

        lineBuildList.clear();

        if (TextUtils.isEmpty(text)) {
            return;
        }

        int startIndex = 0;
        CharSequence originalText = text;
        CharSequence extractText = originalText;

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int endIndex;
            if (isShowHeadDrawable() && i == 0) {
                endIndex = getLineBreakIndexWithHeadDrawable(extractText) + startIndex;
            } else {
                endIndex = getLineBreakIndex(extractText) + startIndex;
            }

            // find Line Feed & correction endIndex
            int firstLineFeedIndex = findLineFeedIndex(extractText);
            if (firstLineFeedIndex != INVALID_INDEX && endIndex > firstLineFeedIndex) {
                endIndex = firstLineFeedIndex + startIndex + 1;
            }

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

        if (isEnabledEllipsize) {
            composeEllipsize();
        }
    }

    private int findLineFeedIndex(CharSequence extractText) {
        for (int i = 0; i < extractText.length(); i++) {
            char extractCharacter = extractText.charAt(i);
            if (extractCharacter == LINE_FEED) {
                return i;
            }
        }

        return INVALID_INDEX;
    }

    private int getLineBreakIndex(CharSequence text) {
        return textPaint.breakText(text, 0, text.length(), true, getAvailableWidth(), null);
    }

    private int getLineBreakIndexWithHeadDrawable(CharSequence text) {
        return textPaint.breakText(text, 0, text.length(), true, getAvailableWidthWithHeadDrawable(), null);
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

        int breakIndex;
        if (isShowHeadDrawable() && maxLine == 1) {
            breakIndex = getLineBreakIndexWithHeadDrawable(ellipsizeText);
        } else {
            breakIndex = getLineBreakIndex(ellipsizeText);
        }

        if (breakIndex >= totalLength) {
            return ellipsizeText;
        }

        if (text.length() > 0) {
            CharSequence removeLastCharacterText = text.subSequence(0, text.length() - 1);
            return getEllipsizeSentenceByRecursive(removeLastCharacterText);
        } else {
            return "";
        }
    }

    private CharSequence toTextRidOfNull(CharSequence charSequence) {
        return charSequence == null ? "" : charSequence;
    }

    private boolean isTextChanged() {
        CharSequence originText = toTextRidOfNull(text);
        CharSequence targetText = toTextRidOfNull(getText());

        return !originText.equals(targetText);
    }

    @VisibleForTesting
    private void printAttributeDebug() {
        Log.d("TEST", "isEnableRemoveSpaceFrontOfText: " + isEnabledRemoveSpaceFrontOfText + "\n"
                + "headDrawable: " + headDrawable + "\n"
                + "headDrawableMarginRight: " + headDrawableMarginRight
                + "headDrawableVisibility: " + headDrawableVisibility);

    }
}