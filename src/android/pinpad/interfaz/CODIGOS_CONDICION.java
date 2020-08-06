package ve.com.megasoft.pinpad.interfaz;

public interface CODIGOS_CONDICION {
	
	/** PROCESO EJECUTADO EXITOSAMENTE */
	public static final String PROCESO_APROBADO = "000";
	
	/** TRANSACCION RECHAZADA. BIN O TARJETA NO REGISTRADO*/
	public static final String TRX_BIN_NOREGISTRADO  = "JA";
	
	/** FALLA EN DICCIONARIO - CAMPO VACIO */
	public static final String CAMPO_VACIO = "ACVACIO";
	
	/** FALLA EN DICCIONARIO - CAMPO NO CUMPLE FORMATO */
	public static final String CAMPO_FORMATO_INVALIDO = "ACFI";
	
	/** TRANSACCION CANCELADA. CAMPOS DEL REQUEST INCORRECTOS */
	public static final String TRX_REQUEST_INCORRECTO  = "DRI";	

}
