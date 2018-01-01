package strollthroughthewoods.com.tinydo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.net.URI;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class TodoAppWidgetProvider extends AppWidgetProvider {

    private static final String CURRENT_WIDGET_ID = "CURRENT_WIDGET_ID";
    public static String EXTRA_WORD = "BALLS";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("OnlyOneUpdating", "ACTION:" + (intent.getAction() != null ? intent.getAction() : "No action"));
        Log.d("OnlyOneUpdating", "ID-" + String.valueOf(intent.getExtras().getInt(CURRENT_WIDGET_ID, -1)));
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, int[] appWidgetIds) {


        CharSequence widgetText = context.getString(R.string.appwidget_text);

        // Construct the RemoteViews object
        String number = String.format("%03d", (new Random().nextInt(900) + 100));
        Log.d("OnlyOneUpdating", number + " " + String.valueOf(appWidgetId));


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.todo_app_widget);
        remoteViews.setTextViewText(R.id.appwidget_text, widgetText + " " + number);


        Intent intent = new Intent(context, TodoAppWidgetProvider.class);
//        intent.setAction("MAKE_UPDATE");
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.putExtra(CURRENT_WIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        appWidgetId, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

        // Try a listview thingy.
        //remoteViews.setRemoteAdapter(R.layout.todo_app_widget, intent);
//        intent.setData(Uri.parse("file:///tmp/data.txt"));
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        remoteViews.setRemoteAdapter(R.id.appwidget_list, intent);

        String[] values = new String[] {"one", "two"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d("OnlyOneUpdating", "onUpdate");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

