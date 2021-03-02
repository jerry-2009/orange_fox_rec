package com.fordownloads.orangefox.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import org.jetbrains.annotations.NotNull;

public class OverScrollBehavior extends CoordinatorLayout.Behavior<View> {
        private int mOverScrollY;

        public OverScrollBehavior() {
        }

        public OverScrollBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onStartNestedScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull View child, @NotNull View directTargetChild, @NotNull View target, int nestedScrollAxes, int what) {
            mOverScrollY = 0;
            return true;
        }

        @Override
        public void onNestedScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull View child, @NotNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int wtf, @NotNull int[] what) {
            if (dyUnconsumed == 0) {
                return;
            }

            mOverScrollY -= dyUnconsumed;
            final ViewGroup group = (ViewGroup) target;
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = group.getChildAt(i);
                view.setTranslationY(mOverScrollY);
            }
        }

        @Override
        public void onStopNestedScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull View child, @NotNull View target, int what) {
            final ViewGroup group = (ViewGroup) target;
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = group.getChildAt(i);
                ViewCompat.animate(view).translationY(0).start();
            }
        }
    }
