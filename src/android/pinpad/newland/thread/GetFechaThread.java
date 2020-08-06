/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.newland.thread;

import android.os.Handler;
import android.os.Message;

import com.newland.me.DeviceManager;

import java.util.Date;

/**
 * clase base para los thread del newland n910
 *
 * @author Alejandro Enrique Castro Rodriguez / Adrian Jesus Silva Simoes
 *
 */
public class GetFechaThread extends BaseThread {

    //CONSTANTES
    private static final String TAG = GetFechaThread.class.getName();

    //Constructor
    public GetFechaThread(Handler handler, DeviceManager deviceManager) {
        super(handler, deviceManager);
    }

    //Metodos Sobre Escritos
    @Override
    public void run() {
        super.run();

        try {
            //obtenemos la fecha del dispositivos
            Date date = this.deviceManager.getDevice().getDeviceDate();

            //creamos el mensaje
            Message msg = new Message();
            msg.what = 0;
            msg.obj = date;

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
