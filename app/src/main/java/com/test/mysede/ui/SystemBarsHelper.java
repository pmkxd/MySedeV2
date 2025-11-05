package com.test.mysede.ui;

import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Utilidad para aplicar padding dinámico basado en las barras del sistema.
 */
public final class SystemBarsHelper {

    private SystemBarsHelper() {
        // Utility class
    }

    public static void applyEdgeToEdge(AppCompatActivity activity, int rootViewId) {
        EdgeToEdge.enable(activity);

        View root = activity.findViewById(rootViewId);
        if (root == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        ViewCompat.requestApplyInsets(root);
    }
}