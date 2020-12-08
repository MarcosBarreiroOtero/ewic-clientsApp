package es.ewic.clients.utils;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import es.ewic.clients.R;

public class RequestUtils {

    //Error messages
    public static final String RESERVATION_WHEN_SHOP_FULL = "Reservation when shop full";
    public static final String MOVE_RESERVATION_TO_PAST = "Move reservation to past";

    public static void sendJsonArrayRequest(Context context, int mehod, String url, JSONArray jsonRequest,
                                            Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mehod, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        queue.add(jsonArrayRequest);
    }

    public static void sendJsonObjectRequest(Context context, int method, String url, JSONObject jsonRequest,
                                             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public static void sendStringRequest(Context context, int method, String url, Response.Listener<String> listener,
                                         Response.ErrorListener errorListener) {

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(method, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public static void showSnackbarNoConnectionServer(View v, Resources resources, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(v, resources.getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, listener);
        snackbar.show();
    }

    public static String getErrorMessageRequest(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            return data.getString("message");
        } catch (UnsupportedEncodingException e) {
        } catch (JSONException e) {
        }
        return null;
    }

    public static int getErrorCodeRequest(VolleyError error) {
        return error.networkResponse.statusCode;
    }
}
