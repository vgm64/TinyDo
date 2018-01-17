package strollthroughthewoods.com.tinydo;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodoAppRemoteViewsFactory(
                this.getApplicationContext(),
                intent
        );
    }
}
