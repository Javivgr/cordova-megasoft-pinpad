package ve.com.megasoft.pinpad.newland.data;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

//import com.newland.example.pointofsale.device.N900Device;
//import com.newland.example.pointofsale.utils.Const;
//import com.newland.example.pointofsale.utils.EventMsg;
//import com.newland.example.pointofsale.utils.MyLogger;

import com.newland.me.SupportMSDAlgorithm;
import com.newland.mtype.common.MESeriesConst;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.EmvTransInfo.AIDSelect;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.module.common.emv.level2.EmvCardholderCertType;
import com.newland.mtype.module.common.emv.level2.EmvLevel2ControllerExtListener;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.K21Swiper;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.ModuleType;
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.ISOUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import ve.com.megasoft.pinpad.newland.modelo.ModeloNewland;

//import de.greenrobot.event.EventBus;

//import static com.newland.example.pointofsale.utils.Const.PIN_FINISH;


/**
 * Created by YJF on 2015/8/14 0014. Emv流程控制监听和QPBOC流程控制监听
 */
public class SimpleTransferListener implements EmvLevel2ControllerExtListener {

	//Constantes
	private static final String TAG = SimpleTransferListener.class.getName();

	//Atributos
	private Activity baseActivity;

	private static int[] L_CAMPO55 = new int[43];
	private static int[] L_2CAMPO55 = new int[7];

	private int isECSwitch = 0;
	private static WaitThreat waitSegCertThreat = new WaitThreat();
	private static WaitThreat waitPinInputThreat = new WaitThreat();
	private static byte[] pinBlock = null;
	private int index;
	private String encryptAlgorithm;
	private Dialog amt_dialog;
	private EditText edit_amt_input;
	private Button btn_sure, btn_cancel;
	private CharSequence temp;
	//private N900Device n900Device;
	private EmvModule emvModule;
	private Handler handlerStartEmv;
	private Handler handlerSegCertEmv;

	private String resultMsg;

	private boolean isTransOnline=false;

	private static String tag39;
	private static String status;
	private static String tag71;
	private static String tag72;
	private static String tag91;

	static {
		L_CAMPO55[0] = 0x50;
		L_CAMPO55[1] = 0x57;
		L_CAMPO55[2] = 0x5A;
		L_CAMPO55[3] = 0x82;
		L_CAMPO55[4] = 0x84;
		L_CAMPO55[5] = 0x8A;
		L_CAMPO55[6] = 0x95;
		L_CAMPO55[7] = 0x9A;
		L_CAMPO55[8] = 0x9B;
		L_CAMPO55[9] = 0x9C;
		L_CAMPO55[10] = 0x5F20;
		L_CAMPO55[11] = 0x5F24;
		L_CAMPO55[12] = 0x5F28;
		L_CAMPO55[13] = 0x5F2A;
		L_CAMPO55[14] = 0x5F30;
		L_CAMPO55[15] = 0x5F34;
		L_CAMPO55[16] = 0x9F02;
		L_CAMPO55[17] = 0x9F03;
		L_CAMPO55[18] = 0x9F06;
		L_CAMPO55[19] = 0x9F07;
		L_CAMPO55[20] = 0x9F09;
		L_CAMPO55[21] = 0x9F0D;
		L_CAMPO55[22] = 0x9F0E;
		L_CAMPO55[23] = 0x9F0F;
		L_CAMPO55[24] = 0x9F10;
		L_CAMPO55[25] = 0x9F12;
		L_CAMPO55[26] = 0x9F16;
		L_CAMPO55[27] = 0x9F1A;
		L_CAMPO55[28] = 0x9F1C;
		L_CAMPO55[29] = 0x9F1E;
		L_CAMPO55[30] = 0x9F21;
		L_CAMPO55[31] = 0x9F26;
		L_CAMPO55[32] = 0x9F27;
		L_CAMPO55[33] = 0x9F33;
		L_CAMPO55[34] = 0x9F34;
		L_CAMPO55[35] = 0x9F35;
		L_CAMPO55[36] = 0x9F36;
		L_CAMPO55[37] = 0x9F37;
		L_CAMPO55[38] = 0x9F39;
		L_CAMPO55[39] = 0x9F40;
		L_CAMPO55[40] = 0x9F41;
		L_CAMPO55[41] = 0x9F4E;
		L_CAMPO55[42] = 0x9F53;

		L_2CAMPO55[0] = 0x9F26;
		L_2CAMPO55[1] = 0x9F27;
		L_2CAMPO55[2] = 0x95;
		L_2CAMPO55[3] = 0x9B;
		L_2CAMPO55[4] = 0x9F5B;
		L_2CAMPO55[5] = 0x8A;
	}

	//Handlers

	/**
	 * Atributo de tipo handler que trata la respuesta del primer certificado y
	 * continúa con el segundo certificado.
	 */
	private static Handler segCertHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				//limpiamos campos 8583
				limpia8583();
				//recuperamos parámetros
				if (msg.obj != null) {
					List res = (List) msg.obj;
					tag39=(String)res.get(0);
					status=(String)res.get(1);
					tag71=(String)res.get(2);
					tag72=(String)res.get(3);
					tag91=(String)res.get(4);
				}
				//notificamos al thread de segundo certificado que continúe el proceso
				waitSegCertThreat.notifyThread();
				break;
			default:
				break;
			}
		}
	};

	private static Handler pinEventHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.d(TAG,"Recibiendo respuesta de handler pinblock emv: "+msg.what);
			pinBlock=null;
			switch (msg.what) {
			case  0:
				if (msg.obj != null) {
					pinBlock = (byte[]) msg.obj;
				}
				waitPinInputThreat.notifyThread();
				break;
			default:
				break;
			}
		}

	};

	//Constructor
	public SimpleTransferListener(Activity baseActivity, EmvModule emvModule,
			Handler handlerStartEmv, Handler handlerSegCertEmv) {
		this.baseActivity = baseActivity;
		this.emvModule = emvModule;
		this.handlerStartEmv = handlerStartEmv;
		this.handlerSegCertEmv = handlerSegCertEmv;
		/* if (((MyApplication) (baseActivity).getApplication()).isDukpt()) {
			index = Const.DUKPTIndexConst.DEFAULT_DUKPT_INDEX;
			encryptAlgorithm = MESeriesConst.TrackEncryptAlgorithm.BY_DUKPT_MODEL;
		} else {
			index = Const.DataEncryptWKIndexConst.DEFAULT_TRACK_WK_INDEX;
			encryptAlgorithm = MESeriesConst.TrackEncryptAlgorithm.BY_UNIONPAY_MODEL;

		} */
	}

	//Metodos Públicos

	/**
	 * método que retorna handler para acceder a listener y continuar con segundo certificado emv
	 * @return (Handler)
	 */
	public static Handler getSegCertHandler() {
		return segCertHandler;
	}

	public static Handler getPinEventHandler() {
		return pinEventHandler;
	}

	public static void setPinEventHandler(Handler pinEventHandler) {
		SimpleTransferListener.pinEventHandler = pinEventHandler;
	}

	//Metodos Privados

	/**
	 * método que limpia los tags obtenidos del primer certificado
	 * @return
	 */
	private static void limpia8583 (){
		tag39=null;
		status=null;
		tag71=null;
		tag72=null;
		tag91=null;
	}

	/**
	 * método que envia primer certificado
	 * @return
	 */
	private void enviarPrimerCertificado(boolean wasSuccessful){
		List<Object> list = new ArrayList<Object>();
		if (wasSuccessful){
			int[] emvTags = new int[2];
			emvTags[0] = 0x5f20;
			emvTags[1] = 0x57;

			//obtenemos tags implícitos en emvTags
			String data = emvModule.fetchEmvData(emvTags);

			//se crea objeto tlv para desglosar los datos
			TLVPackage tlv = ISOUtils.newTlvPackage();
			tlv.unpack(ISOUtils.hex2byte(data));

			//obtiene los tags
			String cardholderName = tlv.getString(0x5f20);
			String track2 = tlv.getString(0x57);

			//obtenemos primer campo 55
			String data55 = emvModule.fetchEmvData(L_CAMPO55);

			//creamos lista para almacenar datos obtenidos
			list.add("ICCARD");
			list.add(cardholderName);
			list.add(track2);
			list.add(data55);
			list.add(pinBlock!=null?ISOUtils.hexString(pinBlock):"null");
		}

		//creamos mensaje con data obtenida
		Message msg = new Message();
		msg.what = wasSuccessful?0:1;
		msg.obj = wasSuccessful?list:resultMsg;

		//entregamos el mensaje al handler
		this.handlerStartEmv.sendMessage(msg);
	}

	/**
	 * método que envia segundo certificado
	 * @return
	 */
	private void enviarSegundoCertificado(){
		//obtenemos segundo campo 55
		String data55 = emvModule.fetchEmvData(L_2CAMPO55);

		//creamos mensaje con data obtenida
		Message msg = new Message();
		msg.what = 0;
		msg.obj = data55;

		//entregamos el mensaje al handler
		this.handlerSegCertEmv.sendMessage(msg);
	}

	//Metodos Sobre Escritos

	@Override  // 当emv  交易正常结束时发生。
	public void onEmvFinished(boolean isSuccess, EmvTransInfo context) throws Exception {
		int executeRslt = context.getExecuteRslt();
		
		switch (executeRslt) {
		case 0:
		case 1:
			resultMsg = "Aceptación de la transacción";
			break;
		case 2:
			resultMsg = "Rechazo de transacciones";
			break;
		case 3:
			resultMsg = "Solicitud en línea";
			break;
		case -2105:
			resultMsg = "El monto de la transacción excede el límite";
			break;
		default:
			resultMsg = "Transacción fallida";
			break;
		}
		Log.d(TAG,">>>>【Transacción completada】，Resultado de la transacción: " + resultMsg );
		int errorCode = context.getErrorcode();
		switch (errorCode) {
		case -6:
			resultMsg = "No se encontraron aplicaciones compatibles";
			break;
		case -11:
			resultMsg = "La autenticación de datos sin conexión falló";
			break;
		case -13:
			resultMsg = "Falló la autenticación del titular de la tarjeta";
			break;
		case -18:
			resultMsg = "Bloqueo de tarjeta";
			break;
		case -1531:
		case -2116:
			resultMsg = "La tarjeta ha caducado";
			break;
		case -1532:
		case -2115:
			resultMsg = "Tarjeta no vigente";
			break;
		case -1822:
			resultMsg = "Saldo electrónico insuficiente en efectivo.";
			break;
		case -1903:
			resultMsg = "EC El importe del depósito supera el límite.";
			break;
		case -1904:
		case -1905:
			resultMsg = "Error de ejecución de script";
			break;
		case -1901:
			resultMsg = "Error de saturación de script";
			break;
		case -2105:
			resultMsg = "La cantidad de entrada de preprocesamiento excede el límite";
			break;
		case -2120:
		case -1441:
			resultMsg = "La tarjeta de crédito electrónica pura no puede estar en línea";
			break;
		case -2121:
			resultMsg = "Tarjeta de rechazo";
			break;
		default:
			resultMsg = null;
			break;
		}
		if (null != resultMsg) {
			Log.d(TAG,">>>>Motivo específico del error: "+errorCode + resultMsg);
		}
		Log.d(TAG,"Traza onEmvFinished: executeRslt: "+executeRslt+" y errorCode: "+errorCode);

		//si la transacción fue fallida, no hay necesidad de enviar segundo certificado.
		if (executeRslt>1 && !isTransOnline){
			Log.d(TAG,"Enviando primer certificado");
			this.enviarPrimerCertificado(false);
		}
		//si la transacción es aceptada
		else{
			//enviamos segundo certificado
			Log.d(TAG,"Se entrega respuesta para segundo certificado");
			this.enviarSegundoCertificado();
		}
	}

	@Override
	public void onError(EmvTransController arg0, Exception arg1) {
		Log.d(TAG,"emv Transacción fallida: " + arg1.getMessage());
		arg1.printStackTrace();
	}

	@Override
	public void onFallback(EmvTransInfo arg0) throws Exception {
		Log.d(TAG,"ic Entorno de comercio de tarjetas no está satisfecho:Reducción de la transacción...");
	}

	/**
	 * 当设备要求联机交易时发生
	 */
	@Override
	public void onRequestOnline(EmvTransController controller, EmvTransInfo context) throws Exception {
		int emvResult = context.getEmvrsltCode();
		this.isTransOnline = true;

		String resultMsgOnline = null;
		switch (emvResult) {
		case 3:
			resultMsgOnline = "pboc En linea";
			break;
		case 15:
			resultMsgOnline = "No conectado qpboc En linea";
			break;
		}
		Log.d(TAG,">>>>Solicitud en línea onRequestOnline，Resultado de ejecucion: " + resultMsgOnline);

		int[] emvTags = new int[4];
		emvTags[0] = 0x5a;
		emvTags[1] = 0x5F34;
		emvTags[2] = 0x5f24;
		emvTags[3] = 0x57;

		String data = emvModule.fetchEmvData(emvTags);
		TLVPackage tlv = ISOUtils.newTlvPackage();
		tlv.unpack(ISOUtils.hex2byte(data));
		/**
		 * getString  的tag信息在 emv 包下面的 linkEmvTransInfo信息中有标注说明
		 */
		String cardNo = tlv.getString(0x5a);
		String cardSN = tlv.getString(0x5F34);// 卡序列号，等效于context.getCardSequenceNumber()
		String expiredDate = tlv.getString(0x5F24);// 过期日期,等效于context.getCardExpirationDate()
		String track2 = tlv.getString(0x57); // 二磁道数据，等效于context.getTrack_2_eqv_data()
		if (cardSN == null) {
			cardSN = "000";
		} else {
			cardSN = ISOUtils.padleft(cardSN, 3, '0');
		}
		String serviceCode = "";
		if (null != track2) {
			serviceCode = track2.substring(track2.indexOf('D') + 5, track2.indexOf('D') + 8);
		}

		Log.d(TAG, "--------------- DATOS OBTENIDOS DE TARJETA ------------------");
		Log.d(TAG,"Numero de tarjeta: " + cardNo);
		Log.d(TAG,"Número de serie de la tarjeta: " + cardSN);
		Log.d(TAG,"Validez de la tarjeta: " + expiredDate);
		Log.d(TAG,"Código de servicio: " + serviceCode);
		Log.d(TAG,"Track 2: " + track2);
		Log.d(TAG, "-------------------------------------------------------------");


		//enviamos primer certificado
		Log.d(TAG,"Se entrega respuesta para primer certificado");
		this.enviarPrimerCertificado(true);
		Log.d(TAG,"Se pausa solicitud online emv hasta recibir respuesta");
		waitSegCertThreat.waitForRslt();
		Log.d(TAG,"Se continúa con procedimiento online emv");

		if (status.equals("00")) {
			Log.d(TAG,"Respuesta de servidor exitosa, ejecutando script");
			// [step2] IC card Online transaction success or connectionless transaction set online return data,and return the result by calling onemvfinished.
			SecondIssuanceRequest request = new SecondIssuanceRequest();
			request.setAuthorisationResponseCode(tag39);// 0x8a Transaction reply code:Taken from the 39 field value of unionpay 8583 specification, this parameter is populated with the actual value of the transaction.
			//request.setIssuerAuthenticationData(ISOUtils.hex2byte(tag91));//发卡行认证数据:取自银联8583规范55域0x91值,该参数按交易实际值填充
			//request.setIssuerScriptTemplate1(ISOUtils.hex2byte(tag71));//发卡行脚本1：取自银联8583规范55域0x71值,该参数按交易实际值填充
			//request.setIssuerScriptTemplate2(ISOUtils.hex2byte(tag72));//发卡行脚本2:取自银联8583规范55域0x72值,该参数按交易实际值填充
			//request.setAuthorisationCode("504343");//0x89 Authorization code

			//TODO: actualmente, el tag 91 hace que la transacción de fallida en algunas tarjetas (todoTicket)
			request.setField55(ISOUtils.hex2byte(tag91+tag71+tag72));// 55 filed data of 8583 message
			controller.secondIssuance(request);
		} else {
			Log.d(TAG,"Respuesta de servidor fallida");
			controller.doEmvFinish(false);
		}
	}

	// 多应用卡片会回调该方法进行应用选择
	@Override
	public void onRequestSelectApplication(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
		Log.d(TAG,"Tarjetas multiaplicaciones, tienes que elegir la aplicación!");
		Map<byte[], AIDSelect> map = arg1.getAidSelectMap();
		List<String> nameList = new ArrayList<String>();
		List<byte[]> aidList = new ArrayList<byte[]>();

		for (Entry<byte[], AIDSelect> entry : map.entrySet()) {
			nameList.add(entry.getValue().getName());
			aidList.add(entry.getValue().getAid());
			Log.d(TAG,"aidName:" + entry.getValue().getName());
			Log.d(TAG,"aid:" + entry.getValue().getAid());
		}
		// 默认选择第一个应用
		arg0.selectApplication(aidList.get(0));
	}

	
	@Override
	public void onRequestTransferConfirm(EmvTransController controller, EmvTransInfo arg1) throws Exception {
		Log.d(TAG,"Confirmación de transacción completada");
		controller.transferConfirm(true);
	}

	/***
	 * 当设备要求app完成一个密码输入过程时发生
     * 若在设备上完成密码输入，则该事件不触发
	 */
	// IM81和N900  N910 会触发，  、ME31不会触发
	@Override
	public void onRequestPinEntry(final EmvTransController emvTransController, EmvTransInfo emvTransInfo) throws Exception {
		Log.d(TAG,"onRequestPinEntry");
		if (emvTransInfo.getCardNo() != null) {

			doPinInput(true,emvTransInfo);
			Log.d(TAG,"Pausando transacción emv");
			waitPinInputThreat.waitForRslt();
			Log.d(TAG,"Pinblock obtenido en emv: "+ISOUtils.hexString(pinBlock));
			Log.d(TAG,"El largo es: "+pinBlock.length);
			emvTransController.sendPinInputResult(pinBlock!=null?new byte[]{0x00,0x00,0x00,0x00,0x00,0x00}:null);
			//EventBus.getDefault().post(new EventMsg(Const.EvenBUSType.READ_CARD_SUCCSE,emvTransInfo.getCardNo()));
			return;
		} else {
			//EventBus.getDefault().post(new EventMsg(Const.EvenBUSType.READ_CARD_ERR));
			Log.d(TAG,"Error en lectura de tarjeta");
			return;
		}
	}

	/**
	 * input password
	 * @param isOnline is it online pin?
	 * @param emvTransInfo emvTransInfo
	 * @throws Exception
	 */
	public void doPinInput(boolean isOnline,EmvTransInfo emvTransInfo) throws Exception {
		if(isOnline){

			Log.d(TAG,"Obteniendo pin handler");
			ArrayList<String> list = new ArrayList<String>();
			list.add(emvTransInfo.getCardNo());
			list.add("true");
			list.add(emvTransInfo.getAmountAuthorisedNumeric());
			

			Message msg = new Message();
            msg.what = 0;
            msg.obj = list;
            ModeloNewland.getPinHandler().sendMessage(msg);
			Log.d(TAG,"Obteniendo handler de Modelo Newland en STL");
		}else{
			/* Message msg = MainActivity.getPinHandler().obtainMessage(2);
			Bundle bundle = new Bundle();
			bundle.putByteArray("modulus", emvTransInfo.getModulus());
			bundle.putByteArray("exponent", emvTransInfo.getExponent());
			msg.setData(bundle);
			msg.sendToTarget(); */
		}
	}

	/**
	 * Whether to intercept acctType select event
	 * @return
	 */
	@Override
	public boolean isAccountTypeSelectInterceptor() {
		return true;
	}

	/**
	 * Whether to intercept the cardHolder certificated confirmation  event
	 * @return
	 */
	@Override
	public boolean isCardHolderCertConfirmInterceptor() {
		return true;
	}

	/**
	 *  whether intercept electron cash confirmation event
	 * 
	 * @return
	 */
	@Override
	public boolean isEcSwitchInterceptor() {
		return true;
	}

	/**
	 * Whether intercept to use  external sequence number processor
	 * @return
	 */
	@Override
	public boolean isTransferSequenceGenerateInterceptor() {
		return true;
	}

	/**
	 * Whether intercept to show message on LCD event
	 * 
	 * @return
	 */
	@Override
	public boolean isLCDMsgInterceptor() {
		return true;
	}


	/**
	 *  account type selection
	 *  <p> 
	 *  return to int range
	 *  <p>
	 *  <ol>
	 *  <li>default</li>
	 *  <li>savings</li>
	 *  <li>Cheque/debit</li>
	 *  <li>Credit</li>
	 *  </ol>
	 *	
	 *  @return 1-4:selection range， －1：failed
	 */
	@Override
	public int accTypeSelect() {
		return 1;
	}

	/**
	 *  cardHolder certificated confirmation
	 *  <p>
	 * 
	 *  @return true:confirmation succeed， false:confirmation failed
	 */
	@Override
	public boolean cardHolderCertConfirm(EmvCardholderCertType certType, String certno) {
		return true;
	}

	/**
	 * 电子现金/emv选择
	 * <p>
	 * 交易返回：
	 * <p>
	 * <ul>
	 * <li>1：继续电子现金交易</li>
	 * <li>0：不进行电子现金交易</li>
	 * <li>－1:用户中止</li>
	 * <li>－3:超时</li>
	 * </ul>
	 */
	@Override
	public int ecSwitch() {
		Log.d(TAG,"ecSwitch");
		try {
			final WaitThreat waitThreat = new WaitThreat();
			final Builder builder = new Builder(baseActivity);
			builder.setMessage("是否使用电子现金消费？");
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
//					SharedPreferencesUtil.setIntParam(baseActivity, "isECSwitch", 1);
					dialog.dismiss();
					isECSwitch = 1;
					waitThreat.notifyThread();
				}
			});
			builder.setNegativeButton("否", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
//					SharedPreferencesUtil.setIntParam(baseActivity, "isECSwitch", 0);
					dialog.dismiss();
					isECSwitch = 0;
					waitThreat.notifyThread();
				}
			});
			baseActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					builder.show();
					builder.setCancelable(false);
				}
			});
			waitThreat.waitForRslt();
			// 电子现金消费返回1，否则返回0
			return isECSwitch;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	/**
	 * serial number Add 1 and return
	 * 
	 * @return
	 */
	@Override
	public int incTsc() {
		return 0;
	}

	@Override
	public boolean isLanguageselectInterceptor() {
		return false;
	}

	/**
	 *  display info
	 * 
	 *  @param title
	 *             title
	 *  @param msg
	 *             message
	 *  @param yesnoShowed
	 *             whether show yes no
	 *  @param waittingTime
	 *             waiting time
	 *  @return if yesnoShow is equal to true, return 1 means confirmation.
	 *  		return 0 means cancel.
	 *  		if yesnoShow is equal to false,return value has no meaning.
	 */
	@Override
	public int lcdMsg(String title, String msg, boolean yesnoShowed, int waittingTime) {
		//baseActivity.showMessage("title:"+title+";msg="+msg,MessageTag.DATA);
		return 1;
	}

	@Override
	public byte[] languageSelect(byte[] language, int len) {
		//baseActivity.showMessage("languageSelect language=:"+(language==null?null:ISOUtils.hexString(language)),MessageTag.DATA);
        if(len>=2){
           return new byte[]{language[0],language[1]};
        }
		return null;
	 }

	// thread wait 、awake
	public static class WaitThreat {
		Object syncObj = new Object();

		void waitForRslt() throws InterruptedException {
			synchronized (syncObj) {
				syncObj.wait();
			}
		}

		void notifyThread() {
			synchronized (syncObj) {
				syncObj.notify();
			}
		}
	}

	@Override
	public void onRequestAmountEntry(final EmvTransController controller, EmvTransInfo context) {
		Log.d(TAG,"Cantidad de solicitud de devolución de llamada...");
	}
	 
}
