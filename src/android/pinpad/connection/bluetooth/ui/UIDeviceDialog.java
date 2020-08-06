/**
 * Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.connection.bluetooth.ui;


import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import ve.com.megasoft.pinpad.connection.bluetooth.adapter.BondedDeviceAdapter;
import ve.com.megasoft.pinpad.connection.bluetooth.bean.BeanBluetoothDevice;
import ve.com.megasoft.pinpad.util.WinUtils;

/**
 * 
 * interfaz de dialogo para la carga de dispositivos Bluetooth, 
 * realizando la coneccion segun lo indica el modelo invocador
 * 
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 * TODO -- REVISAR TIMEOUT DE LA INTERFAZ
 *
 */
public class UIDeviceDialog extends Dialog implements android.view.View.OnClickListener,  OnItemClickListener{

	//CONSTANTES
	private static final String TAG = UIDeviceDialog.class.getName();
	
	//Atributos 
	//Primitivas
	private String packageName;
	private Resources resources;
	private List<BeanBluetoothDevice> devices;
	private BeanBluetoothDevice selectedDevice;
	private boolean showing;
//	private int timeout;
	private boolean stopThread = false;
	
	//Adapter's
	private BondedDeviceAdapter bondedDeviceAdapter;
	
	//UI
	private Context context;
	private ListView bondedListView;
	private Button configBtn;
	private Button cancelBtn;
	private TextView noconnectView;
	
	//Thread
	/**
	 * hilo de timeout en ejecucion 
	 */
	/*
	private Thread timeoutThread = new Thread(new Runnable() {
		
		@SuppressWarnings("static-access")
		@Override
		public void run() {
			Message msg = new Message();
			boolean running = true;
			int count = 0;
			while (count < timeout && !stopThread){
				try {timeoutThread.sleep(1000);} 
				catch (InterruptedException e) {
					running = false;
					Log.e(TAG, "hilo de timeout detenido, error?:",e);
				}
				count++;
			}
			if(running && !stopThread){
				msg.what=5;
				msg.obj="Timeout de espera alcansado";
				responseHandler.sendMessage(msg);
				dismiss();
			}
		}
	});
	*/
	
	//Handlers
	private Handler responseHandler;
	@SuppressLint("HandlerLeak")
	private Handler deviceChangeHandler = new Handler(){
		
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			//actualizamos la lista de dispositivos
			devices = (List<BeanBluetoothDevice>) msg.obj;
			if(devices.size()==0){noconnectView.setVisibility(View.VISIBLE);}
			else{noconnectView.setVisibility(View.GONE);}
			
			//notificamos el cambio de informacions
			bondedDeviceAdapter.notifyDataSetChanged();
		};
		
	};
	
	//Constructores
	/**
	 * constructor de la ventana de dialogo
	 * @param context (Context) contexto donde se ejecuta la aplicacion
	 * @param devices (List[BeanBluetoothDevice]) listado de dispositivos bluetooth disponibles
	 */
	public UIDeviceDialog(Context context, List<BeanBluetoothDevice> devices, Handler responseHandler) {
		super(context);
		
		Log.i(TAG, "creando cuadro de dialogo para seleccion de dispositivo");
		Log.d(TAG, "Parametros, context: "+context+" devices: "+devices+" responseHandler: "+responseHandler);
		
		this.context = context;
		
		//recuperamos el nombre de paquete y el listado de recursos
		packageName = context.getPackageName();
		resources = context.getResources();
		
		Log.i(TAG, "inicializando proceso de emparejamiento");
		
		this.devices = devices;
		this.responseHandler = responseHandler;
	}
	
	/**
	 * constructor de la ventana de dialogo
	 * @param context (Context) contexto donde se ejecuta la aplicacion
	 * @param themeResId (int) id del tema a usar
	 * @param devices (List[BeanBluetoothDevice]) listado de dispositivos bluetooth disponibles
	 */
	public UIDeviceDialog(Context context, int themeResId, List<BeanBluetoothDevice> devices, Handler responseHandler) {
		super(context, themeResId);
		
		Log.i(TAG, "creando cuadro de dialogo para seleccion de dispositivo");
		Log.d(TAG, "Parametros, context: "+context+" themeResId: "+themeResId+" devices: "+devices+" responseHandler: "+responseHandler);
		
		this.context = context;
		
		//recuperamos el nombre de paquete y el listado de recursos
		packageName = context.getApplicationContext().getPackageName();
		resources = context.getResources();
		
		Log.i(TAG, "inicializando proceso de emparejamiento");
		
		this.devices = devices;
		this.responseHandler = responseHandler;
	}
	
	//Metodos Sobre Escritos
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "Ejecutando onCreate, cargando elementos visuales");
		
		//enlazamos los elementos visuales
		setContentView(resources.getIdentifier("bonded_devices", "layout", packageName));
		bondedListView = (ListView) findViewById(resources.getIdentifier("bonded_list", "id", packageName));
		configBtn = (Button) findViewById(resources.getIdentifier("set_btn", "id", packageName));
		cancelBtn = (Button) findViewById(resources.getIdentifier("cancel", "id", packageName));
		noconnectView = (TextView) findViewById(resources.getIdentifier("no_connect", "id", packageName));
		
		Log.d(TAG, "Componente bondedListView "+bondedListView);
		Log.d(TAG, "Componente configBtn "+configBtn);
		Log.d(TAG, "Componente cancelBtn "+cancelBtn);
		Log.d(TAG, "Componente noconnectView "+noconnectView);
		
		//levantamos los adaptadores
		Log.i(TAG, "Ejecutando onCreate, asociamos el adaptador");
		bondedDeviceAdapter = new BondedDeviceAdapter(context, devices);
		bondedListView.setAdapter(bondedDeviceAdapter);
		
		//verificamos la lista de dispositivos registrados
		if(devices.size()==0){noconnectView.setVisibility(View.VISIBLE);}
		else{noconnectView.setVisibility(View.GONE);}
		
		//seteamos los listener de eventos
		Log.i(TAG, "Ejecutando onCreate, seteamos los eventos");
		configBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		bondedListView.setOnItemClickListener(this);
		
		//ajustes finales de interfaz
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.alpha = 0.9f;
		this.getWindow().setAttributes(lp);
		
		//iniciamos el hilos the timeout
//		timeoutThread.start();
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == resources.getIdentifier("set_btn", "id", packageName)){
			Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
			context.startActivity(intent);
		}
		else if(v.getId() == resources.getIdentifier("cancel", "id", packageName)){
			Message msg = new Message();
			msg.what=3;
			msg.obj="Proceso cancelado por el usuario";
			responseHandler.sendMessage(msg);
			stopThread = true;
			showing = false;
			dismiss();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		selectedDevice = (BeanBluetoothDevice) devices.get(position);
		BluetoothDevice btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(selectedDevice.getDeviceAddress());
		Message msg = new Message();
		if(btDevice.getBondState() == BluetoothDevice.BOND_BONDED){
			if(!selectedDevice.getDeviceAddress().equals("null")){
				Log.i(TAG, "Dispositivo bluetooth seleccionado, nombre: "+selectedDevice.getDeviceName()+", Dirección: "+selectedDevice.getDeviceAddress());
				WinUtils.makeToast(context, "Dispositivo Seleccionado: "+selectedDevice.getDeviceName(), false);
				
				//armamos el mensaje de respuesta
				msg.what = 0;
				msg.obj = btDevice;
				
			}
			else{
				Log.e(TAG, "Dispositivo bluetooth invalido, no posee una direccion de coneccion, direccion: "+selectedDevice.getDeviceAddress());
				WinUtils.makeToast(context, "Dispositivo invalido, nombre: "+selectedDevice.getDeviceName(), false);
				selectedDevice = null;
				
				//armamos el mensaje de respuesta
				msg.what = 1;
				msg.obj = "Dispositivo bluetooth no posee una direccion de comunicación";
			}
		}
		else{
			Log.e(TAG, "Dispositivo bluetooth selecionado no asociados");
			WinUtils.makeToast(context, "Dispositivo invalido", false);
			selectedDevice = null;
			
			//armamos el mensaje de respuesta
			msg.what = 2;
			msg.obj = "Dispositivo bluetooth no asociado";
		}
		
		responseHandler.sendMessage(msg);
		stopThread = true;
		showing = false;
		this.dismiss();
	}

	//Getter's && Setter's
	public BeanBluetoothDevice getSelectedDevice() {
		return selectedDevice;
	}

	public void setSelectedDevice(BeanBluetoothDevice selectedDevice) {
		this.selectedDevice = selectedDevice;
	}

	public Handler getDeviceChangeHandler() {
		return deviceChangeHandler;
	}

	
	public boolean isShowing() {
		return showing;
	}

	public void setShowing(boolean showing) {
		this.showing = showing;
	}

	
	public boolean isStopThread() {
		return stopThread;
	}

	public void setStopThread(boolean stopThread) {
		this.stopThread = stopThread;
	}
	
}
