package es.ewic.clients.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.core.graphics.ColorUtils;

import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;
import com.ramijemli.percentagechartview.renderer.RingModeRenderer;

import es.ewic.clients.R;

public class FormUtils {

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static ProgressDialog showProgressDialog(Context contex, Resources resources, int title, int message) {
        ProgressDialog pd = new ProgressDialog(contex);
        pd.setTitle(resources.getString(title));
        pd.setMessage(resources.getString(message));
        pd.setCancelable(false);
        pd.show();
        return pd;
    }

    public static void configureSemaphorePercentageChartView(Resources resources, PercentageChartView percentageChartView, float percentage) {

        if (percentageChartView != null) {
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
}
