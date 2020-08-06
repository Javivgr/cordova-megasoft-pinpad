/**
 * Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.util;

import org.apache.cordova.LOG;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

/**
 * 
 * clase utilitaria, comportamientos comunes para todos los plugins con respecto
 * a la interfaz grafica.
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class WinUtils {

	//CONSTANTES
	private static final String TAG = WinUtils.class.getName();
	
	//Metodos Publicos
//	public static void showResult(Context cxt, String title, String message) {
//        new AlertDialog.Builder(cxt)
//                .setTitle(title)
//                .setMessage(message)
//                .setCancelable(false)
//                .setPositiveButton(cxt.getString(R.string.sure),
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }).show();
//    }
	
	/**
	 * prepara una ventana de dialogo de progreso
	 * @param msg (String) el mensaje a mostrar
	 * @param cxt (Context) contexto de la aplicación
	 * @return (Dialog) dialogo creado
	 */
	public static Dialog getMyDialog(String msg, Context cxt) {
		LOG.i(TAG, "Creando dialogo de progreso, mensaje: "+msg+", Context: "+cxt);
        ProgressDialog mypDialog = new ProgressDialog(cxt);
        mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mypDialog.setMessage(msg);
        mypDialog.setIndeterminate(false);
        mypDialog.setCancelable(false);
        return mypDialog;
    }
	
	/**
	 * muestra un mensaje corto por pantalla 
	 * @param context (Context) contexto de la aplicación
	 * @param message (String) mensaje a mostrar
	 * @param shortDuration (boolean) indica la duracion del mensaje, true corto, false largo
	 */
	public static void makeToast(Context context, String message, boolean shortDuration){
		Toast.makeText(context, message, (shortDuration)?Toast.LENGTH_SHORT:Toast.LENGTH_LONG).show();
	}
	
	
	
}
