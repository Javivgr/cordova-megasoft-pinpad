/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.newland.handler;

import android.content.SharedPreferences;
import android.os.Handler;

/**
 * clase base para todos los handlers a ser usados en el newland n910
 *
 * @author Alejandro Enrique Castro Rodriguez / Adrian Jesus Silva Simoes
 */
public class BaseHandler extends Handler {

    //CONSTANTES
    private static final String TAG = BaseHandler.class.getName();

    //Atributos
    protected String tagAction;
    protected SharedPreferences sp;

    //Constructor
    public BaseHandler (String tagAction, SharedPreferences sp){
        this.tagAction = tagAction;
        this.sp = sp;
    }

}
