/**
 * Mega Soft Computaci�n C.A.
 */
package ve.com.megasoft.pinpad.verifone.command;

import java.text.ParseException;

import android.util.Log;
import ve.com.megasoft.pinpad.bean.BeanAidsInfo;
import ve.com.megasoft.pinpad.bean.BeanEmvKeyInfo;
import ve.com.megasoft.pinpad.util.ConversorNumerico;
import ve.com.megasoft.pinpad.util.Utils;

/**
 * 
 * clase para la construccion de comandos para los 
 * 
 * pinpads Verifone
 * 
 * Modelo Contemplados
 * e265, e355, vx810, vx820
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class CommandBuilder {
	//CONSTANTES
	private static final String TAG = CommandBuilder.class.getName();
	
	//Validaciones
	private static final int MAXIMA_LONGITUD_MODULO_LLAVE = 496;

	//configuracion
		//pinblock
	private static final String minPin = "04";
	private static final String maxPin = "04";
	private static final String mensajeMonto = "Monto: ";
	private static final String mensajeProc = "Procesando.";
	
	//Comandos
	//#
	public static final String COMANDO_06 = "06";
	public static final String COMANDO_08 = "08";	
	
	//E
	public static final String COMANDO_E01 = "E01";
	public static final String COMANDO_E02 = "E02";
	public static final String COMANDO_E03 = "E03";	
	public static final String COMANDO_E04 = "E04";	
	public static final String COMANDO_E06 = "E06";		
	public static final String COMANDO_E07 = "E07";
	public static final String COMANDO_E0B = "E0B";
	
	//Z
	public static final String COMANDO_Z1 = "Z1";
	public static final String COMANDO_Z2 = "Z2";
	public static final String COMANDO_Z8 = "Z8";
	public static final String COMANDO_Z62 = "Z62";
	public static final String COMANDO_Z9030 = "Z9030";
	public static final String COMANDO_Z9033 = "Z9033";
	public static final String COMANDO_Z9230 = "Z9230";
	public static final String COMANDO_Z9233 = "Z9233";
	
	//Comandos Respuesta
	public static final String COMANDO_RESPUESTA_E10 = "E10";
	public static final String COMANDO_RESPUESTA_E1A = "E1A";
	public static final String COMANDO_RESPUESTA_E12 = "E12";
	public static final String COMANDO_RESPUESTA_E13 = "E13";
	public static final String COMANDO_RESPUESTA_E14 = "E14";	
	
	public static final int COMANDO_RESPUESTA_BANDA = 0;
	public static final int COMANDO_RESPUESTA_CHIP = 2;
	
	//Delimitadores
	public static final byte STX = 0x2;
	public static final byte ETX = 0x3;
	public static final byte SI = 0xF;
	public static final byte SO = 0xE;
	
	//respuestas
	public static final byte ACK = 0x6;
	public static final byte NAK = 0x15;
	public static final byte EOT = 0x4;
	public static final byte CAN = 0x18;
	
	//otros
	private static final char SUB =  (char)26;
//	private static final byte SUBBYTE = 0x1A; 
	
	//Atributos
		//mensaje
	private static String mensaje = null;
		//Indicadores
	public static boolean emuladorTAGCero = true;
	
	//Metodos Privados 
	/**
	 * funcion que realiza la construccion de los comando a ser ejecutados en los pinpads
	 * @param mensaje (String) comando y parametros requeridos por el comando 
	 * @param cIni (byte) apertura o inicio de comando 
	 * @param cFin (byte) cierre o fin de comando 
	 * @return (byte[]) comando generado para su ejecucion en el pinpad
	 */
	private static byte[] construirComando(String mensaje, byte cIni, byte cFin) {
		byte[] comandoParaCalcularLRC = new byte[mensaje.length() + 1];
		byte[] comandoCompleto = new byte[mensaje.length() + 3];
		int data;
		comandoCompleto[0] = cIni;
		for (int i = 0; i < mensaje.length(); i++) {
			data = mensaje.charAt(i);
			comandoParaCalcularLRC[i] = (byte) data;
			comandoCompleto[i + 1] = (byte) data;
		}
		comandoParaCalcularLRC[comandoParaCalcularLRC.length - 1] = cFin;
		comandoCompleto[comandoCompleto.length - 2] = cFin;
		int lrcComando = lrc(comandoParaCalcularLRC);
		comandoCompleto[comandoCompleto.length - 1] = (byte)lrcComando;
		return comandoCompleto;
	}
	
	//Metodos Publicos
	//verificadores
	/**
	 * verifica si la respuesta entregada por el pinpad por la ejecucion 
	 * de un comando esta entre los posibles valores positivos de la misma
	 * @param comando (String) comando enviado
	 * @param valor (String) respuesta entregada
	 * @return (Boolean) true si esta entre los posibles valores, false en caso contrario.
	 */
	public static boolean isPositive(String comando, String valor) {
		
		//los posible valores positivos
		String tempValor = "";
		if      (comando.equals(COMANDO_Z9033)){tempValor = "00,02";} 
		else if (comando.equals(COMANDO_Z9030)){tempValor = "00,02";}
		else if (comando.equals(COMANDO_Z9230)){tempValor = "00,02";}
		else if (comando.equals(COMANDO_Z9233)){tempValor = "00,02";}
		else if (comando.equals(COMANDO_E01))  {tempValor = "00";} 
		else if (comando.equals(COMANDO_E02))  {tempValor = "00";} 
		else if (comando.equals(COMANDO_E03))  {tempValor = "00,08,10";} 
		else if (comando.equals(COMANDO_E04))  {tempValor = "00";} 
		else if (comando.equals(COMANDO_E06))  {tempValor = "00";} 
		else if (comando.equals(COMANDO_E07))  {tempValor = "00";} 
		else if (comando.equals(COMANDO_Z62))  {tempValor = "71";}
		
		//verifica si la respuesta esta entre los posibles valores positivos
		String [] temp = tempValor.split(",");
		for (int i=0; i<temp.length; i++){
			if (temp[i].equals(valor)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * verifica si el proceso de lectura fue producto de un fallback
	 * @param comando (String) comando ejecutado
	 * @param valor (String) valor recibido
	 * @return (boolean) true para indicar fallback, false en caso contrario
	 */
	public static boolean isFallback(String comando, String valor) {
		
		//los posible valores positivos
		String tempValor = "";
		if (comando.equals(COMANDO_E01)) {
			tempValor = "01,02,03,08,A1,B1,A3";	
		}
		
		//verifica si la respuesta esta entre los posibles valores positivos
		String [] temp = tempValor.split(",");
		for (int i=0; i<temp.length; i++){
			if (temp[i].equals(valor)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * verifica que el codigo entregado por la ejecucion del comando 
	 * indica una falla
	 * @param comando (String) comando ejecutado
	 * @param valor (String) valor obtenido
	 * @return (boolean) true para indicar que fallo, false en caso contrario.
	 */
	public static boolean isFail(String comando, String valor) {
		
		//los posible valores positivos segun el comando
		String tempValor = "";
		if      (comando.equals(COMANDO_Z9033)){tempValor = "70,71,72,73,74,75,80";}
		else if (comando.equals(COMANDO_Z9233)){tempValor = "70,73,74,75";}
		else if (comando.equals(COMANDO_E01))  {tempValor = "05,06,08,09,70,72,76,77";} 
		else if (comando.equals(COMANDO_E03))  {tempValor = "05,06,08,10,70,73";}
		
		//verifica si la respuesta esta entre los posibles valores positivos
		String [] temp = tempValor.split(",");
		for (int i=0; i<temp.length; i++){
			if (temp[i].equals(valor)) {
				return true;
			}
		}
		return false;
	}
	
	//Setter's
	/**
	 * procedimiento que settea el mensaje a mostrar en la pantalla del pinpad durante la captura del pinblock
	 * @param message
	 */
	public static void setMessage(String message){mensaje = message;}
	
	//TODO - extraer mensajes de los recurso
	/**
	 * funcion que retorna los mensajes asociados a los codigos de error del pinpad
	 * @param valor (String) valor a verificar
	 * @return (String) mensaje de error asociado, o default si este codigo no esta tipificado.
	 */
	public static String getFailMessage(String valor){
		int failCode = Integer.parseInt(valor);
		
		switch (failCode) {
			case 5: return "cancelado por el usuario";
			case 6: return "tarjeta EMV extra&#237;da o no presente";
			case 8: return "error en la transacci&#243;n EMV";
			case 9: return "tiempo m&#225;ximo de lectura alcanzado";
			case 10: return "error en la transacci&#243;n EMV";
			case 12: return "error inicializando los AIDS";
			case 13: return "no se pueden actualizar AIDS / Llaves EMV";
			case 70: return "error de formato";
			case 73: return "tiempo m&#225;ximo de captura alcanzado";
			case 74: return "error leyendo banda";
			case 75: return "cancelado por el usuario";
			case 76: return "montos de transacci&#243;n inv&#225;lidos";
			case 77: return "error en la transacci&#243;n EMV";
			case 80: return "transacci&#243;n EMV no iniciada";
			default: return "tarjeta o proceso no ejecutado";
		}
	}
	
	//Utils
	/**
	 * funcion que realiza el calculo del caracter de chequeo del comando 
	 * @param mensaje (byte[]) comando a calcular
	 * @return (byte) caaracter de chequeo calculado
	 */
	public static byte lrc(byte[] mensaje) {
		int lrcCalculado = 0;
		if (mensaje.length > 0) {
			lrcCalculado = mensaje[0];
			for (int i = 1; i < mensaje.length; i++) {
				//el caracter se obtiene utilizando la operacion XOR de los bytes del comando
				lrcCalculado ^= mensaje[i]; 
			}
		}
		return (byte)lrcCalculado;
	}
	
	/**
	 * funcion que calcula el lrc del mensaje y lo campara con el entregado
	 * @param msg (byte[]) cadenas de bytes verificar
	 * @param lrc (int) lrc contra el cual se verificara 
	 * @return true si son iguales, false en caso contrario.;
	 */
	public static boolean verificarLrc(byte[] msg, int lrc){
		int lrcOfMsg = lrc(msg);
		return ((lrcOfMsg == lrc) || (lrcOfMsg == 13 && lrc == 10))?true:false;
	}
	
	//#
	/**
	 * funcion que genera el comando requerido para la solicitud de serial del pinpad
	 * @return (byte[]) comando a ser ejecutado en el pinpad 
	 */
	public static byte[] getComando06(){return construirComando(COMANDO_06, SI, SO);}
	
	/**
	 * funcion que genera el comando requerido para indicar el indice working key a usar para la solicitud de pinblock
	 * @param indiceWk (String) indice de working key
	 * @return (byte[]) comando generado
	 */
	public static byte[] getComando08(String indiceWk){
		//Iniciamos el comando
		StringBuffer sb = new StringBuffer(COMANDO_08);
		
		//colocamos el indice de la workingKey
		sb.append(indiceWk);
		
		//Entregamos el comando 
		return construirComando(sb.toString(), SI, SO);
	}
	
	//E
	/**
	 * funcion que genera el comando requerido para la solicitud de lectura chip de la tarjeta
	 * @param tipoTransaccion (String) tipo de transaccion a ejecutar
	 * @param monto (String) monto de la transaccion
	 * @param avance (String) monto del avance 
	 * @return (byte[]) comando a ser ejecutado en el pinpad 
	 * @throws Exception - validacion de la informacion entregada
	 */
	public static byte[] getComandoE01(String tipoTransaccion, String monto, String avance) throws Exception{
		
		//iniciamos la contruccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_E01);
		
		//colocamos el tipo de transaccion
		if(tipoTransaccion==null || tipoTransaccion.equals("")){throw new Exception("No se recibio el monto de la transacci�n");}
		sb.append(tipoTransaccion);
		
		//colocamos el monto de la transaccion
		if(monto==null || monto.equals("") || monto.equals("0")){throw new Exception("No se recibio un monto de transacci�n valido");}
		if(monto.length()>0){
			while(monto.length()<12){monto = "0"+monto;}
			sb.append('M');
			sb.append('T');
			sb.append(monto);
		}
		
		//colocamos el monto de avance si este esta presente
		if(avance!=null && !avance.equals("") && !avance.equals("0")){
			while(avance.length()<12){avance = "0"+avance;}
			sb.append('M');
			sb.append('A');
			sb.append(avance);
		}
		
		//construimos y entregamos el comando
		return construirComando(sb.toString(), STX, ETX);
	}
	
	/**
	 * funcion que genera el comando para la solicitud de pinblock de una tarjeta
	 * chip en un proceso EMV
	 * @param indiceWk (String) indice que se usara para identificar la llave
	 * @param workingKey (String) llave con la que se encriptara 
	 * @param montoTransaccion (String) monsto de la transaccion sin formatear
	 * @return (byte[]) comando solicitado
	 */
	public static byte[] getComandoE02(String indiceWk, String workingKey, String montoTransaccion){
		
		//formateamos el monto a ser mostrado en pantalla
		new ConversorNumerico(",");
		String montoTxn= (montoTransaccion!=null)?ConversorNumerico.formatMontoString(montoTransaccion,true):" ";
		
		//iniciamos la construccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_E02);
		
		//colocamos el indice de workingkey y la working key
		sb.append(indiceWk);
		sb.append(workingKey);
		
		//indicamos la longitud minima y maxima del pinblock
		sb.append(minPin).append(maxPin);
		
		//indicamos que no permitimo pin nulos
		sb.append("N");
		
		//Colocamos el mensaje a mostrar en la primera linea
		sb.append((mensaje!=null)?mensaje:mensajeMonto).append((char) 28);
		
		//Colocamos el monto de la transaccion en la segunda linea
		sb.append(montoTxn).append((char) 28); 
		
		//colocamos el mensaje de procesamiento de pinbloc
		sb.append(mensajeProc);
		
		//colocamos la longitud de la workingKey
		int wkLength = workingKey.length();
		sb.append((wkLength == 32)?((char) 28 + "1"):(wkLength==16)?((char)28+"0"):"0");
		
		//retornamos el comando solicitado
		return construirComando(sb.toString(), STX, ETX);
	}
	
	/**
	 * funcion que genera el comando para la ejecucion de un segundo certificado 
	 * @param tag39 (String) codigo respuesta de merchant - 2 caracters
	 * @param indicador (String) indicador de comunicacion con merchant 0 aprobada, 1 rechazada, 2 no hubo respuesa
	 * @param tlv (String) contiene los tags 91,71,72 para la ejecucion del certificaod
	 * @return (byte[]) comando contrstuido
	 */
	public static byte[] getComandoE03(String tag39, String indicador, String tlv){
		
		//iniciamos la contruccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_E03);
		
		//colocamos la respuesta recibida de merchant
		sb.append(tag39);
		
		//colocamo el estado de respuesta del host
		sb.append(indicador);
		
		//colocamos el tlv con los tags requeridos para la ejecucion del comando
		sb.append(tlv);
		
		//retornamos el comando solicitado
		return construirComando(sb.toString(), STX, ETX);
	}
	
	/**
	 * funcion que genera el comando para la carga de una llave publica emv
	 * @param info (BeanEmvKeyInfo) llave emv que se desea cargar
	 * @return (byte[]) comando Construido
	 * @throws ParseException 
	 */
	public static byte[] getComandoE06(BeanEmvKeyInfo info) throws ParseException{
		Log.i(TAG, "Armando Comando E06");

		if(info.getModulo().length()>MAXIMA_LONGITUD_MODULO_LLAVE){
			return null;
		}
		
		//iniciamos la contruccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_E06);
		
		//colocamos la informacion del comando
		Log.i(TAG, "Colocando RID e Indice");
		sb.append(info.getRid());
		sb.append(info.getIndice());
		
		//procesamo la longitud como se requiere 
		Log.i(TAG, "Colocando Longitud");
		String longitud = Integer.toString(info.getModulo().length()/2);
		switch (longitud.length()) {
			case 1:{longitud = "00"+longitud; break;}
			case 2:{longitud = "0"+longitud; break;}
		}
		sb.append(longitud);
		
		//colocamos el modulo y el exponente en el formato requerido
		Log.i(TAG, "Colocando Modulo y exponente");
		sb.append(info.getModulo());
		sb.append((info.getExponente().length()==1)?"0"+info.getExponente():info.getExponente());
		
		//procesamos la marca de la llave
		Log.i(TAG, "Colocando marca");
		String marca = BeanEmvKeyInfo.aidsSchema.get(info.getRid());
		sb.append((marca.length()<10)?"0"+Integer.toString(marca.length()):Integer.toString(marca.length()));
		sb.append(marca);
		
		//colocamos el hash
		if(info.getHash()!=null && !info.getHash().equals("")){
			Log.i(TAG, "Colocando HASH de la llave");
			sb.append(info.getHash());
		}
		
		//procesamos la fecha de vencimiento
		Log.i(TAG, "Formateando y colocando fecha de expiraci�n");
		sb.append(Utils.dateToString(info.getFechaDeExpiracion(), Utils.DATE_YEAR_MONTH_DAY_WITHOUT_SEPARATOR));
		
		//colocaion de los tac requeridos en el orden indicado
		Log.i(TAG, "Colocando TAC");
		sb.append(info.getTACDefault()).append(info.getTACDenial()).append(info.getTACOnline());
		
		Log.d(TAG, "Comando E06 generado: "+sb.toString());
		
		//retornamos el comando solicitado
		return construirComando(sb.toString(), STX, ETX);
	}
	
	/**
	 * funcion que genera el comando para la carga de un aid en el pinpad
	 * @param aid (BeanAidsInfo) informacion del aid a cargar en el pinpad 
	 * @return (byte[]) comando Construido
	 */
	public static byte[] getComandoE07(BeanAidsInfo aid){
		//iniciamos la contruccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_E07);
		
		//colocamos la informacion recibida del aid recibido
		sb.append(aid.getRid()); //identificador de la franquisia
		sb.append(Integer.toString(aid.getAid().length())); //longitud en caracteres el aid
		sb.append(aid.getAid()); //aid a cargar
		sb.append(aid.getTipoSeleccion()); // tipo de seleccion
		sb.append(aid.getVersionApp()); //terminal app version number
		
		//procesamiento de la marca de la aplicacion
		String marca = BeanAidsInfo.aidsSchema.get(aid.getRid());
		sb.append((marca.length()<10)?"0"+Integer.toString(marca.length()):Integer.toString(marca.length()));
		sb.append(marca);
		
		//colocacion de lo tac requeridos en el orden indicado
		sb.append(aid.getTACDefault()).append(aid.getTACDenial()).append(aid.getTACOnline());
		
		Log.d(TAG, "Comando E07 generado: "+sb.toString());
		
		//retornamos el comando solicitado
		return construirComando(sb.toString(), STX, ETX);
		
	}
	
	//Z
	/**
	 * Funcion que le indica al pinpad que retorne a un estado idle
	 * @return (byte[]) comando solicitado 
	 */
	public static byte[] getComandoZ1(){return construirComando(COMANDO_Z1, STX, ETX);}
	
	/**
	 * funcion que genera el comando para colocar texto en la pantalla
	 * @param mensaje (String) Mensaje a colocar
	 * @return (byte[]) comando generado 
	 * @throws Exception - si el texto es superior a los 16 caracteres
	 */
	public static byte[] getComandoZ2(String mensaje) throws Exception{
		//iniciamos la construccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_Z2);
		
		//colocar el SUB para limpiar la pantalla
		sb.append(SUB);
		
		//colocamos el texto a mostrar
		if(mensaje.length()<=16){sb.append(mensaje);}
		else{throw new Exception("El texto supera los 16 caracteres permitidos");}
		
		//retornamos el comando
		return construirComando(sb.toString(), STX, ETX);
	}
	
	/**
	 * funcion que genera el comando para ajustar el texto a mostrar en estado IDLE del pinpad
	 * @param idle (String) mensaje a colocar no superior a 16 caracteres
	 * @return (byte[]) el comando a ejecutar
	 * @throws Exception - si la cadena es superior a 16 caracteres
	 */
	public static byte[] getComandoZ8(String idle) throws Exception{
		
		//iniciamos la construccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_Z8);
		
		//colocamos el texto a mostrar
		if(idle.length()<=16){sb.append(idle);}
		else{throw new Exception("El texto supera los 16 caracteres permitidos");}
		
		//retornamos el comando
		return construirComando(sb.toString(), STX, ETX);
	}
	
	/**
	 * funcion que genera el comando para la solicitud de pinblock
	 * @param workingKey
	 * @param numeroTarjeta
	 * @param montoTransaccion
	 * @return
	 */
	public static byte[] getComandoZ62(String workingKey, String numeroTarjeta, String montoTransaccion){
		
		//formateamos el monto a ser mostrado en pantalla
		new ConversorNumerico(",");
		String montoTxn= (montoTransaccion!=null)?ConversorNumerico.formatMontoString(montoTransaccion,true):" ";
		
		//iniciamos la construccion del comando
		StringBuffer sb = new StringBuffer(COMANDO_Z62).append(".");
		
		//colocamos el numero de tarjeta
		sb.append(numeroTarjeta).append((char) 28);
		
		//colocamos la working key
		sb.append(workingKey);
		
		//indicamos la longitud minima y maxima del pinblock
		sb.append(minPin).append(maxPin);
		
		//indicamos que no permitimo pin nulos
		sb.append("N");
		
		//Colocamos el mensaje a mostrar en la primera linea
		sb.append((mensaje!=null)?mensaje:mensajeMonto).append((char) 28);
		
		//Colocamos el monto de la transaccion en la segunda linea
		sb.append(montoTxn).append((char) 28); 
		
		//colocamos el mensaje de procesamiento de pinbloc
		sb.append(mensajeProc);
		
		//colocamos la longitud de la workingKey
		int wkLength = workingKey.length();
		sb.append((wkLength == 32)?((char) 28 + "1"):(wkLength==16)?((char)28+"0"):"0");
		
		//retornamos el comando solicitado
		return construirComando(sb.toString(), STX, ETX);
		
	}

	/**
	 * funcion que genera el comando requerido para la activacion de los lectores de banda magnetica y Chip (EMV)
	 * @return
	 */
	public static byte[] getComandoZ9033(){return construirComando(COMANDO_Z9033, STX, ETX);}
	
	/**
	 * funcion que genera el comando requerido para la activacion del lector de banda magnetica
	 * @return
	 */
	public static byte[] getComandoZ9030(){return construirComando(COMANDO_Z9030, STX, ETX);}

	/**
	 * funcion que genera el comando requerido para la activacion del lector de banda magnetica 
	 * entregando la informacion de la tarjeta de forma encriptada.
	 * @return
	 */
	public static byte[] getComandoZ9230(){return construirComando(COMANDO_Z9230, STX, ETX);}
	
	/**
	 * funcion que genera el comando requerido para la activacion del lector de banda magnetica
	 * entregando la informacion de la tarjeta de forma encriptada.
	 * @return
	 */
	public static byte[] getComandoZ9233(){return construirComando(COMANDO_Z9233, STX, ETX);}
}
