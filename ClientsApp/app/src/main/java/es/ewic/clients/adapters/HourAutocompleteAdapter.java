package es.ewic.clients.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import es.ewic.clients.R;

public class HourAutocompleteAdapter extends BaseAdapter implements Filterable {

    private List<String> hoursList;
    private List<String> mOriginalValues; // Original Values
    private final Fragment fragment;

    public HourAutocompleteAdapter(List<String> hoursList, Fragment fragment) {
        assert hoursList != null;
        assert fragment != null;

        this.hoursList = hoursList;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return hoursList.size();
    }

    @Override
    public String getItem(int position) {
        return hoursList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = fragment.getLayoutInflater().inflate(R.layout.shop_list_item, null);
        }
        String hour = getItem(position);
        TextView shop_item_list_name = convertView.findViewById(R.id.shop_list_item_name);
        shop_item_list_name.setText(hour);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> FilteredArrList = new ArrayList<>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<>(hoursList);
                }
                if (constraint == null || constraint.length() == 0) {
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    boolean showAll = false;
                    for (String hour : mOriginalValues) {
                        if (hour.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            FilteredArrList.add(hour);
                            if (hour.toLowerCase().equals(constraint.toString().toLowerCase())) {
                                showAll = true;
                            }
                        }
                    }
                    if (showAll) {
                        results.count = mOriginalValues.size();
                        results.values = mOriginalValues;
                    } else {
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                hoursList = (List<String>) results.values; // has the filtered values
                notifyDataSetChanged();
            }
        };
    }
}
