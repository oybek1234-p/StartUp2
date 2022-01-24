package com.example.market.recycler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.market.MyApplication;
import com.example.market.R;
import com.example.market.utils.AndroidUtilities;
import com.example.market.utils.LogUtilsKt;

import org.jetbrains.annotations.NotNull;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class EmptyRecyclerView extends RecyclerView {
    private boolean isEmpty = false;
    private StaticLayout emptyTextView;
    private static TextPaint paint;
    private Drawable emptyDrawable;
    private String text;

    public EmptyRecyclerView(@NonNull @NotNull Context context) {
        super(context);
    }

    public void setEmptyData(String text,int drawableId){
        this.text = text;

        if (drawableId!=0) {
            try {
                emptyDrawable = getContext().getDrawable(drawableId);
            }catch (Exception e){
                LogUtilsKt.log(e.getMessage());
            }
        }
        setEmpty(true);
    }

    public EmptyRecyclerView(@NonNull @NotNull Context context,AttributeSet attributeSet) {
        super(context,attributeSet);
    }
    public EmptyRecyclerView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs,0);
    }

    public void setEmpty(boolean isEmpty){
        if (emptyTextView!=null&&emptyTextView.getText() == text&&this.isEmpty == isEmpty) {
            return;
        }

        this.isEmpty = isEmpty;

        if (paint==null){
            paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.GRAY);
            paint.setTextSize(AndroidUtilities.dp(15));
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.DEFAULT);
        }
        if (text==null) {
            text = "Empty";
        }

        emptyTextView = new StaticLayout(text,paint, MyApplication.displaySize.getFirst() - AndroidUtilities.dp(56f), Layout.Alignment.ALIGN_CENTER,1.0F,0.0f,false);
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (isEmpty){
            if (emptyDrawable!=null) {
                int y = getMeasuredHeight()/2 - emptyDrawable.getIntrinsicHeight() - emptyTextView.getHeight() - AndroidUtilities.dp(8f);
                int x = getMeasuredWidth() /2 - emptyDrawable.getIntrinsicWidth()/2;
                emptyDrawable.setBounds(x,y,x+emptyDrawable.getIntrinsicWidth(),y+emptyDrawable.getIntrinsicHeight());
                emptyDrawable.draw(c);
            }
            c.save();
            c.translate(getMeasuredWidth()/2f - emptyTextView.getWidth()/2f,getMeasuredHeight()/2f);
            emptyTextView.draw(c);
            c.restore();
        }
    }
}
