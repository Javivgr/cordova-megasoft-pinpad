package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import java.util.List;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanTarjeta;
import ve.com.megasoft.pinpad.util.Tarjeta;
import ve.com.megasoft.pinpad.util.Utils;

public class HandlerSegCertEmv extends BaseHandler {
    //CONSTANTES
    private static final String TAG = HandlerSegCertEmv.class.getName();

    //Constructor
    public HandlerSegCertEmv(String tagAction, SharedPreferences sp) {
        super(tagAction, sp);
    }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de segundo certificado EMV");

        try {
            if (msg.what == 0) {

                String data55 = (String) msg.obj;

                Log.d(TAG, "--------------- DATOS TARJETA RECIBIDOS ------------------");
                Log.i(TAG, "TLV data: " + data55 );
                Log.d(TAG, "--------------------------------------------------------");

                //lo cambié a false porque solo guardará tlv
                BeanTarjeta tarjeta = new BeanTarjeta(false);
                tarjeta.setTlv(data55);

                //falta seteo pinblock

                //indicamos que el procesamiento fue correcto
                tarjeta.setEstatus("00");
                tarjeta.setMensaje("Lectura EMV");

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS FECHA GUARDADOS ------------------");
                Log.d(TAG, "Bean Tarjeta: " + tarjeta.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");
                Utils.saveTmpData(tagAction, tarjeta.toJson().toString(), sp);

            }else{throw new Exception((String) msg.obj);} //este error no es lanzado correctamente

        } catch (Exception e) {
            Log.e(TAG, "no se pudo iniciar la transacción EMV, error: ",e);
            try{
                BeanBase json = new BeanBase();
                json.setEstatus("99");
                json.setMensaje("No se pudo recuperar leer la tarjeta correctamente, error "+e.getMessage());
                Utils.saveTmpData(tagAction, json.toJson().toString(), sp);
            }
            catch(Exception e1){
                //en caso de que no pueda trabajar el JSON
                Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
                Utils.saveTmpData(tagAction, "!ERROR!"+":99:No se pudo obtener serial, "+e.getMessage(), sp);
            }

        }
    }
}
