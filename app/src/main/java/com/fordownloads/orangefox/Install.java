package com.fordownloads.orangefox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Interpolator;
import android.graphics.Point;
import android.view.Display;
import android.view.View;

import androidx.core.view.animation.PathInterpolatorCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class Install {

    public static void dialog(Activity activity) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity, R.style.ThemeBottomSheet);

        View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_install, null);
        dialog.setContentView(sheetView);
        dialog.setDismissWithAnimation(true);
        sheetView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(v -> dialog.getWindow().setWindowAnimations(0));

        sheetView.findViewById(R.id.btnInstall).setOnClickListener(v -> {

        });

        sheetView.findViewById(R.id.btnDownload).setOnClickListener(v -> {

        });

        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        sheetView.setY(size.y);
        dialog.show();

        sheetView.animate()
                .setInterpolator(vars.intr)
                .setDuration(600)
                .setStartDelay(200)
                .setStartDelay(100)
                .translationY(0);
    }
}
