/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.newland.modelo;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.Build; //Agregado C2P

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.Device;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.K21CardReader;
import com.newland.mtype.module.common.security.K21SecurityModule;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtypex.nseries3.NS3ConnParams;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.emv.jni.service.EmvJNIService;
import com.newland.common.RunningModel;

import com.newland.mtype.module.common.pin.K21Pininput;
import com.newland.mtype.module.common.pin.KekUsingType;
import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.util.ISOUtils;
import ve.com.megasoft.pinpad.newland.data.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import ve.com.megasoft.mobilepos.MainActivity;
import ve.com.megasoft.pinpad.bean.BeanAidsInfo;
import ve.com.megasoft.pinpad.bean.BeanEmvKeyInfo;
import ve.com.megasoft.pinpad.newland.data.N900KeyBoardNumberActivity;

import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanCalibracion;
import ve.com.megasoft.pinpad.modelo.ModeloPinpadBase;
//No existe hasta creación de proyecto.
//import ve.com.megasoft.pinpad.newland.activity.N900KeyBoardNumberActivity;
import ve.com.megasoft.pinpad.newland.handler.HandlerBateria;
import ve.com.megasoft.pinpad.newland.handler.HandlerCargaAids;
import ve.com.megasoft.pinpad.newland.handler.HandlerCargaCapks;
import ve.com.megasoft.pinpad.newland.handler.HandlerGetFecha;
import ve.com.megasoft.pinpad.newland.handler.HandlerSerial;
import ve.com.megasoft.pinpad.newland.handler.HandlerStartEmv;
import ve.com.megasoft.pinpad.newland.handler.HandlerBandaMagnetica;
import ve.com.megasoft.pinpad.newland.handler.HandlerPinblock;
import ve.com.megasoft.pinpad.newland.handler.HandlerSegCertEmv;
import ve.com.megasoft.pinpad.newland.handler.HandlerImpresora; //impresora
import ve.com.megasoft.pinpad.newland.thread.DownloadAidsThread;
import ve.com.megasoft.pinpad.newland.thread.DownloadCapksThread;
import ve.com.megasoft.pinpad.newland.thread.GetBateriaThread;
import ve.com.megasoft.pinpad.newland.thread.GetFechaThread;
import ve.com.megasoft.pinpad.newland.thread.GetSerialThread;
import ve.com.megasoft.pinpad.newland.thread.StartEmvThread;
import ve.com.megasoft.pinpad.newland.thread.BandaMagneticaThread;
import ve.com.megasoft.pinpad.newland.thread.GetPinblockThread;
import ve.com.megasoft.pinpad.newland.thread.ImpresoraThread;      //impresora
import ve.com.megasoft.pinpad.util.Utils;

import ve.com.megasoft.pinpad.newland.data.AidsCapksData;
import ve.com.megasoft.pinpad.newland.data.NlUtils;

import ve.com.megasoft.pinpad.newland.data.SimpleTransferListener;

/**
 * Cordova Java Part Plugin Project
 *
 * "Cordova-Megasoft-Pinpad"
 *
 * clase que entiende la comunicacion con el Newland N910
 *
 * @author Alejandro Castro Rodriguez / Adrian Jesus Silva Simoes
 */
public class ModeloNewland extends ModeloPinpadBase {

    //CONSTANTES
    private static final String TAG = ModeloNewland.class.getName();

    //Atributos
    private static final String K21_DRIVER_NAME = "com.newland.me.K21Driver";
    private static final int GET_PIN_FOR_EMV=1;
    private static final int GET_PIN_FOR_ANOTHER=2;
    private String statusCode = null;
    private byte[] aids;

    //working key
    private String workingKey = "";
    private JSONArray args;

    //Handler's -- TODO definir los handlers a usar
    private static DeviceManager deviceManager ;
    private K21SecurityModule securityModule;
    private CallbackContext callbackContext;

    //Constructor
    public ModeloNewland(){BLUETOOTHUSER = false;}

    //Handlers
    private Handler handlerConnectDevice = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            try {
                Log.i(TAG, "Estableciendo conexion");
                BeanBase bean = new BeanBase();

                //revisamos si el dispositivo ya esta conectado
                if(!isDeviceAlive()) {

                    deviceManager = ConnUtils.getDeviceManager();
                    deviceManager.init(cordova.getActivity().getApplicationContext(), K21_DRIVER_NAME, new NS3ConnParams(), new DeviceEventListener<ConnectionCloseEvent>() {

                        @Override
                        public void onEvent(ConnectionCloseEvent event, Handler handler) {
                            if (event.isSuccess()) {
                                Log.i(TAG, "Event Success");
                            }
                            if (event.isFailed()) {
                                Log.i(TAG, "Event Failed");
                            }
                        }

                        @Override
                        public Handler getUIHandler() {
                            return null;
                        }
                    });

                    deviceManager.connect();
                    deviceManager.getDevice().setBundle(new NS3ConnParams());

                    /* //logs emv
                    EmvJNIService jniEmvservice = new EmvJNIService();
                    jniEmvservice.jniemvSetDebugMode(1);
                    
                    //RunningModel debug
                    RunningModel.isDebugEnabled =true; */
                    
                    //indicamos que tenemos el puerto aperturado
                    bean.setEstatus(OK);
                    bean.setMensaje("Canal de comunicacion aperturado");
                }
                else{
                    //indicamos que tenemos el puerto aperturado
                    bean.setEstatus(OK);
                    bean.setMensaje("Canal de comunicacion ya abierto");
                }

                Utils.saveTmpData(TAGACTIVESTATUS, bean.toJson().toString(), sp);

            } catch (Exception e) {
                Log.e(TAG, "No se pudo establecer conexion con el dispositivo, error: ",e);
                try{
                    BeanBase json = new BeanBase();
                    json.setEstatus("99");
                    json.setMensaje("No se pudo establecer conexion con el dispositivo, error "+e.getMessage());
                    Utils.saveTmpData(TAGACTIVESTATUS, json.toJson().toString(), sp);
                }
                catch(Exception e1){
                    //en caso de que no pueda trabajar el JSON
                    Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
                    Utils.saveTmpData(TAGACTIVESTATUS, "!ERROR!"+":99:No se pudo obtener serial, "+e.getMessage(), sp);
                }
            }
        }
    };

    private static Handler pinHandler= new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 0: {
                List list = (List) msg.obj;
                try {
                    //Master Key y Working Key a cargar
                    String masterKey = "BDA3D42227B71BF5D7E278EEA9A37DD7";
                    String tempWk = "D2CEEE5C1D3AFBAF00374E0CC1526C86";

                    //obtenemos working key
                    tempWk=NlUtils.getWorkingKey();

                    //Obtenemos el modulo del pinpad del dispositivo
                    Log.d(TAG,"Obteniendo instancia de K21Pininput");
                    K21Pininput pininput=(K21Pininput) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PININPUT);

                    //carga MK
                    Log.d(TAG,"Cargando mk");
                    byte[] mainKey = pininput.loadMainKey(KekUsingType.MAIN_KEY, Const.MKIndexConst.DEFAULT_MK_INDEX, ISOUtils.hex2byte(masterKey), null, Const.MKIndexConst.ZERO_MK_INDEX);

                    //carga WK
                    Log.d(TAG,"Cargando wk");
                    byte[] wk_pin = pininput.loadWorkingKey(WorkingKeyType.PININPUT, Const.MKIndexConst.DEFAULT_MK_INDEX, Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX, ISOUtils.hex2byte(tempWk), null);

                    //obtenemos instancia de plugin de cordova
                    CordovaPlugin cordovaPlugin = NlUtils.getCordovaPlugin();

                    //creamos intent con actividad de pinblock
                    Intent intent = new Intent(cordovaPlugin.cordova.getActivity().getApplicationContext(),
                    N900KeyBoardNumberActivity.class);

                    //agregamos valores necesarios para iniciar obtención de pinblock
                    intent.putExtra("accNo", (String)list.get(0));
                    intent.putExtra("monto", (String)list.get(2));

                    //obtenemos request code para identificar para qué proceso se está
                    //obteniendo el pinblock
                    String isEmv=(String)list.get(1);
                    int reqCode=isEmv.equals("true")?GET_PIN_FOR_EMV:GET_PIN_FOR_ANOTHER;

                    //iniciamos actividad
                    Log.d(TAG,"Obteniendo pinblock para "+(reqCode==GET_PIN_FOR_EMV?"emv":"otro"));
                    cordovaPlugin.cordova.startActivityForResult(cordovaPlugin, intent, reqCode);
                } catch (Exception e) {
                    //si ocurre un error, hay que dar la respuesta al servicio que solicitó el pinblock.
                    String isEmv=(String)list.get(1);
                    int reqCode=isEmv.equals("true")?GET_PIN_FOR_EMV:GET_PIN_FOR_ANOTHER;

                    //si fue solicitada para emv
                    if (reqCode==GET_PIN_FOR_EMV){
                        Message msgError = new Message();
                        msgError.what = 0;
                        msgError.obj = null;
                        //enviamos respuesta a evento emv
                        SimpleTransferListener.getPinEventHandler().sendMessage(msgError);
                    //si fue solicitada para un procedimiento distinto
                    } else if (reqCode==GET_PIN_FOR_ANOTHER){
                        HandlerPinblock handler = new HandlerPinblock(TAGPINBLOCK, NlUtils.getSharedPreferences());
                        Message msgError = new Message();
                        msgError.what = 1;
                        msgError.obj = e.getMessage();
                        //entregamos mensaje a handler de pinblock
                        handler.sendMessage(msgError);
                        //damos concluída la ejecución de comando EXECMODE de cordova exitosamente
                        NlUtils.getCallbackContext().success();
                    }
                }
                break;
                }
            default:
                break;
            }
        }
    };

    //Metodos Privados

    /**
     * procedimiento para verificar la conexion con el dipositivo
     * @return (boolean) dispositivo esta conectado
     */
    private boolean isDeviceAlive() {
        boolean ifConnected = ( deviceManager== null ? false : deviceManager.getDevice().isAlive());
        return ifConnected;
    }

    private void connectDevice(){
        try {
            deviceManager = ConnUtils.getDeviceManager();
            deviceManager.init(cordova.getActivity().getApplicationContext(), K21_DRIVER_NAME, new NS3ConnParams(), new DeviceEventListener<ConnectionCloseEvent>() {
                @Override
                public void onEvent(ConnectionCloseEvent event, Handler handler) {
                    if (event.isSuccess()) {
                        Log.i(TAG, "Event Success");
                    }
                    if (event.isFailed()) {
                        Log.i(TAG, "Event Failed");
                    }
                }

                @Override
                public Handler getUIHandler() {
                    return null;
                }
            });
            deviceManager.connect();
            deviceManager.getDevice().setBundle(new NS3ConnParams());

        } catch (Exception e1) {
            Log.e(TAG, "No se pudo establecer el canal de comunicacion con el pinpad en connectDevice", e1);
        }

    }

    /**
     * procedimiento que cierra el canal de comunicación
     */
    private void disconectDevice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (deviceManager != null) {
                        deviceManager.disconnect();
                        deviceManager = null;
                        //baseActivity.showMessage(baseActivity.getString(R.string.msg_device_disconn_succ), MessageTag.TIP);
                        //baseActivity.btnStateToWaitingInit();
                    }
                } catch (Exception e) {
                    //baseActivity.showMessage(baseActivity.getString(R.string.msg_device_disconn_exception) + e, MessageTag.TIP);
                }
            }
        }).start();
    }

    //Metodos Publicos
    static public Device getDevice(){
        return deviceManager.getDevice();
    }

    public static Handler getPinHandler() {
		return pinHandler;
    }

    //Metodos Sobre Escritos
    /*Apertura y cierre de canales de comunicacion*/
    @Override
    public boolean openComChannel(CallbackContext callbackContext) {
        try{

                
            //ubicamos el dispositivo y abrimos el canal de comunicacion
            Thread ejecutor = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = 0;
                    handlerConnectDevice.sendMessage(msg);
                }
            });

            cordova.getThreadPool().execute(ejecutor);

            callbackContext.success();
            return true;
        }

        catch(Exception e){
            Log.e(TAG, "No se pudo establecer el canal de comunicacion con el pinpad", e);
            errorCallback("No se pudo establecer el canal de comunicacion con el pinpad", e, callbackContext);  
            disconectDevice();
            // errorCallback("verifique que este se encuentra encendido y con carga", e, callbackContext);
            return false;
         
        }
       
    }

    @Override
    public boolean closeComChannel(CallbackContext callbackContext) {
        try{
            //cerramos el canal de comunicacion del pinpad
            if(isDeviceAlive()) {
                disconectDevice();
            }

            BeanBase bean = new BeanBase();
            bean.setEstatus(OK);
            bean.setMensaje("Canal de comunicacion cerrado");

            //armamos el bean de respuesta
            callbackContext.success(bean.toJson());

            return true;

        }
        catch(Exception e){
            Log.e(TAG, "No se pudo cerrar el canal de comunicacion con el pinpad", e);
            errorCallback("No se pudo cerrar el canal de comunicacion con el pinpad", e, callbackContext);
            return false;
        }
    }

    /*Funciones basicas de la aplicacion*/
    @Override
    public boolean getFecha(CallbackContext callbackContext) {
        try {
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    //limpiamos los TMP
                    Utils.clearTmpData(sp);

                    //Instanciamos el handler
                    HandlerGetFecha handler = new HandlerGetFecha(TAGFECHA, sp);

                    //Instaciamos el thread de recuperacion de fechas
                    GetFechaThread ejecutor = new GetFechaThread(handler, deviceManager);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();

                    Log.i(TAG, "Ejecucion iniciadad");
                    return true;
                }
                else{
                    throw new Exception("Dispositivo no conectado");
                }
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de solicitud de fecha");
                return getModeExecute(TAGFECHA, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}

        }
        catch (Exception e) {
            Log.e(TAG, "No se pudo recuperar la fecha y hora del pinpad", e);
            Utils.clearTmpData(sp);
            errorCallback("No se pudo recuperar la fecha y hora del pinpad", e, callbackContext);
            return false;
        }
    }

    @Override
    public boolean getBateria(CallbackContext callbackContext) {
        try{
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    //limpiamos los TMP
                    Utils.clearTmpData(sp);

                    //guardamos cordova plugin para su posterior uso
                    NlUtils.setCordovaPlugin(this);

                    //Instanciamos el handler
                    HandlerBateria handler = new HandlerBateria(TAGBATERIA, sp);

                    //Instaciamos el thread de recuperacion de fechas
                    GetBateriaThread ejecutor = new GetBateriaThread(handler, deviceManager);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();

                    Log.i(TAG, "Ejecucion iniciadad");
                    return true;
                }
                else{
                    throw new Exception("Dispositivo no conectado");
                }
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de solicitud de serial");
                return getModeExecute(TAGBATERIA, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo recuperar el serial del pinpad", e);
            Utils.clearTmpData(sp);
            errorCallback("no se pudo recuperar el serial del pinpad", e, callbackContext);
            return false;
        }
    }

    @Override
    public boolean printer(JSONArray args, CallbackContext callbackContext) {
        try{
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    //limpiamos los TMP
                    Utils.clearTmpData(sp);

                    //guardamos cordova plugin para su posterior uso
                    NlUtils.setCordovaPlugin(this);

                    //obtenemos todos
                    JSONArray parametros = args.getJSONArray(0);
                    String voucher = parametros.getString(0);
                    String tiempo = parametros.getString(1);
                   
                    Log.i(TAG,"*********** IMPRESORA ModeloNewland ***********");
                    Log.i(TAG,"voucher: "+voucher);
                    Log.i(TAG,"tiempo: "+tiempo);


                    ArrayList<String> list = new ArrayList<String>();
			        list.add(voucher);
			        list.add(tiempo);

                    //Instanciamos el handler
                    HandlerImpresora handler = new HandlerImpresora(TAGIMPRESORA, sp);

                    //Instaciamos el thread de recuperacion de fechas
                    ImpresoraThread ejecutor = new ImpresoraThread(list, handler, deviceManager);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();

                    Log.i(TAG, "Ejecucion iniciadad");
                    return true;
                }
                else{
                    throw new Exception("Dispositivo no conectado");
                }
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de solicitud de impresora");
                return getModeExecute(TAGIMPRESORA, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo realizar la impresion: ", e);
            Utils.clearTmpData(sp);
            errorCallback("no se pudo recuperar la impresora del pinpad", e, callbackContext);
            return false;
        }
    }

    @Override
    public boolean getTamper(CallbackContext callbackContext) {
        return false;
    }

    @Override
    public boolean getSerial(CallbackContext callbackContext) {

        Log.i(TAG, "Iniciando proceso de solicitud de serial");
        try{
            if(mode.equals(EXECMODE)){
                //verificar si tenemos una coneccion establecida por en el pinpad
                if (isDeviceAlive()) {
                    Log.i(TAG, "Ejecutando solicitud de serial del dispositivo");

                    //abrimos un dialogo para mostrar el progreso solicitando serial
                    showProgressDialog("pd_pinpad_data");

                    //limpiamos los TMP
                    Utils.clearTmpData(sp);

                    HandlerSerial handler = new HandlerSerial(TAGSERIAL, sp);

                    GetSerialThread ejecutor = new GetSerialThread(handler, deviceManager);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();

                    Log.i(TAG, "Ejecucion de obtecion de serial iniciadad");
                    return true;
                }
                else{
                    throw new Exception("Dispositivo no conectado");
                }
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de solicitud de serial");
                return getModeExecute(TAGSERIAL, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo recuperar el serial del pinpad", e);
            Utils.clearTmpData(sp);
            errorCallback("no se pudo recuperar el serial del pinpad", e, callbackContext);
            return false;
        }
    }

    /*Banda Magnetica y PIN block*/
    @Override
    public boolean getBandaMagnetica(CallbackContext callbackContext) {
        Log.i(TAG, "Iniciando proceso de transaccion banda magnética");
        try{
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    Log.i(TAG, "ejecutando comando de inicio transaccion banda magnética");

                    //muestro dialogo
                    showProgressDialog("texto_lector_banda");

                    //limpiamos los tmp
                    Utils.clearTmpData(sp);

                    //guardamos working key
                    NlUtils.setWorkingKey(configuracion.getPinpadWorkingKey());

                    //Instanciamos el handler
                    HandlerBandaMagnetica handler = new HandlerBandaMagnetica(TAGPRIMERCERT, sp);

                    //Instaciamos el thread de lectura de datos por banda magnética
                    BandaMagneticaThread ejecutor = new BandaMagneticaThread(handler, deviceManager);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();

                    return true;

                } else{
                    throw new Exception("Dispositivo no conectado");
                }
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de inicio transaccion banda magnética");
                return getModeExecute(TAGPRIMERCERT, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo iniciar transaccion banda magnética", e);
            Utils.clearTmpData(sp);
            errorCallback("No se pudo iniciar transaccion banda magnética", e, callbackContext);
            return false;
        }
    }

    //método que maneja la respuesta de una actividad lanzada
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        byte[] pin = null;
        boolean wasOk = false;

        Log.d(TAG,"Procesando resultado de actividad");

        //si la actividad fue terminada correctamente
        if (resultCode == Activity.RESULT_OK) {
            pin = data.getByteArrayExtra("pin");
            wasOk = true;
        } else if (resultCode == Activity.RESULT_CANCELED) {
            //acciones a tomar si la actividad fue cancelada
        }else if(resultCode == -2){
            //acciones a tomar si la actividad tuvo una excepción
        }

        //si la actividad fue lanzada para obtener el pinblock en una transacción emv
        if (requestCode==GET_PIN_FOR_EMV){
            Message msg = new Message();
            msg.what = 0;
            msg.obj = pin;
            //enviamos respuesta a evento emv
            SimpleTransferListener.getPinEventHandler().sendMessage(msg);
        //si la actividad fue lanzada para otro tipo de transacción
        } else if (requestCode==GET_PIN_FOR_ANOTHER){
            HandlerPinblock handler = new HandlerPinblock(TAGPINBLOCK, sp);
            Message msg = new Message();
            msg.what = wasOk?0:1;
            msg.obj = pin;
            //entregamos mensaje a handler de pinblock
            handler.sendMessage(msg);
            //damos concluída la ejecución de comando EXECMODE de cordova exitosamente
            this.callbackContext.success();
        }
    }
    
    @Override
    public boolean getPinblock(JSONArray args, CallbackContext callbackContext) {
        try{
            if(mode.equals(EXECMODE)){
                //verificar si tenemos una coneccion establecida por en el pinpad
                if (isDeviceAlive()) {
                    Log.i(TAG, "Ejecutando solicitud de captura de pin y generacion de pinblock");

                    //recuperamos la data requerida
                    JSONArray parametros = args.getJSONArray(0);
                    String track2 = parametros.getString(0);
                    String montoTransaccion = parametros.getString(1);
                    String mensaje = null;
                    if(parametros.length()==3){mensaje = parametros.getString(2);}
                    String[] data = track2.split("=");

                    Log.i(TAG,"track2: "+track2);
                    Log.i(TAG,"montoTransaccion: "+montoTransaccion);
                    Log.i(TAG,"data: "+data[0]);
                    Log.i(TAG,"parametros: "+parametros);

                    //limpiamos los TMP
                    Utils.clearTmpData(sp);

                    //guardamos cordova plugin para su posterior uso, es necesario para poder invocar
                    //nueva actividad mediante método estático.
                    NlUtils.setCordovaPlugin(this);

                    //seteamos atributos necesarios para lanzar handler desde método estático, 
                    //esto, si ocurre error en pinHandler
                    NlUtils.setSharedPreferences(sp);
                    NlUtils.setCallbackContext(callbackContext);

			        ArrayList<String> list = new ArrayList<String>();
			        list.add(data[0]);
			        list.add(montoTransaccion);

                    //se crea thread, GetPinblockThread(boolean isEmv, DeviceManager deviceManager, String accNo)
                    GetPinblockThread ejecutor = new GetPinblockThread(false, deviceManager,list);

                    cordova.getThreadPool().execute(ejecutor);

                    PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
				    r.setKeepCallback(true);
				    callbackContext.sendPluginResult(r);

                    Log.i(TAG, "Ejecucion iniciada");
                    return true;
                }
                else{
                    throw new Exception("Dispositivo no conectado");
                }

            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de generación de Pinblock en el N910");
                return getModeExecute(TAGPINBLOCK, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo generar el Pinblock", e);
            Utils.clearTmpData(sp);
            errorCallback("No se pudo generar el Pinblock", e, callbackContext);
            return false;
        }
    }

    /*transaccionalidad EMV*/
    @Override
    public boolean getEmvTrans(JSONArray args, CallbackContext callbackContext) {
        try{
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    Log.i(TAG, "ejecutando comando de inicio transaccion EMV");

                    //muestro dialogo
                    showProgressDialog("texto_lector_chip");

                    //limpiamos los tmp
                    Utils.clearTmpData(sp);

                    //recuperamos la data requerida
                    JSONArray parametros = args.getJSONArray(0);
                    String monto = parametros.getString(0);
                    String cashback = parametros.getString(1);
                    String tipoTrans = parametros.getString(2);
                    String mensaje = null;
                    if(parametros.length()==4){mensaje = parametros.getString(3);}

                    Log.d(TAG, "---------------- DATOS PRIMER CERTIFICADO ------------------");
                    Log.i(TAG,"monto: "+monto);
                    Log.i(TAG,"cashback: "+cashback);
                    Log.i(TAG,"tipoTrans: "+tipoTrans);
                    Log.i(TAG,"mensaje: "+mensaje);
                    Log.d(TAG, "--------------------------------------------------------");

                    //creamos array con todos los parámetros necesarios para iniciar transacción emv
                    ArrayList<String> sArgs = new ArrayList<String>();
                    sArgs.add(monto);
                    sArgs.add(cashback);
                    sArgs.add(tipoTrans);
                    sArgs.add(mensaje);

                    //guardamos working key
                    NlUtils.setWorkingKey(configuracion.getPinpadWorkingKey());

                    //guardamos cordova plugin para su posterior uso, es necesario para poder obtener
                    //módulo EMV (se necesita context de actividad).
                    NlUtils.setCordovaPlugin(this);

                    //Instanciamos los handlers de primer y segundo certificado
                    HandlerStartEmv handlerStartEmv = new HandlerStartEmv(TAGPRIMERCERT, sp);

                    HandlerSegCertEmv handlerSegCertEmv = new HandlerSegCertEmv(TAGSEGUNDCERT, sp);

                    //Instanciamos el thread de transacción emv
                    StartEmvThread ejecutor = new StartEmvThread(
                        sArgs, handlerStartEmv, handlerSegCertEmv, deviceManager);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();
                    return true;

                } else{
                    throw new Exception("Dispositivo no conectado");
                }
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de comando EMV");
                return getModeExecute(TAGPRIMERCERT, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo iniciar transaccion emv", e);
            Utils.clearTmpData(sp);
            errorCallback("No se pudo iniciar transaccion emv", e, callbackContext);
            return false;
        }
    }

    @Override
    public boolean getEmvSegundoCertificado(JSONArray args, CallbackContext callbackContext) {
        try{
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    Log.i(TAG, "ejecutando comando de segundo certificado EMV");

                    //limpiamos los tmp
                    Utils.clearTmpData(sp);

                    //recuperamos la data requerida
                    JSONArray parametros = args.getJSONArray(0);
                    String tag39 = parametros.getString(0);
                    String status = parametros.getString(1);
                    String tag71 = parametros.getString(2);
                    String tag72 = parametros.getString(3);
                    String tag91 = parametros.getString(4);

                    Log.d(TAG, "---------------- DATOS SEGUNDO CERTIFICADO ------------------");
                    Log.i(TAG,"tag39: "+tag39);
                    Log.i(TAG,"status: "+status);
                    Log.i(TAG,"tag71: "+tag71);
                    Log.i(TAG,"tag72: "+tag72);
                    Log.i(TAG,"tag91: "+tag91);
                    Log.d(TAG, "--------------------------------------------------------");

                    //obtenemos handler de transacción ejecutándose
                    Handler segCertHandler = SimpleTransferListener.getSegCertHandler();

                    //creamos array con todos los parámetros necesarios para ejecutar segundo certificado
                    ArrayList<String> sArgs = new ArrayList<String>();
                    sArgs.add(tag39);
                    sArgs.add(status);
                    sArgs.add(tag71);
                    sArgs.add(tag72);
                    sArgs.add(tag91);

                    //creamos mensaje con parámetros
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = sArgs;

                    //entregamos el mensaje al handler
                    segCertHandler.sendMessage(msg);

                    callbackContext.success();
                    return true;

                } else{
                    throw new Exception("Dispositivo no conectado");
                }
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de comando de segundo certificado EMV");
                return getModeExecute(TAGSEGUNDCERT, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo iniciar comando de segundo certificado emv", e);
            Utils.clearTmpData(sp);
            errorCallback("No se pudo iniciar comando de segundo certificado emv", e, callbackContext);
            return false;
        }
    }

    /*Cancelaciones*/
    @Override
    public boolean getAbortOperation(CallbackContext callbackContext) {
        return false;
    }

    @Override
    public boolean getAbortOperationEmv(CallbackContext callbackContext) {
        return false;
    }

    /*Calibracion*/
    @Override
    public boolean getCalibracionDispositivo(CallbackContext callbackContext) {
        Log.i(TAG, "Iniciando proceso de calibracion pinpad - dispositivo");
        try{

        if(Build.MANUFACTURER.toLowerCase().equals("newland")){

            if(mode.equals(EXECMODE)){
                Log.i(TAG, "Ejecutando proceso de calibracion");

                //limpiamos los tmp
                Utils.clearTmpData(sp);

                BeanCalibracion bean;
                bean = new BeanCalibracion();
                bean.setEstatus(OK);
                bean.setDeviceClass(ModeloNewland.class.getName());

                //se agregó setDeviceName y setDeviceAddres porque sino, no funciona.
                Log.e(TAG, "Se quemó el nombre y dirección del dispositivo. Cambiar");
                bean.setDeviceName(Build.MANUFACTURER+" "+Build.MODEL);
                bean.setDeviceAddress("");

                bean.setMensaje("Calibracion Exitosa");

                Utils.saveTmpData(TAGCALIBRACION, bean.toJson().toString(), sp);
                callbackContext.success();
                return true;
            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de proceso de calibracion");
                return getModeExecute(TAGCALIBRACION, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        else{
            throw new Exception(Build.MANUFACTURER);
        }
        }
        catch(Exception e){

            Log.e(TAG, "No se pudo realizar la calibracion", e);
            closeSelectDeviceDialog();
            Utils.clearTmpData(sp);
            errorCallback("No se pudo realizar la calibracion", e, callbackContext);
            return false;

        }
    }

    /*Carga de AIDS y CAPKS*/
    @Override
    public boolean downloadAids(JSONArray args, CallbackContext callbackContext) {
        try{
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    Log.i(TAG, "Ejecutando solicitud de carga de AIDS en el N910");

                    //limpiamos los TMP
                    Utils.clearTmpData(sp);

                    //recuperamos la data requerida
                    JSONArray aidsJson = args.getJSONArray(0);

                    //recuperamos el listado de aids
                    List<BeanAidsInfo> listAids = new ArrayList<BeanAidsInfo>();
                    for (int i = 0; i < aidsJson.length(); i++) {
                        listAids.add(new BeanAidsInfo(aidsJson.getJSONObject(i)));
                    }

                    //debug de json tratado
                    Log.d(TAG, "---------------- DATOS AIDS RECIBIDOS ------------------");
                    int count = 0;
                    for (BeanAidsInfo info : listAids) {
                        Log.d(TAG, "AID " + count + " " + info);
                        count++;
                    }
                    Log.d(TAG, "--------------------------------------------------------");
                    
                    //creamos lista de strings de aids en formato TLV
                    ArrayList<String> aidsStringList = AidsCapksData.buildAidsInfo(listAids);

                    //guardamos cordova plugin para su posterior uso, es necesario para poder obtener
                    //módulo EMV (se necesita context de actividad).
                    NlUtils.setCordovaPlugin(this);

                    //Instanciamos el handler
                    HandlerCargaAids handler = new HandlerCargaAids(TAGDOWNLOADAIDS, sp);

                    //Instaciamos el thread de carga de aids
                    DownloadAidsThread ejecutor = new DownloadAidsThread(handler, deviceManager,aidsStringList);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();

                    Log.i(TAG, "Ejecucion iniciadad");
                    return true;
                }
                else{
                    throw new Exception("Dispositivo no conectado");
                }

            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de carga de AIDS en el N910");
                return getModeExecute(TAGDOWNLOADAIDS, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo realizar la carga de AIDS", e);
            Utils.clearTmpData(sp);
            errorCallback("No se pudo realizar la carga de AIDS", e, callbackContext);
            return false;
        }
    }

    @Override
    public boolean downloadCapks(JSONArray args, CallbackContext callbackContext) {
        try{
            if(mode.equals(EXECMODE)){
                if (isDeviceAlive()) {
                    Log.i(TAG, "Ejecutando solicitud de carga de CAPKs en el N910");

                    //limpiamos los TMP
                    Utils.clearTmpData(sp);

                    //recuperamos la data requerida
                    JSONArray capksJson = args.getJSONArray(0);

                    //recuperamos el listado de capks
                    List<BeanEmvKeyInfo> listCapks = new ArrayList<BeanEmvKeyInfo>();
                    for (int i = 0; i < capksJson.length(); i++) {
                        listCapks.add(new BeanEmvKeyInfo(capksJson.getJSONObject(i)));
                    }

                    //debug de json tratado
                    Log.d(TAG, "---------------- DATOS CAPKS RECIBIDOS ------------------");
                    int count = 0;
                    for (BeanEmvKeyInfo info : listCapks) {
                        Log.d(TAG, "CAPK " + count + " " + info);
                        count++;
                    }
                    Log.d(TAG, "--------------------------------------------------------");
                    
                    //creamos lista de strings de capks en formato TLV
                    ArrayList<String> capksStringList = AidsCapksData.buildCapksInfo(listCapks);

                    //guardamos cordova plugin para su posterior uso, es necesario para poder obtener
                    //módulo EMV (se necesita context de actividad).
                    NlUtils.setCordovaPlugin(this);

                    //Instanciamos el handler
                    HandlerCargaCapks handler = new HandlerCargaCapks(TAGDOWNLIADEMVKEYS, sp);

                    //Instaciamos el thread de carga de capks
                    DownloadCapksThread ejecutor = new DownloadCapksThread(handler, deviceManager,capksStringList);

                    //solicitamos la ejecucion del hilo
                    cordova.getThreadPool().execute(ejecutor);
                    callbackContext.success();

                    Log.i(TAG, "Ejecucion iniciada");
                    return true;
                }
                else{
                    throw new Exception("Dispositivo no conectado");
                }

            }
            else if(mode.equals(GETMODE)){
                Log.i(TAG, "Consultando estado de ejecucion de carga de CAPKs en el Newland");
                return getModeExecute(TAGDOWNLIADEMVKEYS, callbackContext);
            }
            else{throw new Exception("modo de trabajo no valido");}
        }
        catch(Exception e){
            Log.e(TAG, "No se pudo realizar la carga de CAPKs", e);
            Utils.clearTmpData(sp);
            errorCallback("No se pudo realizar la carga de CAPKs", e, callbackContext);
            return false;
        }
    }

}