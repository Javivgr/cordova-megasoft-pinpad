package ve.com.megasoft.pinpad.newland.handler;

import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.Iterator;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanTarjeta;
import ve.com.megasoft.pinpad.bean.BeanPinblock;
import ve.com.megasoft.pinpad.util.Tarjeta;
import ve.com.megasoft.pinpad.util.Utils;

public class HandlerStartEmv extends BaseHandler {
    //CONSTANTES
    private static final String TAG = HandlerStartEmv.class.getName();

    //Constructor
    public HandlerStartEmv(String tagAction, SharedPreferences sp) {
        super(tagAction, sp);
    }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de transacción EMV");

        try {
            if (msg.what == 0) {

                List res = (List) msg.obj;

                Log.d(TAG, "--------------- DATOS TARJETA RECIBIDOS ------------------");
                Log.i(TAG, "Type: " + res.get(0) );
                Log.i(TAG, "Cardholder Name: " + res.get(1) );
                Log.i(TAG, "Second track: " + res.get(2) );
                Log.i(TAG, "TLV data: " + res.get(3) );
                Log.i(TAG, "Pinblock: " + res.get(4) );
                Log.d(TAG, "--------------------------------------------------------");

                BeanTarjeta tarjeta = new BeanTarjeta(true);
                tarjeta.setExtrationMode("E");

                //Obtenemos el track2
                tarjeta.setTrack2Data((String) res.get(2));

                //Extraemos la informacion del  track2
                String[] camposTrack2 = Tarjeta.extraerDatosTrack2(tarjeta.getTrack2Data());
                tarjeta.setObfuscatedPan(Tarjeta.obtenerPanEnmascarado(camposTrack2[0]));

                //Obtenemos el service code
                tarjeta.setServiceCode(Tarjeta.extraerServiceCode(tarjeta.getTrack2Data()));

                //Obtenemos el track 1 y extraemos el nombre del tarjetahabiente de ser posible
                tarjeta.setCardholderName((String) res.get(1));

                tarjeta.setTlv((String) res.get(3));

                //indicamos que el procesamiento fue correcto
                tarjeta.setEstatus("00");
                tarjeta.setMensaje("Lectura EMV");

                BeanPinblock pinblock = null;
                String sPin = (String) res.get(4);

                if (!sPin.equals("null")){
                    pinblock = new BeanPinblock();
                    pinblock.setPinblockKsn("");
                    pinblock.setPinblockData(sPin);
                }

                //procesamos la respuesta para entregarla
                JSONObject finalJson = tarjeta.toJson();
                if(pinblock!=null){
                    String key;
                    JSONObject pinblockJson = pinblock.toJson();
                    @SuppressWarnings("rawtypes")
                    Iterator i = pinblockJson.keys();
                    while(i.hasNext()){
                        key = (String) i.next();
                        finalJson.put(key, pinblockJson.get(key));
                    }
                }

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS EMV PRIMERCERT GUARDADOS ------------------");
                Log.d(TAG, "Bean Tarjeta+Pinblock: " + finalJson.toString());
                Log.d(TAG, "--------------------------------------------------------");
                Utils.saveTmpData(tagAction, finalJson.toString(), sp);

            }else{throw new Exception((String) msg.obj);} //este error no es lanzado correctamente

        } catch (Exception e) {
            Log.e(TAG, "no se pudo iniciar la transacción EMV, error: ",e);
            try{
                BeanBase json = new BeanBase();
                json.setEstatus("99");
                json.setMensaje("No se pudo recuperar leer la tarjeta correctamente. Error: "+e.getMessage());
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
