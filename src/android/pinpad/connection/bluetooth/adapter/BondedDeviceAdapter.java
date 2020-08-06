/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.connection.bluetooth.adapter;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ve.com.megasoft.pinpad.connection.bluetooth.bean.BeanBluetoothDevice;

/**
 * 
 * adaptador para el manejo de los dispositivos bluetooth emparejados
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class BondedDeviceAdapter extends BaseAdapter {

	//CONSTANTES
	private static final String TAG = BondedDeviceAdapter.class.getName();
	
	//Atributos
	private String packageName;
	private Resources resources;
	
	private List<BeanBluetoothDevice> list;
	private LayoutInflater inflater;
	
	//Class
	class ViewHolder{
		protected View child;
		protected TextView msg;
		
		public ViewHolder(View child, TextView msg){
			this.child = child;
			this.msg = msg;
		}
	}
	
	//Constructor
	/**
	 * constructor del adaptador
	 * @param ctx (Context) contexto de la aplicacion 
	 * @param lista (list[BeanBluetoothDevice]) dispositivos emparejados
	 */
	public BondedDeviceAdapter(Context ctx, List<BeanBluetoothDevice> lista) {
		Log.i(TAG, "Creando adaptador listado dispositivo");
		Log.d(TAG, "Parametros ctx: "+ctx+" lista: "+lista);
		
		packageName = ctx.getPackageName();
		resources = ctx.getResources();
		
		Log.i(TAG,"Parametros de contexto recuperados, inflando lista ");
		
		list = lista;
		inflater = LayoutInflater.from(ctx);
	}
	
	//Metodos Sobre Escritos
	@Override
	public int getCount() {return list.size();}

	@Override
	public Object getItem(int position) {return list.get(position);}

	@Override
	public long getItemId(int position) {return position;}

	@Override
	public int getItemViewType(int position) {return position;}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		BeanBluetoothDevice item = list.get(position);
		
		if(convertView == null){
			convertView = inflater.inflate(resources.getIdentifier("list_item", "layout", packageName), null);
			viewHolder = new ViewHolder(
				(View) convertView.findViewById(resources.getIdentifier("list_child", "id", packageName)),
				(TextView) convertView.findViewById(resources.getIdentifier("msg", "id", packageName))
			);
			convertView.setTag(viewHolder);
		}
		else{viewHolder = (ViewHolder) convertView.getTag();}
		
		String deviceName = item.getDeviceName();
		viewHolder.msg.setText(deviceName);
		return convertView;
		
	}

}
