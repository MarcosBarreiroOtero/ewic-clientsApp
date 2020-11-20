package es.ewic.clients.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.core.graphics.ColorUtils;

import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;
import com.ramijemli.percentagechartview.renderer.RingModeRenderer;

import org.w3c.dom.Text;

import es.ewic.clients.R;

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

    public static void configureSemaphorePercentageChartView(Resources resources, PercentageChartView percentageChartView, float percentage) {

        percentageChartView.drawBackgroundEnabled(false)
                .drawBackgroundBarEnabled(true)
                .orientation(BaseModeRenderer.ORIENTATION_CLOCKWISE)
                .progressBarStyle(RingModeRenderer.CAP_SQUARE)
                .progressBarThickness(10)
                .startAngle(90)
                .textStyle(Typeface.BOLD)
                .backgroundBarColor(resources.getColor(R.color.purple_500)).apply();

        percentageChartView.setProgress(percentage, false);
        percentageChartView.setAdaptiveColorProvider(new AdaptiveColorProvider() {
            @Override
            public int provideProgressColor(float progress) {
                if (progress < 25) {
                    return resources.getColor(R.color.semaphore_green);
                } else if (progress < 100) {
                    return resources.getColor(R.color.semaphore_ambar);
                } else {
                    return resources.getColor(R.color.semaphore_red);
                }
            }

            @Override
            public int provideBackgroundColor(float progress) {
                //This will provide a bg color that is 80% darker than progress color.
                return ColorUtils.blendARGB(provideProgressColor(progress), Color.BLACK, .5f);
            }

            @Override
            public int provideTextColor(float progress) {
                return provideProgressColor(progress);
            }
        });
    }
}
