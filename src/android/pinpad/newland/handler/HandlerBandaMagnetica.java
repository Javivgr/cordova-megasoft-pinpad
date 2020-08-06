package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import java.util.List;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanTarjeta;
import ve.com.megasoft.pinpad.util.Tarjeta;
import ve.com.megasoft.pinpad.util.Utils;

public class HandlerBandaMagnetica extends BaseHandler {
    //CONSTANTES
    private static final String TAG = HandlerBandaMagnetica.class.getName();

    //Constructor
    public HandlerBandaMagnetica(String tagAction, SharedPreferences sp) {
        super(tagAction, sp);
    }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de transacción banda mágnetica");

        try {
            if (msg.what == 0) {

                List res = (List) msg.obj;

                Log.d(TAG, "--------------- DATOS TARJETA RECIBIDOS ------------------");
                Log.i(TAG, "Type: " + res.get(0) );
                Log.i(TAG, "First track: " + res.get(1) );
                Log.i(TAG, "Second track: " + res.get(2) );
                //Log.i(TAG, "Third track: " + res.get(3) );
                //Log.i(TAG, "Account: " + res.get(4) );
                Log.d(TAG, "--------------------------------------------------------");

                BeanTarjeta tarjeta = new BeanTarjeta(true);
                tarjeta.setExtrationMode("B");

                //Obtenemos el track2
                tarjeta.setTrack2Data((String) res.get(2));

                //Extraemos la informacion del  track2
                String[] camposTrack2 = Tarjeta.extraerDatosTrack2(tarjeta.getTrack2Data());
                tarjeta.setObfuscatedPan(Tarjeta.obtenerPanEnmascarado(camposTrack2[0]));

                //Obtenemos el service code
                tarjeta.setServiceCode(Tarjeta.extraerServiceCode(tarjeta.getTrack2Data()));

                //Obtenemos el track 1 y extraemos el nombre del tarjetahabiente de ser posible
                tarjeta.setCardholderName(Tarjeta.extrarDatosTrack1((String) res.get(1) ));

                //indicamos que el procesamiento fue correcto
                tarjeta.setEstatus("00");
                tarjeta.setMensaje("Lectura Banda");

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS TARJETA GUARDADOS ------------------");
                Log.d(TAG, "Bean Tarjeta: " + tarjeta.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");
                Utils.saveTmpData(tagAction, tarjeta.toJson().toString(), sp);

            }else{throw new Exception((String) msg.obj);}

        } catch (Exception e) {
            Log.e(TAG, "no se pudo iniciar la transacción banda mágnetica, error: ",e);
            try{
                BeanBase json = new BeanBase();
                json.setEstatus("99");
                json.setMensaje("No se pudo leer la tarjeta correctamente, error "+e.getMessage());
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
