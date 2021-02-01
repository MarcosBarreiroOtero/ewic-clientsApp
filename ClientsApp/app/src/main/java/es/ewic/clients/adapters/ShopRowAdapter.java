package es.ewic.clients.adapters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ramijemli.percentagechartview.PercentageChartView;

import java.util.List;

import es.ewic.clients.R;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.FormUtils;
import es.ewic.clients.utils.ImageUtils;
import es.ewic.clients.utils.RequestUtils;

public class ShopRowAdapter extends BaseAdapter implements ListAdapter {

    private final List<Shop> shopList;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

    public ShopRowAdapter(Fragment fragment, List<Shop> shopList, Resources resources, String packageName) {
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
            return shopList.size();
    }

    @Override
    public Shop getItem(int position) {
        if (null == shopList) {
            return null;
        } else
            return shopList.get(position);
    }

    @Override
    public long getItemId(int position) {
        Shop shop = getItem(position);
        return shop.getIdShop();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Siempre se sobreescribe la view por culpa del percentchartview
        convertView = fragment.getLayoutInflater().inflate(R.layout.shop_row, null);

        TextView shopTitle = convertView.findViewById(R.id.shop_title);
        TextView shopLocation = convertView.findViewById(R.id.shop_location);
        TextView shopOpen = convertView.findViewById(R.id.shop_open);

        Shop shop_data = getItem(position);

        String type = resources.getString(resources.getIdentifier(shop_data.getType(), "string", packageName));
        if (shop_data != null) {
            shopTitle.setText(type + " - " + shop_data.getName());
            shopLocation.setText(shop_data.getLocation());

            if (shop_data.isAllowEntries()) {
                shopOpen.setText(resources.getString(R.string.open));
                shopOpen.setTextColor(resources.getColor(R.color.semaphore_green));
            } else {
                shopOpen.setText(resources.getString(R.string.close));
                shopOpen.setTextColor(resources.getColor(R.color.semaphore_red));
            }

            PercentageChartView percentageChartView = convertView.findViewById(R.id.shop_percentage);
            float percentage = ((float) shop_data.getActualCapacity() / shop_data.getMaxCapacity()) * 100;
            FormUtils.configureSemaphorePercentageChartView(resources, percentageChartView, percentage);


            ImageView image = convertView.findViewById(R.id.shop_image);
            getShopImage(shop_data, image);
        }


        Animation animation = AnimationUtils.loadAnimation(convertView.getContext(), R.anim.slide_in);
        animation.setDuration(500 + (position * 100));
        convertView.startAnimation(animation);

        return convertView;
    }

    private void getShopImage(Shop shopData, ImageView image) {
        String url = BackEndEndpoints.CONFIGURATION_IMAGE(shopData.getIdShop());
        RequestUtils.sendStringRequest(fragment.getContext(), Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("HTTP", "ok " + response.length());
                if (!response.isEmpty()) {
                    String base64 = response;
                    Bitmap map = ImageUtils.convert(base64);
                    image.setImageBitmap(map);
                } else {
                    image.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "ok");
                image.setVisibility(View.GONE);
            }
        });
    }

}
