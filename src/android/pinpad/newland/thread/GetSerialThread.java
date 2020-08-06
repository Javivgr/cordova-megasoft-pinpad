package ve.com.megasoft.pinpad.newland.thread;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.newland.me.DeviceManager;
import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.security.K21SecurityModule;
import com.newland.mtype.DeviceInfo;

import org.apache.cordova.BuildConfig;

import java.util.ArrayList;
import java.util.List;

//import ve.com.megasoft.mobilepos.BuildConfig;

public class GetSerialThread extends BaseThread {

    //CONSTANTES
    private static final String TAG = GetSerialThread.class.getName();

    //Constructor
    public GetSerialThread(Handler handler, DeviceManager deviceManager) {
        super(handler, deviceManager);
    }

    //Metodos Sobre Escritos
    @Override
    public void run() {
        super.run();

        try {
            //Obtenemos el modulo de seguridad del dispositivo
            K21SecurityModule securityModule = (K21SecurityModule) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SECURITY);

            //Lista que contine informacion del dispositivo
            List<Object> list = new ArrayList<Object>();

            //(0)obtenemos el serial del dispositivos
            list.add(securityModule.getDeviceInfo().getSN());
            //(1)obtenemos version del sistema operativo
            list.add(System.getProperty("os.name"));
            //(2)obtenemos emulador
            list.add("MobilePOS - 2.1.0");
            //list.add("MobilePOS - " + BuildConfig.VERSION_NAME);
            //(3)obtenemos la version del kernel
            list.add("");
            //(4)obtenemos modelo del dispositivo
            list.add(Build.MODEL);
            
            //(5)obtenemos marca del dispositivo
            list.add(Build.MANUFACTURER);
            
            //(6)obtenemos si tiene impresora
            list.add(securityModule.getDeviceInfo().isSupportPrint());

            //(7)revisamos si tenemos lector de ICCARDS
            list.add(securityModule.getDeviceInfo().isSupportICCard());

            //(8)revisamos si tenemos lector de Banda Magnetica
            list.add(securityModule.getDeviceInfo().isSupportMagCard());
           
            Log.i(TAG,"---------------PROBANDO_SERIAL----------------------");
            Log.i(TAG,Build.DEVICE);
            Log.i(TAG,Build.PRODUCT);
            Log.i(TAG,Build.BRAND);
            Log.i(TAG,Build.MANUFACTURER);
            Log.i(TAG,Build.MODEL);
            Log.i(TAG,"---------------PROBANDO_SERIAL----------------------");

            //Log de datos del dispositivo
            /* DeviceInfo deviceInfo = deviceManager.getDevice().getDeviceInfo();

            Log.d(TAG,deviceInfo.getModel());
            Log.d(TAG,deviceInfo.getFirmwareVer()); */

            //creamos el mensaje
            Message msg = new Message();
            msg.what = 0;
            msg.obj = list;

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);
        }
        catch (Exception e){

            Message msg = new Message();
            msg.what = 1;
            msg.obj = e;

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);
        }

    }


}
