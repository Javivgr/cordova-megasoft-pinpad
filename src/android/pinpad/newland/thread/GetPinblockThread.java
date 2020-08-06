package ve.com.megasoft.pinpad.newland.thread;

//import ve.com.megasoft.pinpad.newland.data.N900KeyBoardNumberActivity;
import ve.com.megasoft.pinpad.newland.data.NlUtils;
import ve.com.megasoft.pinpad.newland.modelo.ModeloNewland;
import ve.com.megasoft.pinpad.newland.data.Const;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.content.Context;
import android.content.Intent;

import com.newland.me.DeviceManager;
import com.newland.mtype.util.ISOUtils;
import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.pin.AccountInputType;
import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.module.common.pin.KeyManageType;
import com.newland.mtype.module.common.pin.KekUsingType;
import com.newland.mtype.module.common.pin.K21Pininput;
import com.newland.mtype.module.common.pin.KeyboardRandom;
import com.newland.mtype.module.common.pin.PinConfirmType;
import com.newland.mtype.module.common.pin.WorkingKey;

import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.K21PininutEvent;
import com.newland.mtype.module.common.pin.PinInputEvent;
import java.util.Arrays;

import org.apache.cordova.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.TimeUnit;

//import ve.com.megasoft.mobilepos.BuildConfig;

public class GetPinblockThread extends BaseThread {

    //CONSTANTES
    private String accNo;
    private String monto;
	private boolean isEmv;

    private static final String TAG = GetPinblockThread.class.getName();

    //Constructor
    public GetPinblockThread(boolean isEmv, DeviceManager deviceManager, List data) {
        super(null, deviceManager);
        this.accNo= (String) data.get(0);
		this.monto= (String) data.get(1);
		this.isEmv=isEmv;
	}

    //Metodos Sobre Escritos
    @Override
    public void run() {
        super.run();

        try {
            //TODO: no estamos cargando la working key de la transacción.

            //creamos lista con datos necesarios para la actividad de pinblock
			ArrayList<String> list = new ArrayList<String>();
			list.add(accNo);
            list.add(Boolean.toString(isEmv));
            list.add(monto);

            //creamos mensaje con lista
			Message msg = new Message();
            msg.what = 0;
            msg.obj = list;

            //enviamos mensaje a handler
            ModeloNewland.getPinHandler().sendMessage(msg);
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
