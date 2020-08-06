/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Date;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanFecha;
import ve.com.megasoft.pinpad.util.Utils;

/**
 * Clase handler encargada de manejar la respuesta de recuperacion de fecha
 *
 * @author Alejandro Enrique Castro Rodriguez / Adrian Jesus Silva Simoes
 */
public class HandlerGetFecha extends BaseHandler {

    //CONSTANTES
    private static final String TAG = HandlerGetFecha.class.getName();

    //Constructor
    public HandlerGetFecha(String tagAction, SharedPreferences sp) {
        super(tagAction,sp);
    }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de fecha y hora");

        try{
            if(msg.what==0){
                Date date = (Date) msg.obj;

                //armamos la respuesta de la fecha
                BeanFecha bean = new BeanFecha();
                bean.setEstatus("00");/**/
                bean.setMensaje("Fecha Recuperada");
                bean.setFecha(Utils.dateToString(date,Utils.DATETIME_FORMAT));

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS FECHA RECIBIDOS ------------------");
                Log.d(TAG, "Bean Fecha: " + bean.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");
                Utils.saveTmpData(tagAction, bean.toJson().toString(), sp);
            }
            else{throw new Exception((String) msg.obj);}

        } catch (Exception e) {
            Log.e(TAG, "no se pudo recuperar la fecha del dispositivo, error: ",e);
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
