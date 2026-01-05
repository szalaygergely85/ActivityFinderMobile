package com.gege.activityfindermobile.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom RecyclerView that properly measures its content when inside a NestedScrollView
 * by calculating the total height of all items
 */
public class WrapContentRecyclerView extends RecyclerView {

    public WrapContentRecyclerView(@NonNull Context context) {
        super(context);
    }

    public WrapContentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapContentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        android.util.Log.d("WrapContentRV", "onMeasure called - heightSpec mode: " +
                MeasureSpec.getMode(heightSpec) + ", size: " + MeasureSpec.getSize(heightSpec) +
                ", adapter: " + (getAdapter() != null ? getAdapter().getItemCount() + " items" : "null"));

        if (getAdapter() != null) {
            int itemCount = getAdapter().getItemCount();

            if (itemCount > 0) {
                // Measure the first item to get an estimate of item height
                // Then calculate total height for all items
                LayoutManager layoutManager = getLayoutManager();

                if (layoutManager != null) {
                    // Let the layout manager measure
                    super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                    int totalHeight = 0;
                    int itemWidth = MeasureSpec.getSize(widthSpec);

                    // Measure all items
                    for (int i = 0; i < itemCount; i++) {
                        View child = layoutManager.findViewByPosition(i);
                        if (child == null) {
                            // Create a view for measurement
                            child = getAdapter().createViewHolder(this, getAdapter().getItemViewType(i)).itemView;
                        }

                        if (child != null) {
                            // Measure the child
                            child.measure(
                                MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                            );

                            totalHeight += child.getMeasuredHeight();

                            // Add margin
                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                            if (lp != null) {
                                totalHeight += lp.topMargin + lp.bottomMargin;
                            }
                        }
                    }

                    android.util.Log.d("WrapContentRV", "Calculated total height: " + totalHeight + " for " + itemCount + " items");

                    // Set the calculated height
                    setMeasuredDimension(
                        MeasureSpec.getSize(widthSpec),
                        totalHeight + getPaddingTop() + getPaddingBottom()
                    );
                    return;
                }
            }
        }

        // Fallback to default measurement with expanded spec
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, expandSpec);

        android.util.Log.d("WrapContentRV", "Final measured height: " + getMeasuredHeight() +
                ", child count: " + getChildCount());
    }
}
