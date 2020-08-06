package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.newpos.app.cmd.Instruction;

import java.util.List;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanPinblock;
import ve.com.megasoft.pinpad.util.Utils;

import com.newland.mtype.util.ISOUtils;

public class HandlerPinblock extends BaseHandler {

    //CONSTANTES
    private static final String TAG = HandlerPinblock.class.getName();

    //Constructor
    public HandlerPinblock(String tagAction, SharedPreferences sp) { super(tagAction, sp); }

    //Metodos Sobre Escritos
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Log.i(TAG, "Procesando peticion de pinblock");

        try{
            if(msg.what==0){
                byte[] pinblock= (byte[]) msg.obj;

                //armamos la respuesta del pinblock
                BeanPinblock bean = new BeanPinblock();
                bean.setEstatus("00");
                bean.setMensaje("Pinblock Recuperado");
                bean.setPinblockData(ISOUtils.hexString(pinblock));
                bean.setPinblockKsn("");

                //generamos el JSON y lo guardamos en memoria
                Log.d(TAG, "--------------- DATOS PINBLOCK RECIBIDOS ------------------");
                Log.d(TAG, "Bean Pinblock: " + bean.toJson().toString());
                Log.d(TAG, "--------------------------------------------------------");

                //generamos el JSON y lo guardamos en memoria
                Log.i(TAG, "Guardando temporal de pinblock");
                Utils.saveTmpData(tagAction, bean.toJson().toString(), sp);
            }
            else{throw new Exception((String) msg.obj);}
        }
        catch(Exception e){
            try{
                BeanBase json = new BeanBase();
                json.setEstatus("99");
                json.setMensaje("No se pudo recuperar el pinblock del pinpad, error "+e.getMessage());
                Utils.saveTmpData(tagAction, json.toJson().toString(), sp);
            }
            catch(Exception e1){
                //en caso de que no pueda trabajar el JSON
                Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
                Utils.saveTmpData(tagAction, "!ERROR!"+":99:No se pudo obtener el pinblock, "+e.getMessage(), sp);
            }
        }
    }
}
