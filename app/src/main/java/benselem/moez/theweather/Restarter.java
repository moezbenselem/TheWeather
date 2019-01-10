package benselem.moez.theweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Restarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        //Toast.makeText(context, "The wi", Toast.LENGTH_SHORT).show();
            if(intent.getAction()=="restartservice")
            {
                context.startService(new Intent(context, MyService.class));
                System.out.println("service restarted");
            }
        }
    }



