package strollthroughthewoods.com.tinydo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class TodoAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter(TodoistQueryingIntentService.SEND_BACK_INTENT_ACTION);
        localBroadcastManager.registerReceiver(this, intentFilter);

        Log.w("TodoAppWidgetProvider:onReceive():", "ACTION:" + (intent.getAction() != null ? intent.getAction() : "No action"));
        if (intent.getExtras() != null) {
            intent.getExtras().keySet().stream()
                    .forEach(key -> {
                        Log.i("    TodoAppWidgetProvider - ", key + ":: " + intent.getExtras().get(key));
                    });
        }
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, int[] appWidgetIds) {


        Log.i("TodoAppWidgetProvider", "updateAppWidget");

        Intent intentServiceIntent = new Intent(context, TodoistQueryingIntentService.class);
        intentServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intentServiceIntent.setData(Uri.parse(intentServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        context.startService(intentServiceIntent);

        CharSequence widgetText = context.getString(R.string.appwidget_text);

        // Construct the RemoteViews object
        String number = String.format("%03d", (new Random().nextInt(900) + 100));

        // Number refresh works.
        Intent clickIntent = new Intent(context, TodoAppWidgetProvider.class);
        clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingClickIntent = PendingIntent.getBroadcast(context,
                appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Fill in the list view with data from RemoteViewsService and
        Intent listViewIntent = new Intent(context, WidgetRemoteViewsService.class);
        listViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        listViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        // Set on RemoteViews of the list
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.todo_app_widget);
        remoteViews.setTextViewText(R.id.appwidget_text, widgetText + " " + number);
        remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingClickIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
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

