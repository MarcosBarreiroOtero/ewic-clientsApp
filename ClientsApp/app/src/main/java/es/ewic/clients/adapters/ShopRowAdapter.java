package es.ewic.clients.adapters;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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
        TextView shop_capacity_status_text = convertView.findViewById(R.id.shop_capacity_status_text);

        ImageView shop_capacity_status = convertView.findViewById(R.id.shop_capacity_status);

        JSONObject shop_data = getItem(position);

        String type = resources.getString(resources.getIdentifier(shop_data.optString("type"), "string", packageName));
        if (shop_data != null) {
            shopTitle.setText(type + " - " + shop_data.optString("name"));
            shopLocation.setText(shop_data.optString("location"));
            shop_capacity_status_text.setText(shop_data.optInt("actualCapacity") + "/" + shop_data.optInt("maxCapacity"));

            shop_capacity_status.setColorFilter(R.color.purple_200);
        }

        return convertView;
    }

}
