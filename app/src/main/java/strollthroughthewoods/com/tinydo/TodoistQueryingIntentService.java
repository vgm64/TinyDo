package strollthroughthewoods.com.tinydo;


import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class TodoistQueryingIntentService extends IntentService {
    public static final String SEND_BACK_INTENT_ACTION = "SendBackIntentAction";
    public static List<String> todoItems = new ArrayList<>();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TodoistQueryingIntentService(String name) {
        super(name);
    }

    public TodoistQueryingIntentService() {
        super("TodoistQueryingIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("IntentService", "onHandleIntent");
        Log.i("IntentService", "ACTION:" + (intent.getAction() != null ? intent.getAction() : "No action"));
        intent.getExtras().keySet().stream()
                .forEach(key -> {
                    Log.i("IntentService", "  " + key + ":: " + intent.getExtras().get(key));
                });


        // This is some ugly error propagation. Would have been trivial to notify the user
        // if I had used the broadcast back to the widget provider.
        String postJsonString = doPostRequest();
        if (postJsonString == null) {
            todoItems = new ArrayList<>();
            todoItems.add("Network error.");
        }
        else {
            JSONObject jsonObjectPost = parseJSON(postJsonString);
            Map<String, Object> jsonMap = jsonToMap(jsonObjectPost);
            jsonMap.entrySet().stream()
                    .forEach(entity -> {
                                Log.w("jsonMap", entity.getKey() + " --> " + entity.getValue());
                            }
                    );
            todoItems = getTodoItemsFromJson(jsonObjectPost);
        }



        Context context = getApplicationContext();
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

        // Set on RemoteViews of the list
        Intent listViewIntent = new Intent(context, WidgetRemoteViewsService.class);
        listViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        listViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {appWidgetId});
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.todo_app_widget);
        remoteViews.setTextViewText(R.id.appwidget_text, "TACOS in TQIS");
        remoteViews.setRemoteAdapter(R.id.appwidget_list, listViewIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, TodoAppWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.appwidget_list);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        /* Send back a message to the TodoAppProvider */
        Intent sendBackIntent = new Intent(SEND_BACK_INTENT_ACTION);
        // Super useful.
        // sendBackIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        sendBackIntent.putExtra("resultsContent", "I did it!!");
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(sendBackIntent);

        // Debug IntentReceivers
        PackageManager pm = context.getPackageManager();
        Log.w("Query", "Number of receivers: " + pm.queryBroadcastReceivers(sendBackIntent, 0).size());
    }

    private List<String> getTodoItemsFromJson(JSONObject jsonObjectPost) {
        List<String> todoItems = new ArrayList<>();
        try {
            JSONArray items = jsonObjectPost.getJSONArray("items");
            for(int i=0; i<items.length(); i++) {
                String content = items.getJSONObject(i).getString("content");
                Log.w("JSON todo item:", content);
                todoItems.add(content);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return todoItems;
    }

    private String doPostRequest() {
        String result = null;
        int status = 0;
        try {
            URL url = new URL("https://todoist.com/api/v7/sync");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            con.setRequestMethod("POST");

            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("token", TodoistAPIKeyHolder.API_KEY);
            paramsMap.put("sync_token", "'*'");
            paramsMap.put("resource_types", "[\"items\"]");
            String paramsString = getParamsString(paramsMap);

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(paramsString);
            out.flush();
            out.close();

            status = con.getResponseCode();
            if (status != 200) {
                con.disconnect();
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            result = content.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("HTTP status code", String.valueOf(status));
            return result;
        }
        return result;
    }

    private JSONObject parseJSON(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            Log.i("JSON", json.toString());
            return json;
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "JSONException in parseJSON", Toast.LENGTH_SHORT);
            e.printStackTrace();
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "NullPointerException in parseJSON", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
        return null;
    }

    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
    public static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> retMap = new HashMap<>();

        if(json != JSONObject.NULL) {
            try {
                retMap = toMap(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
