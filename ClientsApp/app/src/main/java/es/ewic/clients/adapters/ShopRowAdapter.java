package es.ewic.clients.adapters;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.callback.OnProgressChangeListener;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import es.ewic.clients.R;

public class ShopRowAdapter extends BaseAdapter implements ListAdapter {

    private final JSONArray shopList;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

    public ShopRowAdapter(Fragment fragment, JSONArray shopList, Resources resources, String packageName) {


        assert fragment != null;
        assert shopList != null;
        assert resources != null;
        assert packageName != null;

        this.fragment = fragment;
        this.shopList = shopList;
        this.resources = resources;
        this.packageName = packageName;
    }

    @Override
    public int getCount() {
        if (null == shopList)
            return 0;
        else
            return shopList.length();
    }

    @Override
    public JSONObject getItem(int position) {
        if (null == shopList) {
            return null;
        } else
            return shopList.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        JSONObject jsonObject = getItem(position);
        return jsonObject.optInt("idShop");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = fragment.getLayoutInflater().inflate(R.layout.shop_row, null);

        TextView shopTitle = convertView.findViewById(R.id.shop_title);
        TextView shopLocation = convertView.findViewById(R.id.shop_location);

        JSONObject shop_data = getItem(position);

        String type = resources.getString(resources.getIdentifier(shop_data.optString("type"), "string", packageName));
        if (shop_data != null) {
            shopTitle.setText(type + " - " + shop_data.optString("name"));
            shopLocation.setText(shop_data.optString("location"));
        }

        PercentageChartView percentageChartView = convertView.findViewById(R.id.shop_percentage);

        float percentage = (float) ((shop_data.optDouble("actualCapacity") / shop_data.optDouble("maxCapacity")) * 100);
        percentageChartView.setProgress(percentage, false);

        AdaptiveColorProvider colorProvider = new AdaptiveColorProvider() {
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
        };

        percentageChartView.setAdaptiveColorProvider(colorProvider);

        return convertView;
    }

}
