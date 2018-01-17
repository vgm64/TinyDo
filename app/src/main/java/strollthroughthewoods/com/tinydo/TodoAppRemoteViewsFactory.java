package strollthroughthewoods.com.tinydo;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class TodoAppRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context ctxt = null;
    private int appWidgetId;

    public TodoAppRemoteViewsFactory(Context ctxt, Intent intent) {
        this.ctxt = ctxt;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        // no-op
    }

    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        return TodoistQueryingIntentService.todoItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // My layout
        RemoteViews row = new RemoteViews(ctxt.getPackageName(), R.layout.todo_item);
        row.setTextViewText(android.R.id.text1, TodoistQueryingIntentService.todoItems.get(position));
        return (row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return (null);
    }

    @Override
    public int getViewTypeCount() {
        return (1);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public boolean hasStableIds() {
        return (true);
    }

    @Override
    public void onDataSetChanged() {
        // no-op
    }
}
