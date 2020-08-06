package ve.com.megasoft.pinpad.newland.data;

import android.content.Intent;
import android.graphics.Rect;
import android.newland.os.NlBuild;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.TextView;

import org.apache.cordova.CordovaPlugin;
import android.content.res.Resources;
import android.app.Application;
import android.app.Activity;

import ve.com.megasoft.pinpad.newland.modelo.ModeloNewland;
import ve.com.megasoft.pinpad.newland.data.NlUtils;

import ve.com.megasoft.pinpad.newland.data.N900PinKeyBoard.PinKeySeq;
import ve.com.megasoft.pinpad.newland.data.N900PinKeyBoard;

import ve.com.megasoft.pinpad.newland.data.Const;
import ve.com.megasoft.pinpad.newland.data.SoundPoolImpl;

import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.AccountInputType;
import com.newland.mtype.module.common.pin.K21Pininput;
import com.newland.mtype.module.common.pin.K21PininutEvent;
import com.newland.mtype.module.common.pin.KeyManageType;
import com.newland.mtype.module.common.pin.KeyboardRandom;
import com.newland.mtype.module.common.pin.PinConfirmType;
import com.newland.mtype.module.common.pin.PinInputEvent;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.util.ISOUtils;

import java.util.concurrent.TimeUnit;
import java.text.DecimalFormat;

/**
 * Password Keyboard Activity
 */
//public class N900KeyBoardNumberActivity extends BaseActivity {
public class N900KeyBoardNumberActivity extends Activity {
	private static final String TAG = "KeyBoardNumber";
	private K21Pininput pinInput;
	/** password */
	private TextView txtPassword;
	private StringBuffer buffer;
	private int inputLen = 0;
	private N900PinKeyBoard pkb;
	private SoundPoolImpl spi ;

	private String package_name;
	private Resources resources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//obtenemos pool de recursos (sustituye a R de Android Studio)
		CordovaPlugin cp=NlUtils.getCordovaPlugin();
		Application app = cp.cordova.getActivity().getApplication();
		package_name = app.getPackageName();
		resources = app.getResources();
		
		//seteamos vista de la actividad
		setContentView(resources.getIdentifier("input_pin_fragment", "layout", package_name));

		//obtenemos modulo de pin
		pinInput =(K21Pininput) ModeloNewland.getDevice().getStandardModule(ModuleType.COMMON_PININPUT);

		//instanciamos y obtenemos pool de sonidos
		spi= SoundPoolImpl.getInstance();
		spi.initLoad(cp.cordova.getActivity().getApplicationContext(),
				resources.getIdentifier("click1", "raw", package_name));

		//iniciamos teclado
		init();
	}

	private void init() {

		//obtenemos monto de operación
		String monto = getIntent().getStringExtra("monto");
		Log.d(TAG,"monto en n900:"+monto);

		//seteamos monto en campo de texto cuando el monto sea distinto a 000
		if (!(monto.equals("0")||monto.equals("000"))){
			TextView txtAmount = (TextView) findViewById(resources.getIdentifier("tv_input_pin_amount", "id", package_name));
			monto = new StringBuilder(monto).insert(monto.length()-2, ".").toString();
			double dMonto = Double.parseDouble(monto);
			DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
			txtAmount.setText("Monto: "+ decimalFormat.format(dMonto));
		}

		//obtenemos campo de texto para contraseña
		txtPassword = (TextView) findViewById(resources.getIdentifier("txt_password", "id", package_name));

		//obtenemos objeto de diseño de teclado
		pkb = (N900PinKeyBoard) findViewById(resources.getIdentifier("n900pinkeyboard", "id", package_name));

		//obtenemos numero de cuenta y se guarda en memoria
		final String accNo = getIntent().getStringExtra("accNo");

		//creamos listener encargado de diseñar apartado gráfico de teclado
		pkb.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

			private boolean first;//  To prevent it from entering the onPreDraw() all the time.

			@Override
			public boolean onPreDraw() {
				if (!first) {
					first = true;
					boolean bool = getRandomKeyBoardNumber();
					if(!bool){
						finish();
						return first;
					}
					/* if (((MyApplication) getApplication()).isDukpt()) {
						pinInput.startStandardPinInput(null, new WorkingKey(Const.DUKPTIndexConst.DEFAULT_DUKPT_INDEX), KeyManageType.DUKPT, AccountInputType.USE_ACCOUNT, accNo, 12, getPinLengthRange(0,12), new byte[] { 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F' }, PinConfirmType.ENABLE_ENTER_COMMANG, 59, TimeUnit.SECONDS, null, null, pinInputListener);
					} else if (((MyApplication) getApplication()).isSM4()) {
						pinInput.startStandardPinInput(null, new WorkingKey(Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX), KeyManageType.SM4, AccountInputType.USE_ACCOUNT, accNo, 12, getPinLengthRange(0,12), new byte[] { 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F' }, PinConfirmType.ENABLE_ENTER_COMMANG, 59, TimeUnit.SECONDS, null, null, pinInputListener);
					}else{ */
						pinInput.startStandardPinInput(null, new WorkingKey(Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX), KeyManageType.MKSK, AccountInputType.USE_ACCOUNT, accNo, 12, getPinLengthRange(4,12), new byte[] { 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F' }, PinConfirmType.ENABLE_ENTER_COMMANG, 10, TimeUnit.SECONDS, null, null, pinInputListener);
					//}
				}
				return first;
			}
		});

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2: // inputting
				int len = (Integer) msg.obj;
				buffer = new StringBuffer();
				for (int i = 0; i < len; i++) {
					buffer.append(" * ");
				}
				txtPassword.setText(buffer.toString());
				break;

			default:
				break;
			}
		}
	};

	/**
	 * Get the number of random keyboard
	 * 
	 * @return
	 */
	private boolean getRandomKeyBoardNumber() {
		try {
			byte[] initCoordinate = pkb.getCoordinate();
			Log.i(TAG, "init coordinates:" + ISOUtils.hexString(initCoordinate));
			// get key value of random keyboard
			byte[] keySeq=pkb.getPinKeySeq(PinKeySeq.NORMAL);
			KeyboardRandom keyboardRandom = null;
			// If the number is random and the function key is fixed, do not pass the key value sequence.
			if(keySeq!=null){
				keyboardRandom=new KeyboardRandom(initCoordinate,keySeq);
			}else{
				keyboardRandom=new KeyboardRandom(initCoordinate);
			}
			
			byte[] randomCoordinate = pinInput.loadRandomKeyboard(keyboardRandom);
			pkb.loadRandomKeyboardfinished(randomCoordinate);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private DeviceEventListener<K21PininutEvent> pinInputListener = new DeviceEventListener<K21PininutEvent>() {
		@Override
		public Handler getUIHandler() {
			return null;
		}

		@Override
		public void onEvent(K21PininutEvent event, Handler h) {
			spi.play();
			//si se están presionando teclas
			if (event.isProcessing()) {// Inputting
				Log.i(TAG, "is Processing");
				PinInputEvent.NotifyStep notifyStep = event.getNotifyStep();
				if (notifyStep == PinInputEvent.NotifyStep.ENTER) {
					inputLen = inputLen + 1;
					Log.i(TAG, "press key code:" + inputLen);
				} else if (notifyStep == PinInputEvent.NotifyStep.BACKSPACE) {
					inputLen = (inputLen <= 0 ? 0 : inputLen - 1);
					Log.i(TAG, "press cancel code:" + inputLen);
				}
				Message msg = mHandler.obtainMessage(2);
				msg.obj = inputLen;
				msg.sendToTarget();

			} else if (event.isUserCanceled()) {// cancel. si se presiono cancel.
				Log.i(TAG, "Is UserCanceled");
				Intent i = new Intent();
				setResult(RESULT_CANCELED, i);
				finish();
			} else if (event.isSuccess()) {// confirm. si se presiono enter.
				Log.i(TAG, "Is Success");
				byte[] pin = event.getEncrypPin();
				//Setea pin en aplicación global
				//((MyApplication) N900KeyBoardNumberActivity.this.getApplication()).setPin(event.getEncrypPin());
				Log.i(TAG, "input successfully." + ISOUtils.hexString(pin));
				Intent i = new Intent();
				i.putExtra("pin", pin);
				setResult(RESULT_OK, i);
				finish();
			} else {
				Log.i(TAG, "input exception.", event.getException());
				Intent i = new Intent();
				setResult(-2, i);
				finish();
			}
		}
	};
	/**
	 * Gets the length range of the input password
	 * @param pinMinLen Minimum length allowed
	 * @param pinMaxLen Maximum length allowed
	 * @return
	 */
	private byte[] getPinLengthRange(int pinMinLen,int pinMaxLen){
		byte[] sumPinLen = new byte[]{0x00,0x00,0x00,0x00,0x04,0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C};
		byte[] pinLen = new byte[pinMaxLen-pinMinLen+1]; 
		System.arraycopy(sumPinLen, pinMinLen, pinLen, 0, pinLen.length);
		return pinLen;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		spi.release();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);Rect rect = new Rect();
		Window window = getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);
		// status bar height
		int statusBarHeight = rect.top;

		// title bar height + status bar height
		int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();

		// title bar height
		int contentViewHeight = contentViewTop - statusBarHeight;
		// Give it different values depending on the platform.
		Log.i("N900PinKeyBoard", "contentViewHeight="+contentViewHeight+";contentViewTop"+contentViewTop+"statusBarHeight"+statusBarHeight);

		// screen
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Log.i("N900PinKeyBoard", "screen height:" + dm.heightPixels); //952

		// application area
		Rect outRect1 = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
		Log.i("N900PinKeyBoard", "the application top:" + outRect1.top);// the status bar height is 50dp,The navigation bar height is 96dp.
		Log.i("N900PinKeyBoard",  "the application height:" + outRect1.height());

		//View draw area
		Rect outRect2 = new Rect();
		getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect2);
		Log.i("N900PinKeyBoard", "View draw area-error method:" + outRect2.top);   // get outRect2.top don't like the above,it maybe get 0 with outRect2.top, maybe a bug.
		int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();   // right method
		Log.i("N900PinKeyBoard", "View draw area-right method:" + viewTop);  // status bar height + title bar height=146
		Log.i("N900PinKeyBoard", "View draw area height:" + outRect2.height());


		String TOUCHSCREEN_RESOLUTION = NlBuild.VERSION.TOUCHSCREEN_RESOLUTION;
		int height = Integer.valueOf(TOUCHSCREEN_RESOLUTION.split("x")[0]); // Get Touch resolution of K21
		// Geometric scaling
		int width = Integer.valueOf(TOUCHSCREEN_RESOLUTION.split("x")[1]);
		Log.i("N900PinKeyBoard", "TOUCHSCREEN_RESOLUTION：height" + height+"width："+width);
	}
}
