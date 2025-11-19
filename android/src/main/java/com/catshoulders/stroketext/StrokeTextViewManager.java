package com.catshoulders.stroketext;

import android.util.Log;
import androidx.annotation.Nullable;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.LayoutShadowNode;

public class StrokeTextViewManager extends SimpleViewManager<StrokeTextView> {
    public static final String REACT_CLASS = "StrokeTextView";
    private static final String TAG = "StrokeTextViewManager";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public StrokeTextView createViewInstance(ThemedReactContext reactContext) {
        Log.d(TAG, "createViewInstance called");
        StrokeTextView view = new StrokeTextView(reactContext);
        return view;
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        Log.d(TAG, "createShadowNodeInstance called");
        return new StrokeTextShadowNode();
    }

    @Override
    public Class<LayoutShadowNode> getShadowNodeClass() {
        return (Class) StrokeTextShadowNode.class;
    }

    @ReactProp(name = "text")
    public void setText(StrokeTextView view, @Nullable String text) {
        if (text == null) {
            text = "";
        }
        view.setText(text);
        view.requestLayout();
    }

    @ReactProp(name = "fontSize", defaultFloat = 14f)
    public void setFontSize(StrokeTextView view, float fontSize) {
        view.setFontSize(fontSize);
        view.requestLayout();
    }

    @ReactProp(name = "color")
    public void setColor(StrokeTextView view, @Nullable String color) {
        if (color == null) {
            color = "#000000";
        }
        view.setTextColor(color);
    }

    @ReactProp(name = "strokeColor")
    public void setStrokeColor(StrokeTextView view, @Nullable String strokeColor) {
        if (strokeColor == null) {
            strokeColor = "#FFFFFF";
        }
        view.setStrokeColor(strokeColor);
    }

    @ReactProp(name = "strokeWidth", defaultFloat = 1f)
    public void setStrokeWidth(StrokeTextView view, float strokeWidth) {
        view.setStrokeWidth(strokeWidth);
        view.requestLayout();
    }

    @ReactProp(name = "fontFamily")
    public void setFontFamily(StrokeTextView view, @Nullable String fontFamily) {
        if (fontFamily == null) {
            fontFamily = "sans-serif";
        }
        view.setFontFamily(fontFamily);
        view.requestLayout();
    }

    @ReactProp(name = "align")
    public void setTextAlignment(StrokeTextView view, @Nullable String align) {
        if (align == null) {
            align = "center";
        }
        view.setTextAlignment(align);
    }

    @ReactProp(name = "numberOfLines", defaultInt = 0)
    public void setNumberOfLines(StrokeTextView view, int numberOfLines) {
        view.setNumberOfLines(numberOfLines);
        view.requestLayout();
    }

    @ReactProp(name = "ellipsis", defaultBoolean = false)
    public void setEllipsis(StrokeTextView view, boolean ellipsis) {
        view.setEllipsis(ellipsis);
        view.requestLayout();
    }

    @ReactProp(name = "width", defaultFloat = 0f)
    public void setWidth(StrokeTextView view, float width) {
        view.setCustomWidth(width);
        view.requestLayout();
    }
}

