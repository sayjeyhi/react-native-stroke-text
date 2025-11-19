package com.catshoulders.stroketext;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.facebook.react.uimanager.ThemedReactContext;

import java.util.HashMap;
import java.util.Map;

class StrokeTextView extends View {
    private static final String TAG = "StrokeTextView";

    private String text = "";
    private float fontSize = 14;
    private int textColor = 0xFF000000;
    private int strokeColor = 0xFFFFFFFF;
    private float strokeWidth = 1;
    private String fontFamily = "sans-serif";
    private int numberOfLines = 0;
    private boolean ellipsis = false;
    private final TextPaint textPaint;
    private final TextPaint strokePaint;
    private Layout.Alignment alignment = Layout.Alignment.ALIGN_CENTER;
    private StaticLayout textLayout;
    private StaticLayout strokeLayout;
    private boolean layoutDirty = true;
    private float customWidth = 0;
    private final Map<String, Typeface> fontCache = new HashMap<>();
    private int measuredWidth = 0;
    private int measuredHeight = 0;

    public StrokeTextView(ThemedReactContext context) {
        super(context);
        Log.d(TAG, "Constructor called");

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        strokePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        // Initialize paint with default values
        float scaledFontSize = getScaledSize(fontSize);
        textPaint.setTextSize(scaledFontSize);
        textPaint.setColor(textColor);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(getScaledSize(strokeWidth));
        strokePaint.setColor(strokeColor);
        strokePaint.setTextSize(scaledFontSize);

        // Ensure view draws
        setWillNotDraw(false);

        // Set layer type for proper text rendering
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Set a minimum size to ensure view is visible
        setMinimumWidth((int) scaledFontSize * 2);
        setMinimumHeight((int) scaledFontSize + 10);

        // Ensure view is visible
        setVisibility(View.VISIBLE);
        setAlpha(1.0f);

        // Force layout on next frame
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });

        Log.d(TAG, "Constructor complete - fontSize: " + fontSize + ", scaledSize: " + scaledFontSize);
    }

    private void createLayout(int width) {
        Log.d(TAG, "createLayout called with width: " + width);

        try {
            if (width <= 0) {
                width = getDefaultWidth();
                Log.d(TAG, "Width was <= 0, using default: " + width);
            }

            Typeface typeface = getFont(fontFamily);
            float scaledFontSize = getScaledSize(fontSize);

            Log.d(TAG, "Creating layout - text: '" + text + "', fontSize: " + scaledFontSize + ", width: " + width);

            textPaint.setTypeface(typeface);
            textPaint.setTextSize(scaledFontSize);
            textPaint.setColor(textColor);

            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(getScaledSize(strokeWidth));
            strokePaint.setColor(strokeColor);
            strokePaint.setTypeface(typeface);
            strokePaint.setTextSize(scaledFontSize);

            // Ensure we have text to render
            String textToRender = (text != null && !text.isEmpty()) ? text : "";
            if (textToRender.isEmpty()) {
                Log.d(TAG, "No text to render, setting minimal dimensions");
                layoutDirty = false;
                measuredWidth = (int) (scaledFontSize * 2);
                measuredHeight = (int) (scaledFontSize + getScaledSize(strokeWidth) * 2);
                return;
            }
            Log.d(TAG, "Text to render: '" + textToRender + "'");

            CharSequence ellipsizedText = textToRender;
            if (ellipsis && width > 0) {
                ellipsizedText = TextUtils.ellipsize(textToRender, textPaint, width, TextUtils.TruncateAt.END);
            }

            textLayout = new StaticLayout(ellipsizedText, textPaint, width, alignment, 1.0f, 0.0f, false);

            if (numberOfLines > 0 && numberOfLines < textLayout.getLineCount()) {
                int lineEnd = textLayout.getLineEnd(numberOfLines - 1);
                ellipsizedText = ellipsizedText.subSequence(0, lineEnd);
                textLayout = new StaticLayout(ellipsizedText, textPaint, width, alignment, 1.0f, 0.0f, false);
            }

            strokeLayout = new StaticLayout(ellipsizedText, strokePaint, width, alignment, 1.0f, 0.0f, false);

            layoutDirty = false;

            // Store measured dimensions with padding for stroke
            float strokePadding = getScaledSize(strokeWidth);
            measuredWidth = (int) Math.ceil(textLayout.getWidth() + strokePadding * 2);
            measuredHeight = (int) Math.ceil(textLayout.getHeight() + strokePadding * 2);

            Log.d(TAG, "Layout created successfully - measuredWidth: " + measuredWidth + ", measuredHeight: " + measuredHeight + ", lineCount: " + textLayout.getLineCount() + ", strokePadding: " + strokePadding);

        } catch (Exception e) {
            Log.e(TAG, "Error creating layout", e);
            e.printStackTrace();
            layoutDirty = true;
            measuredWidth = getDefaultWidth();
            measuredHeight = (int) getScaledSize(fontSize) + 20;
        }
    }

    private int getDefaultWidth() {
        if (customWidth > 0) {
            int width = (int) getScaledSize(customWidth);
            Log.d(TAG, "Using custom width: " + width);
            return width;
        }

        // Calculate width based on text
        if (text == null || text.isEmpty()) {
            return 100; // Minimum default width when no text
        }

        String[] lines = text.split("\n");
        float maxLineWidth = 0;

        for (String line : lines) {
            if (line != null && !line.isEmpty()) {
                float lineWidth = textPaint.measureText(line);
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
            }
        }

        // Add stroke width padding
        maxLineWidth += getScaledSize(strokeWidth) * 2;

        // Ensure minimum width
        int width = Math.max(100, (int) Math.ceil(maxLineWidth));
        Log.d(TAG, "Calculated default width: " + width + " for text: '" + text + "'");
        return width;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = right - left;
        int height = bottom - top;
        Log.d(TAG, "onLayout - changed: " + changed + ", size: " + width + "x" + height + ", bounds: [" + left + "," + top + "," + right + "," + bottom + "]");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged - new: " + w + "x" + h + ", old: " + oldw + "x" + oldh);
        if (w > 0) {
            layoutDirty = true;
            createLayout(w);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure called!!!!");

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "MeasureSpec - width: " + getModeString(widthMode) + " " + widthSize + ", height: " + getModeString(heightMode) + " " + heightSize);

        // Calculate our desired dimensions
        int defaultWidth = getDefaultWidth();

        // Create layout with default width to get proper dimensions
        if (layoutDirty || textLayout == null) {
            createLayout(defaultWidth);
        }

        // Use our measured dimensions, ignoring React Native's 0 constraints
        int width = measuredWidth;
        int height = measuredHeight;

        // Only respect non-zero constraints
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            width = widthSize;
            // Recreate layout with new width
            createLayout(width);
            height = measuredHeight;
        } else if (widthMode == MeasureSpec.AT_MOST && widthSize > 0) {
            width = Math.min(measuredWidth, widthSize);
            if (width != measuredWidth) {
                createLayout(width);
                height = measuredHeight;
            }
        }

        if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST && heightSize > 0) {
            height = Math.min(height, heightSize);
        }

        // Ensure minimum dimensions
        float strokePadding = getScaledSize(strokeWidth) * 2;
        int minWidth = (int) (getScaledSize(fontSize) * 2 + strokePadding);
        int minHeight = (int) (getScaledSize(fontSize) + strokePadding);
        width = Math.max(minWidth, width);
        height = Math.max(minHeight, height);

        Log.d(TAG, "setMeasuredDimension: " + width + "x" + height);
        setMeasuredDimension(width, height);

        // Force React Native to respect our size
        post(new Runnable() {
            @Override
            public void run() {
                if (getWidth() == 0 || getHeight() == 0) {
                    Log.d(TAG, "View still has 0 size after measure, requesting layout again");
                    requestLayout();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw called - view size: " + getWidth() + "x" + getHeight() + ", visibility: " + getVisibility() + ", text: '" + text + "'");

        if (text == null || text.isEmpty()) {
            Log.d(TAG, "No text to draw, skipping");
            return;
        }

        // If view has 0x0 size, request layout
        if (getWidth() == 0 || getHeight() == 0) {
            Log.d(TAG, "View has 0x0 size, requesting layout");
            requestLayout();
            return;
        }

        if (textLayout == null || strokeLayout == null) {
            Log.d(TAG, "Layout is null, creating...");
            createLayout(getWidth() > 0 ? getWidth() : getDefaultWidth());
        }

        if (strokeLayout != null && textLayout != null) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            // Account for stroke padding
            float strokePadding = getScaledSize(strokeWidth);

            float x = strokePadding;
            float y = strokePadding;

            if (alignment == Layout.Alignment.ALIGN_CENTER) {
                x = Math.max(strokePadding, (viewWidth - textLayout.getWidth()) / 2.0f);
            } else if (alignment == Layout.Alignment.ALIGN_OPPOSITE) {
                x = Math.max(strokePadding, viewWidth - textLayout.getWidth() - strokePadding);
            }

            // Vertically center
            y = Math.max(strokePadding, (viewHeight - textLayout.getHeight()) / 2.0f);

            Log.d(TAG, "Drawing at position: (" + x + ", " + y + "), strokePadding: " + strokePadding);

            canvas.save();
            canvas.translate(x, y);

            // Draw stroke first, then text on top
            strokeLayout.draw(canvas);
            textLayout.draw(canvas);

            canvas.restore();

            Log.d(TAG, "Draw complete");
        } else {
            Log.e(TAG, "Cannot draw - layouts are null!");
        }
    }

    private String getModeString(int mode) {
        switch (mode) {
            case MeasureSpec.EXACTLY: return "EXACTLY";
            case MeasureSpec.AT_MOST: return "AT_MOST";
            case MeasureSpec.UNSPECIFIED: return "UNSPECIFIED";
            default: return "UNKNOWN";
        }
    }

    private float getScaledSize(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics());
    }

    public void setText(String text) {
        Log.d(TAG, "setText called: '" + text + "'");
        if (text == null) text = "";
        if (!this.text.equals(text)) {
            this.text = text;
            layoutDirty = true;
            requestLayout();
            invalidate();
        }
    }

    public void setFontSize(float fontSize) {
        Log.d(TAG, "setFontSize called: " + fontSize);
        if (this.fontSize != fontSize) {
            this.fontSize = fontSize;
            layoutDirty = true;
            requestLayout();
            invalidate();
        }
    }

    public void setTextColor(String color) {
        Log.d(TAG, "setTextColor called: " + color);
        try {
            int parsedColor = parseColor(color != null ? color : "#000000");
            if (this.textColor != parsedColor) {
                this.textColor = parsedColor;
                invalidate();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing text color", e);
            this.textColor = 0xFF000000;
            invalidate();
        }
    }

    public void setStrokeColor(String color) {
        Log.d(TAG, "setStrokeColor called: " + color);
        try {
            int parsedColor = parseColor(color != null ? color : "#FFFFFF");
            if (this.strokeColor != parsedColor) {
                this.strokeColor = parsedColor;
                invalidate();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing stroke color", e);
            this.strokeColor = 0xFFFFFFFF;
            invalidate();
        }
    }

    public void setStrokeWidth(float strokeWidth) {
        Log.d(TAG, "setStrokeWidth called: " + strokeWidth);
        if (this.strokeWidth != strokeWidth) {
            this.strokeWidth = strokeWidth;
            layoutDirty = true;
            requestLayout();
            invalidate();
        }
    }

    public void setFontFamily(String fontFamily) {
        Log.d(TAG, "setFontFamily called: " + fontFamily);
        if (fontFamily == null) fontFamily = "sans-serif";
        if (!this.fontFamily.equals(fontFamily)) {
            this.fontFamily = fontFamily;
            layoutDirty = true;
            requestLayout();
            invalidate();
        }
    }

    public void setTextAlignment(String alignment) {
        Log.d(TAG, "setTextAlignment called: " + alignment);
        Layout.Alignment newAlignment = Layout.Alignment.ALIGN_CENTER;
        if ("left".equals(alignment)) {
            newAlignment = Layout.Alignment.ALIGN_NORMAL;
        } else if ("right".equals(alignment)) {
            newAlignment = Layout.Alignment.ALIGN_OPPOSITE;
        } else if ("center".equals(alignment)) {
            newAlignment = Layout.Alignment.ALIGN_CENTER;
        }

        if (this.alignment != newAlignment) {
            this.alignment = newAlignment;
            layoutDirty = true;
            invalidate();
        }
    }

    public void setNumberOfLines(int numberOfLines) {
        Log.d(TAG, "setNumberOfLines called: " + numberOfLines);
        if (this.numberOfLines != numberOfLines) {
            this.numberOfLines = numberOfLines;
            layoutDirty = true;
            requestLayout();
            invalidate();
        }
    }

    public void setEllipsis(boolean ellipsis) {
        Log.d(TAG, "setEllipsis called: " + ellipsis);
        if (this.ellipsis != ellipsis) {
            this.ellipsis = ellipsis;
            layoutDirty = true;
            requestLayout();
            invalidate();
        }
    }

    public void setCustomWidth(float width) {
        Log.d(TAG, "setCustomWidth called: " + width);
        if (this.customWidth != width) {
            this.customWidth = width;
            layoutDirty = true;
            requestLayout();
            invalidate();
        }
    }

    private int parseColor(String color) {
        if (color == null || color.isEmpty()) {
            return 0xFF000000;
        }
        try {
            if (color.startsWith("#")) {
                return Color.parseColor(color);
            } else if (color.startsWith("rgb")) {
                return parseRgbColor(color);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error parsing color: " + color, e);
            return 0xFF000000;
        }
        return 0xFF000000;
    }

    private int parseRgbColor(String color) {
        try {
            String[] parts = color.replaceAll("[rgba()\\s]", "").split(",");
            if (parts.length < 3) return 0xFF000000;

            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            int a = parts.length > 3 ? (int) (Float.parseFloat(parts[3].trim()) * 255) : 255;

            return Color.argb(a, r, g, b);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RGB color: " + color, e);
            return 0xFF000000;
        }
    }

    private Typeface getFont(String fontFamily) {
        if (fontCache.containsKey(fontFamily)) {
            return fontCache.get(fontFamily);
        }
        Typeface typeface = FontUtil.getFont(getContext(), fontFamily);
        fontCache.put(fontFamily, typeface);
        Log.d(TAG, "Loaded font: " + fontFamily + ", typeface: " + (typeface != null));
        return typeface;
    }
}
