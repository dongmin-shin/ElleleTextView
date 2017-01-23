package backup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @author dong.min.shin on 2017. 1. 19..
 */

public class ElliTextView extends TextView {

    public final static String NEW_LINE_STR = "\n";

    private ArrayList<int[]> mLines;
    private String mText;
    private Paint mPaint;

    private int mAscent;

    private String mStrEllipsis;
    private int mMaxLineCount;
    private int mDrawLineCount;
    private boolean mExpanded = false;
    private int mLineSpacing;

    public ElliTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ElliTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ElliTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        mText = (String) getText();
        requestLayout();
        invalidate();
    }

    private void init() {
        mStrEllipsis = "...";
        mText = (String) getText();

        int maxLineCount = getMaxLines();
        mMaxLineCount = maxLineCount >= 2 ? maxLineCount : 1;
        mLineSpacing = (int) getLineSpacingExtra();
        mPaint = getPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = measureWidth(widthMeasureSpec);
        int measureHeight = measureHeight(heightMeasureSpec);

        setMeasuredDimension(measureWidth, measureHeight);
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

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;

        } else {
            if (mExpanded) {
                mDrawLineCount = mLines.size();
            } else if (mLines.size() > mMaxLineCount) {
                mDrawLineCount = mMaxLineCount;
            } else {
                mDrawLineCount = mLines.size();
                mExpanded = true;
            }

            int textHeight = (int) (-mAscent + mPaint.descent());
            result = getPaddingTop() + getPaddingBottom();
            if (mDrawLineCount > 0) {
                result += mDrawLineCount * textHeight + (mDrawLineCount - 1) * mLineSpacing;
            } else {
                result += textHeight;
            }

            if (specMode == MeasureSpec.AT_MOST) result = Math.min(result, specSize);
        }

        return result;
    }

    private int breakWidth(int availableWidth) {
        int maxWidth = availableWidth - getPaddingLeft() - getPaddingRight();

        if (mLines == null) {
            if (maxWidth == -1) {
                mLines = new ArrayList<int[]>(1);
                mLines.add(new int[]{0, mText.length()});
            } else {
                int index = 0;
                int newlineIndex = 0;
                int endCharIndex = 0;
                mLines = new ArrayList<int[]>(mMaxLineCount * 2);

                while (index < mText.length()) {
                    if (index == newlineIndex) {
                        newlineIndex = mText.indexOf(NEW_LINE_STR, newlineIndex);
                        endCharIndex = (newlineIndex != -1) ? newlineIndex : mText.length();
                    }

                    int charCount = mPaint.breakText(mText, index, endCharIndex, true, maxWidth, null);
                    if (charCount > 0) {
                        mLines.add(new int[]{index, index + charCount});
                        index += charCount;
                    }

                    if (index == newlineIndex) {
                        newlineIndex++;
                        index++;
                    }
                }
            }
        }

        int widthUsed;
        switch (mLines.size()) {
            case 1:
                widthUsed = (int) (mPaint.measureText(mText) + 0.5f);
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

    @Override
    protected void onDraw(Canvas canvas) {
        int renderWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        float x = getPaddingLeft();
        float y = getPaddingTop() - mAscent;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mDrawLineCount; i++) {
            sb.append(mText, mLines.get(i)[0], mLines.get(i)[1]);

            if (!mExpanded && mDrawLineCount - i == 1) {
                float lineDrawWidth = mPaint.measureText(sb, 0, sb.length());
                float ellipsisWidth = mPaint.measureText(mStrEllipsis);

                while (lineDrawWidth + ellipsisWidth > renderWidth) {
                    sb.deleteCharAt(sb.length() - 1);
                    lineDrawWidth = mPaint.measureText(sb, 0, sb.length());
                }
                sb.append(mStrEllipsis);
            }

            canvas.drawText(sb, 0, sb.length(), x, y, mPaint);

            y += (-mAscent + mPaint.descent()) + mLineSpacing;
            if (y > canvas.getHeight()) break;

            sb.delete(0, sb.length());
        }
    }
}
