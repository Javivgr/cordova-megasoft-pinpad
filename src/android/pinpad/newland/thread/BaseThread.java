/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.newland.thread;

import android.os.Handler;

import com.newland.me.DeviceManager;

/**
 * clase base para los thread del newland n910
 *
 * @author Alejandro Enrique Castro Rodriguez / Adrian Jesus Silva Simoes
 *
 */
public class BaseThread extends Thread {
    //CONSTANTES
    private static final String TAG = BaseThread.class.getName();

    //Atributios
    protected Handler handler;
    protected DeviceManager deviceManager;

    //Constructor
    public BaseThread(Handler handler, DeviceManager deviceManager){
        this.handler = handler;
        this.deviceManager = deviceManager;
    }
}
