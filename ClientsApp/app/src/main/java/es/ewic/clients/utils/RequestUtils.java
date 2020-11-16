package es.ewic.clients.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class RequestUtils {


    public static void sendJsonArrayRequest(Context context, int mehod, String url, JSONArray jsonRequest,
                                            Response.Listener<org.json.JSONArray> listener, Response.ErrorListener errorListener) {

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mehod, url, jsonRequest, listener, errorListener);

        queue.add(jsonArrayRequest);


    }
}
