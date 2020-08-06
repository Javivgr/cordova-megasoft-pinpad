package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.newpos.app.cmd.Instruction;

import java.util.List;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanSerial;
import ve.com.megasoft.pinpad.util.Utils;

public class HandlerSerial extends BaseHandler {

    //CONSTANTES
    private static final String TAG = HandlerSerial.class.getName();

    //Constructor
    public HandlerSerial(String tagAction, SharedPreferences sp) { super(tagAction, sp); }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de serial");

        try{
            if(msg.what==0){
                //procesamos respuesta del serial

                List serial = (List) msg.obj;

                Log.d(TAG, "Serial recuperado: "+serial);

                BeanSerial bean = new BeanSerial();
                bean.setEstatus("00");
                bean.setMensaje("Serial Recuperado");
                bean.setSerial((String) serial.get(0));
                //bean.setSerial("72199513"); //quemado
                bean.setOs((String) serial.get(1));
                bean.setApp((String) serial.get(2));
                bean.setKernel((String) serial.get(3));
                bean.setModelo((String) serial.get(4));
                bean.setMarca((String) serial.get(5));
                //nuevos valores
                bean.setIsPrinter(String.valueOf(serial.get(6)));
                bean.setIsICCard(String.valueOf(serial.get(7)));
                bean.setIsMagCard(String.valueOf(serial.get(8)));

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS SERIAL RECIBIDOS ------------------");
                Log.d(TAG, "Bean SERIAL: " + bean.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");

                //generamos el JSON y lo guardamos en memoria
                Utils.saveTmpData(tagAction, bean.toJson().toString(), sp);
            }
            else{throw new Exception((String) msg.obj);}
        }
        catch(Exception e){
            try{
                BeanBase json = new BeanBase();
                json.setEstatus("99");
                json.setMensaje("No se pudo recuperar el serial del pinpad, error "+e.getMessage());
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
