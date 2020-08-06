package ve.com.megasoft.pinpad.newland.thread;
import android.util.Log;

import android.os.Handler;
import android.os.Message;

import com.newland.me.DeviceManager;
import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.ModuleType;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.newland.mtype.module.common.printer.FontSettingScope;
import com.newland.mtype.module.common.printer.FontType;
import com.newland.mtype.module.common.printer.LiteralType;
import com.newland.mtype.module.common.printer.PrintContext;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterResult;
import com.newland.mtype.module.common.printer.PrinterStatus;
import com.newland.mtype.module.common.printer.ThrowType;
import com.newland.mtype.module.common.printer.WordStockType;


import org.apache.cordova.CordovaPlugin;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import ve.com.megasoft.pinpad.newland.data.NlUtils;

import ve.com.megasoft.pinpad.newland.thread.BaseThread;

public class ImpresoraThread extends BaseThread {

    //CONSTANTES
    private static final String TAG = ImpresoraThread.class.getName();
    private List sArgs;
    private String voucher;
    private String tiempo;

    List<Object> list = new ArrayList<Object>();

    //Constructor
    public ImpresoraThread(List sArgs, Handler handler, DeviceManager deviceManager) {
        super(handler, deviceManager);
        this.sArgs=sArgs;
    }

    private  String voucherScript (String cadena){

        StringBuffer scriptBuffer = new StringBuffer();
        String[] result = cadena.split("\r\n");
        scriptBuffer.append("!asc s\n");//Set sub. Title font as large.
        //scriptBuffer.append("!hz s\r\n!asc s\r\n!gray 6\r\n");
        // !NLFONT (size china, size eng, mode)
        scriptBuffer.append("!yspace 1\n");     //interlineado
        scriptBuffer.append("!NLFONT 6 10 2\n"); //PRN_ZM_FONT_10x8
	    for (int x=0; x<result.length; x++){
	    	if(result[x].length()>1){    //para evitar que escriba *text en saltos de linea
                if(countSpaces(result[x]) >= 3 || (result[x].length()>33 && countSpaces(result[x])> 0)) {
                    result[x] = result[x].replaceAll("^\\s*","");   //elimina todos los espacios del principio
                    result[x] = "*text c " + result[x] +"\n";       //agrega el comando imprimir centrado
                }
                else{
                    result[x] = result[x].replaceAll("^\\s*","");
                    result[x] = "*text l " + result[x] +"\n";
                }
            }
	    	else {result[x] = "*feedline 1\n";}                     //Deja una linea en blanco
	    scriptBuffer.append(result[x]);
        }

                    /*scriptBuffer.append("!asc s\n");//Set sub. Title font as large.
					scriptBuffer.append("*line  \r\n");
				    scriptBuffer.append("*text l     PLATCO MERCANTIL / PROVINCIAL\r\n");
				    scriptBuffer.append("*text l                CARACAS\r\n");
                    scriptBuffer.append("*text l  RIF:J-00000003-0 AFILIADO:PL000001\r\n");*/

        Log.i(TAG,"VoucherScript:  " + scriptBuffer.toString());
      return scriptBuffer.toString();
    }

    //Metodos Sobre Escritos
    @Override
    public void run() {
        super.run();
        String mensaje="";
        try {
            //obtenemos la bateria del dispositivos
            /* BatteryInfoResult battery = deviceManager.getDevice().getBatteryInfo();
            Log.i(TAG, "Electric: "+battery.getElectricBattery()); */

            CordovaPlugin cp= NlUtils.getCordovaPlugin();
            Context context= cp.cordova.getActivity().getApplicationContext();
            Printer impresora=(Printer) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PRINTER);
            PrinterResult result = PrinterResult.GENERAL_ERROR;
            impresora.init();

            PrinterStatus status = impresora.getStatus();
            StringBuffer scriptBuffer = new StringBuffer();

            if (status == PrinterStatus.BUSY) { 
                mensaje="La impresora esta ocupada";
                Log.w(TAG,mensaje);
            }else if (status == PrinterStatus.FLASH_READWRITE_ERROR) { 
                mensaje="Flash read-write error";
                Log.w(TAG, mensaje);
            }else if (status == PrinterStatus.HEAT_LIMITED) { 
                mensaje="La impresora esta recalentada";
                Log.w(TAG, mensaje);
            }else if (status == PrinterStatus.OUTOF_PAPER) {   
                mensaje="Sin papel en la impresora!";
                Log.w(TAG, mensaje);
            } else {
                    this.voucher= (String) sArgs.get(0);
                    this.tiempo= (String) sArgs.get(1);
                    
                    Log.i(TAG,"Iniciando Impresion Thread, voucher: " + this.voucher);

                    impresora.init();
                    Log.i(TAG,"voucher impresoraThread:  " + this.voucher + "tiempo " + this.tiempo);
                    result = impresora.printByScript(PrintContext.defaultContext(),  voucherScript(this.voucher).getBytes("GBK"), 60, TimeUnit.SECONDS);
                    //PrinterResult result =  impresora.printByScript(PrintContext.defaultContext(), scriptBuffer.toString(),null, 60, TimeUnit.SECONDS);


                    Log.i(TAG,"Resultado de la impresion: " + result.toString());
                    impresora.paperThrow(ThrowType.BY_LINE, 2);//Paper Movement

                   

        
                   
            }
            Message msg = new Message();
            if (result.equals(PrinterResult.SUCCESS)) {
                mensaje="La impresora imprimio correctamente";
                list.add(mensaje);
                list.add("00");
                msg.what = 0;
                msg.obj =  list;
            } else {
                list.add(mensaje);
                list.add("99");
                msg.what = 1;
                msg.obj =  list;
            }

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);

            //creamos el mensaje

        }catch (Exception e){
            //creamos el mensaje
            Message msg = new Message();
            msg.what = 2;
            msg.obj = list;

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);

        }

    }

    private int countSpaces(String line) {
		int spaceCount=0;
		for (char c : line.toCharArray()) {
		    if (c == ' ') {
		         spaceCount++;
		    }
		    else {break;}
		}
		
		return spaceCount;
	}
}
