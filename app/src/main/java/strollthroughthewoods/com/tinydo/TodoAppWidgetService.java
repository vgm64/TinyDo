package strollthroughthewoods.com.tinydo;


import android.content.Intent;
import android.widget.RemoteViewsService;

public class TodoAppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodoAppRemoteViewsFactory(
                this.getApplicationContext(),
                intent
        );
    }
}
