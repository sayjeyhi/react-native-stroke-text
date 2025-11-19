package com.catshoulders.stroketext;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;

import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.yoga.YogaMeasureFunction;
import com.facebook.yoga.YogaMeasureMode;
import com.facebook.yoga.YogaMeasureOutput;
import com.facebook.yoga.YogaNode;

public class StrokeTextShadowNode extends LayoutShadowNode implements YogaMeasureFunction {

    private static final String TAG = "StrokeTextShadowNode";

    private String text = "";
    private float fontSize = 14;
    private float strokeWidth = 1;
    private String fontFamily = "sans-serif";
    private int numberOfLines = 0;
    private float customWidth = 0;
    private TextPaint textPaint;

    public StrokeTextShadowNode() {
        super();
        Log.d(TAG, "Constructor called");
        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        setMeasureFunction(this);
        Log.d(TAG, "Constructor complete, measure function set");
    }

    @ReactProp(name = "text")
    public void setText(String text) {
        Log.d(TAG, "setText called: '" + text + "'");
        this.text = text != null ? text : "";
        markUpdated();
    }

    @ReactProp(name = "fontSize", defaultFloat = 14f)
    public void setFontSize(float fontSize) {
        Log.d(TAG, "setFontSize called: " + fontSize);
        this.fontSize = fontSize;
        markUpdated();
    }

    @ReactProp(name = "strokeWidth", defaultFloat = 1f)
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        markUpdated();
    }

    @ReactProp(name = "fontFamily")
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily != null ? fontFamily : "sans-serif";
        markUpdated();
    }

    @ReactProp(name = "numberOfLines", defaultInt = 0)
    public void setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
        markUpdated();
    }

    @ReactProp(name = "width", defaultFloat = 0f)
    public void setCustomWidth(float width) {
        this.customWidth = width;
        markUpdated();
    }

    @Override
    public long measure(
            YogaNode node,
            float width,
            YogaMeasureMode widthMode,
            float height,
            YogaMeasureMode heightMode) {

        Log.d(TAG, "measure called - width: " + width + " (" + widthMode + "), height: " + height + " (" + heightMode + "), text: '" + text + "'");

        // Get scaled size
        float scaledFontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            fontSize,
            getThemedContext().getResources().getDisplayMetrics()
        );

        float scaledStrokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            strokeWidth,
            getThemedContext().getResources().getDisplayMetrics()
        );

        // If text is empty, return minimal dimensions
        if (text == null || text.isEmpty()) {
            float strokePadding = scaledStrokeWidth * 2;
            int minWidth = (int) (scaledFontSize * 2 + strokePadding);
            int minHeight = (int) (scaledFontSize + strokePadding);
            return YogaMeasureOutput.make(minWidth, minHeight);
        }

        // Setup text paint
        textPaint.setTextSize(scaledFontSize);

        int measureWidth;

        // Determine width to use for layout
        if (widthMode == YogaMeasureMode.EXACTLY) {
            measureWidth = (int) width;
        } else {
            // Calculate default width based on text
            int defaultWidth;
            if (customWidth > 0) {
                defaultWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    customWidth,
                    getThemedContext().getResources().getDisplayMetrics()
                );
            } else {
                String[] lines = text.split("\n");
                float maxLineWidth = 0;
                for (String line : lines) {
                    if (line != null && !line.isEmpty()) {
                        float lineWidth = textPaint.measureText(line);
                        maxLineWidth = Math.max(maxLineWidth, lineWidth);
                    }
                }
                maxLineWidth += scaledStrokeWidth * 2;
                defaultWidth = Math.max(100, (int) Math.ceil(maxLineWidth));
            }

            if (widthMode == YogaMeasureMode.AT_MOST) {
                measureWidth = Math.min(defaultWidth, (int) width);
            } else {
                measureWidth = defaultWidth;
            }
        }

        // Create layout to measure height
        StaticLayout layout = new StaticLayout(
            text,
            textPaint,
            measureWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1.0f,
            0.0f,
            false
        );

        int finalWidth = layout.getWidth();
        int finalHeight = layout.getHeight();

        // Apply numberOfLines constraint
        if (numberOfLines > 0 && numberOfLines < layout.getLineCount()) {
            finalHeight = layout.getLineTop(numberOfLines);
        }

        // Add stroke padding
        float strokePadding = scaledStrokeWidth * 2;
        finalWidth = (int) Math.ceil(finalWidth + strokePadding);
        finalHeight = (int) Math.ceil(finalHeight + strokePadding);

        // Apply height constraints if specified
        if (heightMode == YogaMeasureMode.EXACTLY) {
            finalHeight = (int) height;
        } else if (heightMode == YogaMeasureMode.AT_MOST) {
            finalHeight = Math.min(finalHeight, (int) height);
        }

        // Ensure minimum dimensions
        int minWidth = (int) (scaledFontSize * 2 + strokePadding);
        int minHeight = (int) (scaledFontSize + strokePadding);
        finalWidth = Math.max(minWidth, finalWidth);
        finalHeight = Math.max(minHeight, finalHeight);

        Log.d(TAG, "measure returning: " + finalWidth + "x" + finalHeight);
        return YogaMeasureOutput.make(finalWidth, finalHeight);
    }
}
