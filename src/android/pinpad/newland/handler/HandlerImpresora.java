package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import java.util.List;

import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.DeviceRTException;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanImpresora;
import ve.com.megasoft.pinpad.util.Utils;

public class HandlerImpresora extends BaseHandler {

    //CONSTANTES
    private static final String TAG = HandlerImpresora.class.getName();

    //Constructor
    public HandlerImpresora(String tagAction, SharedPreferences sp) {
        super(tagAction, sp);
    }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de impresora");

        try{
            if(msg.what==0){

                List impresora = (List) msg.obj;

                Log.d(TAG, "Impresora recuperada (Handler): "+ (String) impresora.get(0));
                BeanImpresora bean = new BeanImpresora();

                bean.setMensaje((String) impresora.get(0));
                bean.setEstatus((String) impresora.get(1));
               
                //bean.setSerial((String) impresora.get(0));
                /* BatteryInfoResult response = (BatteryInfoResult) msg.obj; */

                Log.d(TAG, "--------------- DATOS IMPRESORA RECIBIDOS ------------------");
                /* Log.d(TAG, "Bateria electrica: " + response.getElectricBattery());
                Log.d(TAG, "Estatus de carga: " + response.getChargeStatus());
                Log.d(TAG, "Estatus de usb: " + response.getUsbStatus()); */
                Log.d(TAG, "--------------------------------------------------------");

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS IMPRESORA GUARDADOS ------------------");
                Log.d(TAG, "Bean Impresora: " + bean.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");
                Utils.saveTmpData(tagAction, bean.toJson().toString(), sp);
            }
            else if(msg.what==1){

                List impresora = (List) msg.obj;

                Log.d(TAG, "Impresora recuperada (Handler): "+ (String) impresora.get(0));
                BeanImpresora bean = new BeanImpresora();

                bean.setMensaje((String) impresora.get(0));
                bean.setEstatus((String) impresora.get(1));
               
                //bean.setSerial((String) impresora.get(0));
                /* BatteryInfoResult response = (BatteryInfoResult) msg.obj; */

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS IMPRESORA Error GUARDADOS ------------------");
                Log.d(TAG, "Bean Impresora: " + bean.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");
                Utils.saveTmpData(tagAction, bean.toJson().toString(), sp);
            }
            else{throw new Exception((String) msg.obj);}

        } catch (Exception e) {
            Log.e(TAG, "no se pudo realizar la impresión, error: ",e);
            try{
                BeanBase json = new BeanBase();
                json.setEstatus("99");
                json.setMensaje(e.toString());
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
