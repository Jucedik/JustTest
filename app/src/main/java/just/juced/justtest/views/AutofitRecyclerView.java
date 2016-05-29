package just.juced.justtest.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

import just.juced.justtest.R;

/**
 * Created by juced on 12.05.2015.
 */
public class AutofitRecyclerView extends RecyclerView {
    private boolean mScrollable;
    private GridLayoutManager manager;
    private int columnWidth = -1;
    private boolean onLoadAnimated = false;

    public void setOnLoadAnimated(boolean onLoadAnimated) {
        this.onLoadAnimated = onLoadAnimated;

        if (onLoadAnimated) {
            mScrollable = true;
        }
    }

    public AutofitRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public AutofitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutofitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        mScrollable = false;
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {
                    android.R.attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }

        manager = new GridLayoutManager(getContext(), 1);
        setLayoutManager(manager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (columnWidth > 0) {
            int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
            manager.setSpanCount(spanCount);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !mScrollable || super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!onLoadAnimated) {
            onLoadAnimated = true;
            for (int i = 0; i < getChildCount(); i++) {
                animate(getChildAt(i), i);

                if (i == getChildCount() - 1) {
                    getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScrollable = true;
                        }
                    }, i * 100);
                }
            }
        }
    }

    private void animate(View view, final int pos) {
        view.animate().cancel();
        view.setTranslationY(100);
        view.setAlpha(0);
        view.animate().alpha(1.0f).translationY(0).setDuration(300).setStartDelay(pos * 100);
    }

}
