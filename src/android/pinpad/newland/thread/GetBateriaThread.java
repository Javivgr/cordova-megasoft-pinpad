package ve.com.megasoft.pinpad.newland.thread;
import android.util.Log;

import android.os.Handler;
import android.os.Message;

import com.newland.me.DeviceManager;
import com.newland.mtype.BatteryInfoResult;

import org.apache.cordova.CordovaPlugin;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import ve.com.megasoft.pinpad.newland.data.NlUtils;

import ve.com.megasoft.pinpad.newland.thread.BaseThread;

public class GetBateriaThread extends BaseThread {

    //CONSTANTES
    private static final String TAG = GetBateriaThread.class.getName();

    //Constructor
    public GetBateriaThread(Handler handler, DeviceManager deviceManager) {
        super(handler, deviceManager);
    }

    //Metodos Sobre Escritos
    @Override
    public void run() {
        super.run();

        try {
            //obtenemos la bateria del dispositivos
            /* BatteryInfoResult battery = deviceManager.getDevice().getBatteryInfo();
            Log.i(TAG, "Electric: "+battery.getElectricBattery()); */

            CordovaPlugin cp= NlUtils.getCordovaPlugin();
            Context context= cp.cordova.getActivity().getApplicationContext();
            
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = 100*level / (float)scale;
            Log.i(TAG, "batteryPct: "+batteryPct);

            //creamos el mensaje
            Message msg = new Message();
            msg.what = 0;
            msg.obj = batteryPct;
            //msg.obj = battery;

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);
        }catch (Exception e){
            //creamos el mensaje
            Message msg = new Message();
            msg.what = 1;
            msg.obj = e;

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);

        }

    }
}
