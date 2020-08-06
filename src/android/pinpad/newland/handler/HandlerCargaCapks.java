package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.util.Utils;

public class HandlerCargaCapks extends BaseHandler {

    //CONSTANTES
    private static final String TAG = HandlerCargaCapks.class.getName();

    //Constructor
    public HandlerCargaCapks(String tagAction, SharedPreferences sp) {
        super(tagAction, sp);
    }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando respuesta de carga de CAPKS en el N910");

        try{
            if(msg.what==0) {
         
                //construimos la respuesta exitosa
                BeanBase bean = new BeanBase();
                bean.setEstatus("00");
                bean.setMensaje("Capks Cargados de forma exitosa");

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "-------------- DATOS CAPKS CARGADOS -----------------");
                Log.d(TAG, "Bean CAPKS: " + bean.toJson().toString());
                Log.d(TAG, "----------------------------------------------------");
                Utils.saveTmpData(tagAction, bean.toJson().toString(), sp);
            }
            else{throw new Exception((String) msg.obj);}
        }
        catch(Exception e){
            Log.e(TAG, "no se pudo iniciar la transacción EMV, error: ",e);
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
