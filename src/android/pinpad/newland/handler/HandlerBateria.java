package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.DeviceRTException;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanBateria;
import ve.com.megasoft.pinpad.util.Utils;

public class HandlerBateria extends BaseHandler {

    //CONSTANTES
    private static final String TAG = HandlerBateria.class.getName();

    //Constructor
    public HandlerBateria(String tagAction, SharedPreferences sp) {
        super(tagAction, sp);
    }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de bateria");

        try{
            if(msg.what==0){
                /* BatteryInfoResult response = (BatteryInfoResult) msg.obj; */

                Float battery = (Float) msg.obj;

                //armamos la respuesta de la fecha
                BeanBateria bean = new BeanBateria();
                bean.setEstatus("00");/**/
                bean.setMensaje("Bateria recuperada");
                //bean.setBateria(response.getElectricBattery());
                bean.setBateria(Float.toString(battery));

                Log.d(TAG, "--------------- DATOS BATERIA RECIBIDOS ------------------");
                /* Log.d(TAG, "Bateria electrica: " + response.getElectricBattery());
                Log.d(TAG, "Estatus de carga: " + response.getChargeStatus());
                Log.d(TAG, "Estatus de usb: " + response.getUsbStatus()); */
                Log.d(TAG, "--------------------------------------------------------");

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS FECHA GUARDADOS ------------------");
                Log.d(TAG, "Bean Bateria: " + bean.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");
                Utils.saveTmpData(tagAction, bean.toJson().toString(), sp);
            }
            else{throw (DeviceRTException) msg.obj;}

        } catch (Exception e) {
            Log.e(TAG, "no se pudo recuperar la bateria del dispositivo, error: ",e);
            try{
                BeanBase json = new BeanBase();
                json.setEstatus("99");
                json.setMensaje(e.getMessage());
                Utils.saveTmpData(tagAction, json.toJson().toString(), sp);
            }
            catch(Exception e1){
                //en caso de que no pueda trabajar el JSON
                Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e1);
                Utils.saveTmpData(tagAction, "!ERROR!"+":99:"+e.getMessage(), sp);
            }
        }
    }
}
