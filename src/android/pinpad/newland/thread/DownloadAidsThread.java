package ve.com.megasoft.pinpad.newland.thread;

import android.os.Handler;
import android.os.Message;
import java.util.List;
import java.util.Iterator;

import com.newland.me.DeviceManager;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.emv.TerminalConfig;
import com.newland.mtype.module.common.emv.AIDConfig;
import com.newland.mtype.util.ISOUtils;

import com.newland.mtype.ModuleType;

import ve.com.megasoft.pinpad.newland.data.NlUtils;

import android.util.Log;

public class DownloadAidsThread extends BaseThread {

    //CONSTANTES
    private static final String TAG = DownloadAidsThread.class.getName();

    //Atributos
    private List<String> aids;

    //Metodos Privados
    public void cargarAids(){
        try{
            //obtenemos el modulo de EMV
            EmvModule emvModule= NlUtils.getEmvModule();

            /* boolean bClearAidsResult= emvModule.clearAllAID();
            Log.d(TAG,"bClearAidsResult: "+bClearAidsResult); */

            //procesamos la lista de aids a cargar 
            boolean bResult = false;
            int iFailAids =0, iAids = 0;

            TerminalConfig trmnlConfig= new TerminalConfig();

            trmnlConfig.setMerchantCategryCode("1234");
            trmnlConfig.setMerchantIdentifier("123456789012345");
            trmnlConfig.setPointOfServiceEntryMode(5);
            trmnlConfig.setFallbackPosentry(new byte[0x80]);
            trmnlConfig.setTerminalCountryCode(new byte[] {0x08,0x62});
            trmnlConfig.setTransactionCurrencyCode("0862");
            trmnlConfig.setTransactionCurrencyExp("02");

            Log.i(TAG,"before setting terminal config"); 
            bResult = emvModule.setTrmnlParams(trmnlConfig);
            Log.i(TAG,"set terminal config result: "+bResult);  

            Log.d(TAG,"aid's loading init");
            for (String aid : this.aids){
                //cargamos AID seleccionada al dispositivo
                Log.d(TAG,"aid to be loaded: "+aid);
                bResult=emvModule.addAIDWithDataSource(ISOUtils.hex2byte(aid));
                Log.d(TAG,"result of aid[" +iAids+ "] load: "+bResult);
                
                //si se encuentra un error muestra el log
                if(!bResult){
                    iFailAids++;
                    Log.e(TAG,"Fallo en cargar AID["+ iAids + "]: "+ aid);   
                }
                iAids++;
            }

            //si todos los AIDs fallan, lanza error
            if(this.aids.size() == iFailAids){
                throw new Exception("Fallo en carga de AIDs");
            }

            /* List<AIDConfig> listAIDConfig;
            listAIDConfig = emvModule.fetchAllAID();

            Log.i(TAG,"before clearAllAID"); 
            bResult = emvModule.clearAllAID();
            Log.i(TAG,"clar all aid result: "+bResult); 

            if (listAIDConfig != null) {
                for (Iterator i = listAIDConfig.iterator(); i.hasNext();) {
                    AIDConfig oneAidConfig = (AIDConfig) i.next();
                    //int len = oneAidConfig.getAidLength();
                    //byte[] aid = new byte[len];
                    //System.arraycopy(oneAidConfig.getAid(),0,aid,0,len);
                    //String aid1 = Dump.getHexDump(aid);
                    oneAidConfig.setTerminalCountryCode(new byte[] {0x08,0x62});
                    oneAidConfig.setTransactionCurrencyCode("0862");
                    oneAidConfig.setTransactionCurrencyExp("02");
                    bResult=emvModule.addAID(oneAidConfig);
                    Log.i(TAG,"second addition to aid: "+bResult);
                }
                if (listAIDConfig.size() == 0) {
                    //msg_no_aid.
                }
            } else {
                //msg_get_all_aid_list_failed.
            } */

            //creamos el mensaje
            Message msg = new Message();
            msg.what = 0;

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);

        }
        catch(Exception e){
            //creamos el mensaje
            Message msg = new Message();
            msg.what = 1;
            msg.obj = e;
 
            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);
        }
    }

    //Constructor
    public DownloadAidsThread(Handler handler, DeviceManager deviceManager, List<String> aids) {
        super(handler, deviceManager);

        //guardamos las Aids obtenidas en el atributo de la clase
        this.aids = aids;
    }

    //Metodos Sobre Escritos
    @Override
    public void run() {
        super.run();
        
        //cargamos los Aids
        this.cargarAids();
    }
}