package es.ewic.clients.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class FormUtils {

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static void showLoadingPanel(Window window, RelativeLayout loadingPanel) {
        loadingPanel.setVisibility(View.VISIBLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    public static void hideLoadingPanel(Window window, RelativeLayout loadingPanel) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loadingPanel.setVisibility(View.GONE);
    }
}
