/**
 * Mega Soft Computacion C.A.
 * 
 * =============================================================================
 * Proyecto    : cordova megasoft pinpad
 * Programa    : plugin pinpad
 * Creado por  : Adrian Jesus Silva Simoes
 * Creado en   : 02/09/2016
 * Descripcion : parte javaScript para la comunicacion hacia los pinpad's
 * -----------------------------------------------------------------------------
 * 				Actualizaciones
 * -----------------------------------------------------------------------------
 * =============================================================================
 * Versión     : 1.0.0
 * Fecha       : 02/09/2016
 * Developer   : Adrian Jesus Silva Simoes
 * Descripción : Implementación inicial, basada en el plugin prototipo 
 * ve.com.megasoft.mobilepos.pinpaddriver
 * =============================================================================
 * 
 */

//requisitos del plugins
var exec = require('cordova/exec');
var cordova = require('cordova');

//bean's
var beanBase = {
	//Accion
	ACCIONECHO : "1",
	ACCIONOPENCOMCHANNEL : 3,
	ACCIONCLOSECOMCHANNEL : 4,
	ACCIONAIDS : "21",
	ACCIONLLAVESEMV : "22",
	//etiquetas
	TAGESTADO : "estatus",
	TAGMENSAJE : "mensaje",
};

var beanConfiguracion={
	//Accion
	ACCION:"2"
};


var beanFecha = {
	//Accion
	ACCION : "10",
	//etiquetas
	TAG : "fecha",
};

var beanBateria={
	//Accion
	ACCION : "11",
	//etiquetas
	TAG : "bateria",	
};

var beanImpresora={
	//Accion
	ACCION : "24",
	//etiquetas
	TAG : "impresora",	
};

var beanTamper={
	//Accion
	ACCION : "12",
	//etiquetas
	TAG : "tamper",
};

var beanSerial={
	//Accion
	ACCION : "13",
	//etiquetas
	TAGSERIAL:"serial",
	TAGOS:"os",
	TAGAPP:"app",
	TAGKERNEL:"kernel",
	TAGMARCA : "marca",
	TAGMODELO : "modelo",
	TAGISPRINTER : "impresora",
	TAGISICCARD : "iccard",
	TAGISMAGCARD : "magcard"
};

var beanTarjeta={
	//Accion
	ACCIONMSR : "14",
	ACCIONEMV : "16",
	ACCIONSEGCER : "17",
	ACCIONANULAR: "23",
	//etiquetas
	TAGOBFPAN:"obfuscated_pan",
	TAGCRDHLDNM:"cardholder_name",
	TAGENCTRCK : "track_2_data",
	TAGKSNTRCK : "track_2_ksn",
	TAGSERVICECODE : "service_code",
	TAGEXTRATIONMODE : "extration_mode",
	TAGTLV:"tlv",
};


var beanPinblock={
	//Accion
	ACCION : "15",
	//etiquetas
	TAGENCPINBLOCK : "pinblock_data",
	TAGKSNPINBLOCK : "pinblock_ksn",
};


var beanAbort={
	//Accion
	ACCIONABORT : "18",
	ACCIONABORTEMV : "19",
};


var beanCalibracion={
	ACCIONCALIBRACION:"20",
	
	TAGDEVICECLASS : "device_class",
	TAGDEVICENAME : "device_name",
	TAGDEVICEADDRESS : "device_address"
};


//Modelo Privados -- TODO colocar toda la logica de procesamiento de tarjeta aqui
var metodosPrivados = {
		
};

//Modelo
var pinpad = {
	//CONSTANTES
	
	//modos
	EXECMODE:"exec",
	GETMODE:"get",
		
	//estatus
	OK:"00",
	CALIBRACIONSTILLOK:"02",
	NOK:"99",
	TIMEOUT:"98",
	NOTFOUND:"404",
	
	//tipos de transaccion
	COMPRA: "00",
    CASHBACK: "01",
    COMPRACONCASHBACK: "09",
    
    //modo de lectura
    MSR:["B",0],
    EMV:["E",5],
    FALLBACK:["F",8],
		
	//Atributos
    //intervalos
    cronos:undefined,
    
    //Tipo de Tarjetas -- TODO
	
	//Bines de Tarjetas -- TODO
    
    //Dispositivos pinpad's disponibles
    devices:[
 	 	{name:"Verifone E265 - E355",clazz:"ve.com.megasoft.pinpad.verifone.modelo.ModeloEWirelessSerie",pinblock:"MS",track:"",initCero:"1"},
		{name:"New Post Technology N58", clazz:"ve.com.megasoft.pinpad.n58.modelo.ModeloN58",pinblock:"MS",track:"",initCero:"0"},
		{name:"Newland", clazz:"ve.com.megasoft.pinpad.newland.modelo.ModeloNewland",pinblock:"MS",track:"",initCero:"1"}
	 ],
    
    //configuracion del pinpad
	configuracion:{
		modelo_pinpad:"",
		pinpad_nombre:"",
		pinpad_direcc:"",
		pinpad_indice_wk:"",
		pinpad_working_key:"",
		pinpad_pinblock:"MS",
		pinpad_track:"",
		pinpad_init_cero:"0",
		pinpad_ver_aids:"19800101000000",
		pinpad_ind_capks:"1",
		pinpad_idle_text:"** Mobile POS **",
		pinpad_isPrinter:"false",
		pinpad_isICCard:"false",
		pinpad_isMagCard:"false"
	},

	/*C2P*/
/* 	bancos:[
		{codigo:"0102",  rif:"G-20009997-6", name:"Banco de Venezuela S.A.C.A. Banco Universal"},
		{codigo:"0104",  rif:"J-00002970-9", name:"Venezolano de Crédito, S.A. Banco Universal"},
		{codigo:"0105",  rif:"J-00002961-0", name:"Banco Mercantil, C.A S.A.C.A. Banco Universal"},
		{codigo:"0108",  rif:"J-00002967-9", name:"Banco Provincial, S.A. Banco Universal"},
		{codigo:"0114",  rif:"J-00002949-0", name:"Bancaribe C.A. Banco Universal"},
		{codigo:"0115",  rif:"J-00002950-4", name:"Banco Exterior C.A. Banco Universal"},
		{codigo:"0116",  rif:"J-30061946-0", name:"Banco Occidental de Descuento, Banco Universal C.A."},
		{codigo:"0128",  rif:"J-09504855-1", name:"Banco Caroní C.A. Banco Universal"},
		{codigo:"0134",  rif:"J-07013380-5", name:"Banesco Banco Universal S.A.C.A."},
		{codigo:"0137",  rif:"J-09028384-6", name:"Banco Sofitasa Banco Universal"},
		{codigo:"0138",  rif:"J-00297055-3", name:"Banco Plaza Banco Universal"},
		{codigo:"0151",  rif:"J-00072306-0", name:"BFC Banco Fondo Común C.A Banco Universal"},
		{codigo:"0156",  rif:"J-08500776-8", name:"100% Banco, Banco Universal C.A."},
		{codigo:"0157",  rif:"J-00079723-4", name:"DelSur Banco Universal, C.A."},
		{codigo:"0163",  rif:"G-20005187-6", name:"Banco del Tesoro, C.A. Banco Universal"},
		{codigo:"0166",  rif:"G-20005795-5", name:"Banco Agrícola de Venezuela, C.A. Banco Universal"},
		{codigo:"0168",  rif:"J-31637417-3", name:"Bancrecer, S.A. Banco Microfinanciero"},
		{codigo:"0169",  rif:"J-31594102-3", name:"Mi Banco Banco Microfinanciero C.A."},
		{codigo:"0171",  rif:"J-08006622-7", name:"Banco Activo, C.A. Banco Universal"},
		{codigo:"0172",  rif:"J-31628759-9", name:"Bancamiga Banco Microfinanciero C.A."},
		{codigo:"0174",  rif:"J-00042303-2", name:"Banplus Banco Universal, C.A."},
		{codigo:"0175",  rif:"G-20009148-7", name:"Banco Bicentenario Banco Universal C.A."},
		{codigo:"0177",  rif:"G-20010657-3", name:"Banco de la Fuerza Armada Nacional Bolivariana, B.U."},
		{codigo:"0191",  rif:"J-30984132-7", name:"Banco Nacional de Crédito, C.A. Banco Universal"},
	  ], */

	bancos:[
	{codigo:"0102",  rif:"G-20009997-6", name:"Banco de Venezuela S.A.C.A. Banco Universal", nameShort:"Venezuela"},
	{codigo:"0104",  rif:"J-00002970-9", name:"Venezolano de Crédito, S.A. Banco Universal", nameShort:"Venezolano de Crédito"},
	{codigo:"0105",  rif:"J-00002961-0", name:"Banco Mercantil, C.A S.A.C.A. Banco Universal", nameShort:"Mercantil"},
	{codigo:"0108",  rif:"J-00002967-9", name:"Banco Provincial, S.A. Banco Universal", nameShort:"Provincial"},
	{codigo:"0114",  rif:"J-00002949-0", name:"Bancaribe C.A. Banco Universal", nameShort:"Bancaribe"},
	{codigo:"0115",  rif:"J-00002950-4", name:"Banco Exterior C.A. Banco Universal", nameShort:"Exterior"},
	{codigo:"0116",  rif:"J-30061946-0", name:"Banco Occidental de Descuento, Banco Universal C.A.", nameShort:"BOD"},
	{codigo:"0128",  rif:"J-09504855-1", name:"Banco Caroní C.A. Banco Universal", nameShort:"Caroní"},
	{codigo:"0134",  rif:"J-07013380-5", name:"Banesco Banco Universal S.A.C.A.", nameShort:"Banesco"},
	{codigo:"0137",  rif:"J-09028384-6", name:"Banco Sofitasa Banco Universal", nameShort:"Sofitasa"},
	{codigo:"0138",  rif:"J-00297055-3", name:"Banco Plaza Banco Universal", nameShort:"Plaza"},
	{codigo:"0151",  rif:"J-00072306-0", name:"BFC Banco Fondo Común C.A Banco Universal", nameShort:"BFC"},
	{codigo:"0156",  rif:"J-08500776-8", name:"100% Banco, Banco Universal C.A.", nameShort:"100% Banco"},
	{codigo:"0157",  rif:"J-00079723-4", name:"DelSur Banco Universal, C.A.", nameShort:"DelSur"},
	{codigo:"0163",  rif:"G-20005187-6", name:"Banco del Tesoro, C.A. Banco Universal", nameShort:"Tesoro"},
	{codigo:"0166",  rif:"G-20005795-5", name:"Banco Agrícola de Venezuela, C.A. Banco Universal", nameShort:"Agrícola"},
	{codigo:"0168",  rif:"J-31637417-3", name:"Bancrecer, S.A. Banco Microfinanciero", nameShort:"Bancrecer"},
	{codigo:"0169",  rif:"J-31594102-3", name:"Mi Banco Banco Microfinanciero C.A.", nameShort:"Mi Banco"},
	{codigo:"0171",  rif:"J-08006622-7", name:"Banco Activo, C.A. Banco Universal", nameShort:"Banco Activo"},
	{codigo:"0172",  rif:"J-31628759-9", name:"Bancamiga Banco Microfinanciero C.A.", nameShort:"Bancamiga"},
	{codigo:"0174",  rif:"J-00042303-2", name:"Banplus Banco Universal, C.A.", nameShort:"Banplus"},
	{codigo:"0175",  rif:"G-20009148-7", name:"Banco Bicentenario Banco Universal C.A.", nameShort:"Bicentenario"},
	{codigo:"0177",  rif:"G-20010657-3", name:"Banco de la Fuerza Armada Nacional Bolivariana, B.U.", nameShort:"BANFANB"},
	{codigo:"0191",  rif:"J-30984132-7", name:"Banco Nacional de Crédito, C.A. Banco Universal", nameShort:"BNC"},
	],

	operadoras: [
	{name: "0412",dial_code: "0412",code: "Digitel"}, 
	{name: "0414",dial_code: "0414",code: "Movistar"},
	{name: "0424",dial_code: "0424",code: "Movistar"}, 
	{name: "0416",dial_code: "0416",code: "Movilnet"}, 
	{name: "0426",dial_code: "0426",code: "Movilnet"}
	],

	doc: [
	{type: "V"}, 
	{type: "E"},
	{type: "J"}, 
	{type: "G"}, 
	{type: "P"} 
	],
	/*C2P*/
    
    //configuracion del plugin
    configPlugin:{
    	//timeout plugin
		timeout_response:60,
		timeout_int_calibracion:180,
		
		//intervalo
		int_segundo:1000,
    },
	
	//ultimo estado/mensaje recibido
	estado:undefined,
	mensaje:undefined,
	
	//datos basicos de PINPAD*/
	comChanelOpen:false,
	fecha:undefined,
	bateria:undefined,
	impresora:{
		estatus:undefined,
		mensaje:undefined
	},
	tamper:undefined,
	serial:{
		number:undefined,
		os:undefined,
		app:undefined,
		kernel:undefined,
		marca:undefined,
		modelo:undefined,
		impresora: undefined,
		iccard: undefined,
		magcard: undefined
	},
	
	//tarjeta y pinblock
	tarjeta:{
		obfuscatedPan:undefined,
		cardholderName:undefined,
		track2Data:undefined,
		track2Ksn:undefined,
		serviceCode:undefined,
		extrationMode:undefined,
		tlv:undefined
	},
	pinblock:{
		pinblockData:undefined,
		pinblockKsn:undefined
	},
	
	//Metodos
	//Manipuladores de datos
	/**
	 * procedimiento que setea el estado y el mensaje recibido del ultimo comando ejecutado
	 * @param info - informacion recibida
	 */
	setBase:function(info){
		pinpad.estado = info[beanBase.TAGESTADO];
		pinpad.mensaje = info[beanBase.TAGMENSAJE];
	},
	
	/**
	 * funcion encargada de recibir todos los errores que se pueden producir
	 * en la ejecucion de comandos en los pinpads
	 * @param error (String,Object) el error producido 
	 * @param fail (function) el callback de fracaso recibido
	 */
	callbackError:function(error, fail){
		//no se pudo realizar la ejecucion del comando
		console.log("error del pinpad, data: "+JSON.stringify(error));
		
		//analizamos el error
		//Timeout del comando
		if(typeof error !== 'object'){
			var timeout = pinpad.TIMEOUT.localeCompare(error);
			if(timeout === 0){
				console.log("Timeout de Espera alcansado, entregando respuesta acorde");
				error = new Object();
				error[beanBase.TAGESTADO] = pinpad.TIMEOUT;
				error[beanBase.TAGMENSAJE] = "timeout espera respuesta de pinpad";
			}
			//Fatal Error no hay JSON
			var fatal = pinpad.NOK.localeCompare(error);
			if(fatal === 0){
				console.log("error interno del plugin, NOK");
				error = new Object();
				error[beanBase.TAGESTADO] = pinpad.NOK;
				error[beanBase.TAGMENSAJE] = "error en el plugin, solicitud no procesada";
			}
		}
		
		//colocamos el mensaje de error
		pinpad.setBase(error);
		
		//indicamos devolvemos la llamada de error 
		fail(error);
		
		//culminamos el intervalo debido al error
		clearInterval(pinpad.cronos);
	},
	
	/**
	 * procedimiento que limpia los datos usados constantemente
	 */
	clearInfo:function(){
		pinpad.estado = undefined;
		pinpad.mensaje = undefined;
	},
	
	//funciones base
	/**
	 * procedimiento que manda un echo y retorna una promesa de ese echo
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	echo:function(message, success, fail){
		exec(
			function(info){
				//seteamos la respuesta del echo
				pinpad.setBase(info);
				
				//retornamos la respuesta
				success(info);
			},
			function(error){pinpad.callbackError(error, fail);},
			"cordova-megasoft-pinpad",beanBase.ACCIONECHO,[message]
		);	
	},
	
	/**
	 * funcion que recupera la configuracion por defecto de los pinpads
	 * @returns (Promise) la promesa para procesar el resultado
	 * TODO - Eliminar esta funcion ya no existe configuracion default
	 */
	getConfiguracionDefault:function(success, fail){
		exec(
			function(configuracion){
				//seteamos la respuesta
				pinpad.configuracion = configuracion;
				
				//retornamos la respuesta
				success(configuracion);
			},
			function(error){pinpad.callbackError(error, fail);},
			"cordova-megasoft-pinpad",beanConfiguracion.ACCION,[]	
		);
	},
	
	//Apertura y Cierra de canales de comunicación
	/**
	 * funcion que abre el canal de comunicacion con el pinpad 
	 * @param success (function) - que hacer despues de recibir la respuesta
	 * @param fail (function) - que hacer despues de recibir un error
	 */
	openComChannel:function(success,fail){
		exec(
			function(info){
				pinpad.comChanelOpen = true;
				success(info);
			},
			function(error){pinpad.callbackError(error, fail);},
			"cordova-megasoft-pinpad",beanBase.ACCIONOPENCOMCHANNEL,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	/**
	 * funcion que cierra el canal de comunicacion con el pinpad 
	 * @param success (function) - que hacer despues de recibir la respuesta
	 * @param fail (function) - que hacer despues de recibir un error
	 */
	closeComChannel:function(success,fail){
		exec(
			function(info){
				pinpad.comChanelOpen = false;
				success(info);
			},
			function(error){pinpad.callbackError(error, fail);},
			"cordova-megasoft-pinpad",beanBase.ACCIONCLOSECOMCHANNEL,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	//funciones basicas de PINPAD
	/**
	 * funcion que ejecuta el comando para solicitar la fecha al pinpad
	 * 
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	getFecha:function(success, fail){
		//limpiamos los campos
		console.log("limpiando campo de fecha");
		pinpad.clearInfo();
		pinpad.fecha = undefined;
		
		//ejecutamos el comando
		console.log("ejecutando comando de fecha");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando fecha procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//procesamos la respuesta
									pinpad.fecha = response[beanFecha.TAG];
									
									//entregamos la informacion
									success(pinpad.fecha);
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanFecha.ACCION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanFecha.ACCION,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	/**
	 * funcion que ejecuta el comando para solicitar el estado de la bateria del pinpad
	 * 
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	getBateria:function(success,fail){
		//limpiamos los campos de respuesta
		pinpad.bateria = undefined;
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando de la bateria");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando bateria procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//procesamos la respuesta
									pinpad.bateria = response[beanBateria.TAG];
									
									//entregamos la informacion
									success(pinpad.bateria);
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanBateria.ACCION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanBateria.ACCION,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},

	/**
	 * funcion que ejecuta el comando para solicitar el estado de la impresora del pinpad
	 * 
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	printer:function(voucher, tiempo, success, fail){
		//limpiamos los campos de respuesta
		pinpad.impresora = undefined;
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log(".JS -> ejecutando comando de la impresora");
		console.log("voucher: "+ voucher + ", tiempo: " + tiempo);
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando impresora procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//procesamos la respuesta
									console.log("Entro a pinpad.OK - response beanImpresora.Tag: " + JSON.stringify(response[beanImpresora.TAG]));
									pinpad.impresora = response[beanImpresora.TAG];

									//entregamos la informacion
									success(pinpad.estado );
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanImpresora.ACCION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanImpresora.ACCION,[([voucher,tiempo]),pinpad.EXECMODE,pinpad.configuracion]
		);
	},

	/**
	 * funcion que ejecuta el comando para imprimir en el pinpad
	 * 
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	/*printer:function(success,fail){
		//limpiamos los campos de respuesta
		pinpad.impresora = undefined;
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando de la impresora");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando impresora procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail)}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//procesamos la respuesta
									pinpad.impresora = response[beanImpresora.TAG];
									
									//entregamos la informacion
									success(pinpad.impresora);
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanImpresora.ACCION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanBateria.ACCION,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},*/
	
	/**
	 * funcion que ejecutar el comando para solicitar la informacion con respecto a la alteracion del equipo
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	getTamper:function(success,fail){
		//limpiamos las respuestas
		pinpad.tamper = undefined;
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando tamper");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando tamper procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//procesamos la respuesta
									pinpad.tamper = response[beanTamper.TAG];
									
									//entregamos la informacion
									success(pinpad.tamper);
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanTamper.ACCION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanTamper.ACCION,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	/**
	 * funcion que ejecutar el comando para solicitar la informacion con respecto al dispositivo
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	getSerial:function(success,fail){
		//limpiamos las respuesta
		pinpad.clearInfo();
		pinpad.serial.number=undefined;
		pinpad.serial.os=undefined;
		pinpad.serial.app=undefined;
		pinpad.serial.kernel=undefined;
		pinpad.serial.marca=undefined;
		pinpad.serial.modelo=undefined;
		pinpad.serial.impresora=undefined;
		pinpad.serial.iccard=undefined;
		pinpad.serial.magcard=undefined;
		
		//ejecutamos el comando
		console.log("ejecutando comando serial del dispositivo");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando serial del dispositivo procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada .js: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
								
									//procesamos la respuesta
									pinpad.serial.number = response[beanSerial.TAGSERIAL];  
									pinpad.serial.os = response[beanSerial.TAGOS];
									pinpad.serial.app = response[beanSerial.TAGAPP];
									pinpad.serial.kernel = response[beanSerial.TAGKERNEL];
									pinpad.serial.marca = response[beanSerial.TAGMARCA];
									pinpad.serial.modelo = response[beanSerial.TAGMODELO];
									pinpad.configuracion.pinpad_isPrinter = response[beanSerial.TAGISPRINTER];
									pinpad.configuracion.pinpad_isICCard = response[beanSerial.TAGISICCARD];
									pinpad.configuracion.pinpad_isMagCard = response[beanSerial.TAGISMAGCARD];
									pinpad.serial.impresora = response[beanSerial.TAGISPRINTER];
									pinpad.serial.iccard = response[beanSerial.TAGISICCARD];
									pinpad.serial.magcard = response[beanSerial.TAGISMAGCARD];
								
									//entregamos la informacion
									success(pinpad.serial);
									
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanSerial.ACCION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanSerial.ACCION,[pinpad.EXECMODE,pinpad.configuracion]
		);
		
	},
	
	//banda magentica y pinblock
	/**
	 * funcion que ejecutar el comando para activar el lector de banda magnetica y obtener los datos de una tarjeta
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	getBandaMagnetica:function(success,fail){
		//limpiamos los campos de respuesta
		pinpad.clearInfo();
		pinpad.tarjeta.obfuscatedPan=undefined;
		pinpad.tarjeta.cardholderName=undefined;
		pinpad.tarjeta.track2Data=undefined;
		pinpad.tarjeta.track2Ksn=undefined;
		pinpad.tarjeta.serviceCode=undefined;
		pinpad.tarjeta.tlv=undefined;
		pinpad.tarjeta.extrationMode=undefined;
		
		//ejecutamos el comando
		console.log("ejecutando comando de lector banda magnetica");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando de lector banda magnetica procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				clearInterval(pinpad.cronos);
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//procesamos la respuesta
									pinpad.tarjeta.obfuscatedPan = response[beanTarjeta.TAGOBFPAN];
									pinpad.tarjeta.cardholderName = response[beanTarjeta.TAGCRDHLDNM];
									pinpad.tarjeta.track2Data = response[beanTarjeta.TAGENCTRCK];
									pinpad.tarjeta.track2Ksn = response[beanTarjeta.TAGKSNTRCK];
									pinpad.tarjeta.serviceCode = response[beanTarjeta.TAGSERVICECODE];
									pinpad.tarjeta.extrationMode = response[beanTarjeta.TAGEXTRATIONMODE];
									
									//entregamos la informacion
									success(pinpad.tarjeta);
								}
								else{pinpad.callbackError(response, fail);}	
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanTarjeta.ACCIONMSR,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanTarjeta.ACCIONMSR,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	/**
	 * funcion que ejecuta el comando para activar el teclado para la captura de pinblock
	 *
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	getPinblock:function(numeroaTarjeta, montoTransaccion, mensaje, success, fail, update){
		//limpiamos los parametros de respuesta
		pinpad.clearInfo();
		pinpad.pinblock.pinblockData=undefined;
		pinpad.pinblock.pinblockKsn=undefined;
		
		//ejecutamos el comando
		console.log("ejecutando comando de pinblock");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando de pinblock procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//procesamos la respuesta
									pinpad.pinblock.pinblockData = response[beanPinblock.TAGENCPINBLOCK];
									pinpad.pinblock.pinblockKsn = response[beanPinblock.TAGKSNPINBLOCK];
									
									//entregamos la informacion
									success(pinpad.pinblock);
								}
								else{pinpad.callbackError(response, fail);}	
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
									
									//actualizamos el estatus en la aplicacion si aplica
									if(update !== undefined && typeof update === 'function'){
										update(errorResponse[beanBase.TAGMENSAJE]);
									}
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanPinblock.ACCION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanPinblock.ACCION,[((mensaje!=null)?[numeroaTarjeta,montoTransaccion,mensaje]:[numeroaTarjeta,montoTransaccion]),pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	//EMV
	/**
	 * funcion que inicia el proceso EMV en el dispositivo 
	 * @param monto - monto de la transaccion
	 * @param cashback - monto de cashback
	 * @param tipoTrans - tipo de transaccion
	 * @returns (Promise) la promesa para procesar el resultado [tarjeta,pinbloc]
	 */
	getStartEmv:function(monto, cashback, tipoTrans, mensaje, success, fail, update){
		//limpiamos los campos
		console.log("limpiando objeto tarjeta y pinblock");
		pinpad.clearInfo();
		pinpad.tarjeta.obfuscatedPan=undefined;
		pinpad.tarjeta.cardholderName=undefined;
		pinpad.tarjeta.track2Data=undefined;
		pinpad.tarjeta.track2Ksn=undefined;
		pinpad.tarjeta.serviceCode=undefined;
		pinpad.tarjeta.tlv=undefined;
		pinpad.tarjeta.extrationMode=undefined;
		pinpad.pinblock.pinblockData=undefined;
		pinpad.pinblock.pinblockKsn = undefined;
		
		//ejecutamos el comando
		console.log("ejecutando comando de transaccion emv");
		exec(
			function(exito){
				//el comando se inicio, preguntar por su estado
				console.log("Comando inicio EMV procesandose");
				console.log("Salida Exito: "+JSON.stringify(exito));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido
					if(count > pinpad.configPlugin.timeout_response){
						console.log("Timeout de Espera alcansado, entregando respuesta acorde");
						var data = new Object();
						data[beanBase.TAGESTADO] = pinpad.TIMEOUT;
						data[beanBase.TAGMENSAJE] = "timeout espera respuesta de pinpad";
						pinpad.setBase(data);
						fail(data);
						clearInterval(pinpad.cronos);
					}
					else{
						//ejecutamos el comando de consulta
						console.log("Ejecutamos comando de consulta");
						exec(
							function(response){
								console.log("respuesta obtenida: "+JSON.stringify(response));
								
								//entregamos la respuesta
								console.log("datos recuperado, EMV");
								pinpad.setBase(response);
								
								//analizamos el estado recuperado
								if(pinpad.estado === pinpad.OK){
								
									//seteamos los datos de la tarjeta
									pinpad.tarjeta.obfuscatedPan = response[beanTarjeta.TAGOBFPAN];
									pinpad.tarjeta.cardholderName = response[beanTarjeta.TAGCRDHLDNM];
									pinpad.tarjeta.track2Data = response[beanTarjeta.TAGENCTRCK];
									pinpad.tarjeta.track2Ksn = response[beanTarjeta.TAGKSNTRCK];
									pinpad.tarjeta.serviceCode = response[beanTarjeta.TAGSERVICECODE];
									pinpad.tarjeta.extrationMode = response[beanTarjeta.TAGEXTRATIONMODE];
									pinpad.tarjeta.tlv = response[beanTarjeta.TAGTLV];
									pinpad.pinblock.pinblockData = response[beanPinblock.TAGENCPINBLOCK];
									pinpad.pinblock.pinblockKsn = response[beanPinblock.TAGKSNPINBLOCK];
	
									//entregamos la informacion
									success([pinpad.tarjeta,pinpad.pinblock]);
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
									
									//actualizamos el estatus en la aplicacion si aplica
									if(update !== undefined && typeof update === 'function'){
										update(errorResponse[beanBase.TAGMENSAJE]);
									}
									
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanTarjeta.ACCIONEMV,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
				
			},
			function(error){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanTarjeta.ACCIONEMV,[((mensaje!=null)?[monto,cashback,tipoTrans,mensaje]:[monto,cashback,tipoTrans]),pinpad.EXECMODE,pinpad.configuracion]
		);
		
	},

	/**
	 * funcion que ejecuta el comando de segundo certificado en la tarjeta procesada
	 * @param estatus - estatus de la transaccion
	 * @param tag71 
	 * @param tag72
	 * @param tag91
	 * @returns (Promise) la promesa para procesar el resultado (tlv)
	 */
	getSegCertificado: function(tag39,estatus,tag71,tag72,tag91, success, fail){
		//limpiamos los parametros
		pinpad.clearInfo();
		pinpad.tarjeta.obfuscatedPan=undefined;
		pinpad.tarjeta.cardholderName=undefined;
		pinpad.tarjeta.track2Data=undefined;
		pinpad.tarjeta.track2Ksn=undefined;
		pinpad.tarjeta.serviceCode=undefined;
		pinpad.tarjeta.tlv=undefined;
		pinpad.tarjeta.extrationMode=undefined;
		
		//ejecutamos el comando
		console.log("ejecutando comando de transaccion emv");
		exec(
			function(exito){
				//el comando se inicio, preguntar por su estado
				console.log("Comando segundo certificado procesandose");
				console.log("Salida Exito: "+JSON.stringify(exito));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido
					if(count > pinpad.configPlugin.timeout_response){
						console.log("Timeout de Espera alcansado, entregando respuesta acorde");
						var data = new Object();
						data[beanBase.TAGESTADO] = pinpad.TIMEOUT;
						data[beanBase.TAGMENSAJE] = "timeout espera respuesta de pinpad";
						pinpad.setBase(data);
						fail(data);
						clearInterval(pinpad.cronos);
					}
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada 2doCert: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									
									//procesamos la respuesta
									pinpad.tarjeta.tlv = response[beanTarjeta.TAGTLV];
									
									//entregamos la informacion
									success(pinpad.tarjeta.tlv);
								}
								else{pinpad.callbackError(response, fail);}	
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error 2doCert obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado 2doCert, volviendo a preguntar");
								}
								else{
									console.log("error interno 2doCert, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanTarjeta.ACCIONSEGCER,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(error){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanTarjeta.ACCIONSEGCER,[[tag39,estatus,tag71,tag72,tag91],pinpad.EXECMODE,pinpad.configuracion]);
	},
	
	//Anulacion
	/**
	 * funcion que captura los datos requeridos de la tarjeta para la ejecucion de una transaccion de anulacion 
	 */
	getAnulacion: function(seqnum, success, fail){
		
		//limpiamos los campos
		console.log("limpiando objeto tarjeta y pinblock");
		pinpad.clearInfo();
		pinpad.tarjeta.obfuscatedPan=undefined;
		pinpad.tarjeta.cardholderName=undefined;
		pinpad.tarjeta.track2Data=undefined;
		pinpad.tarjeta.track2Ksn=undefined;
		pinpad.tarjeta.serviceCode=undefined;
		pinpad.tarjeta.tlv=undefined;
		pinpad.tarjeta.extrationMode=undefined;
		pinpad.pinblock.pinblockData=undefined;
		pinpad.pinblock.pinblockKsn = undefined;
		
		//ejecutamos el comando
		console.log("ejecutando comando de transaccion emv");
		exec(
			function(exito){
				//el comando se inicio, preguntar por su estado
				console.log("Comando inicio EMV procesandose");
				console.log("Salida Exito: "+JSON.stringify(exito));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido
					if(count > pinpad.configPlugin.timeout_response){
						console.log("Timeout de Espera alcansado, entregando respuesta acorde");
						var data = new Object();
						data[beanBase.TAGESTADO] = pinpad.TIMEOUT;
						data[beanBase.TAGMENSAJE] = "timeout espera respuesta de pinpad";
						pinpad.setBase(data);
						fail(data);
						clearInterval(pinpad.cronos);
					}
					else{
						//ejecutamos el comando de consulta
						console.log("Ejecutamos comando de consulta");
						exec(
							function(response){
								console.log("respuesta obtenida: "+JSON.stringify(response));
								
								//entregamos la respuesta
								console.log("datos recuperado, EMV");
								pinpad.setBase(response);
								
								//analizamos el estado recuperado
								if(pinpad.estado === pinpad.OK){
								
									//seteamos los datos de la tarjeta
									pinpad.tarjeta.obfuscatedPan = response[beanTarjeta.TAGOBFPAN];
									pinpad.tarjeta.cardholderName = response[beanTarjeta.TAGCRDHLDNM];
									pinpad.tarjeta.track2Data = response[beanTarjeta.TAGENCTRCK];
									pinpad.tarjeta.track2Ksn = response[beanTarjeta.TAGKSNTRCK];
									pinpad.tarjeta.serviceCode = response[beanTarjeta.TAGSERVICECODE];
									pinpad.tarjeta.extrationMode = response[beanTarjeta.TAGEXTRATIONMODE];
									pinpad.tarjeta.tlv = response[beanTarjeta.TAGTLV];
									pinpad.pinblock.pinblockData = response[beanPinblock.TAGENCPINBLOCK];
									pinpad.pinblock.pinblockKsn = response[beanPinblock.TAGKSNPINBLOCK];
	
									//entregamos la informacion
									success([pinpad.tarjeta,pinpad.pinblock]);
								}
								else{pinpad.callbackError(response, fail);}
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
									
									//actualizamos el estatus en la aplicacion si aplica
									if(update !== undefined && typeof update === 'function'){
										update(errorResponse[beanBase.TAGMENSAJE]);
									}
									
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanTarjeta.ACCIONEMV,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
				
			},
			function(error){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanTarjeta.ACCIONEMV,[seqnum,pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	//Cancelaciones
	/**
	 * funcion que ejecuta el comando para cancelar (abortar) la ultima operacion solicictada
	 * @returns (Promise) la promesa para procesar el resultado (abort)
	 */
	getAbortOperation:function(success, fail){
		//limpiamos los parametros de respuestas
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando de cancelacion");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando de cancelacion de operacion procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								//entregamos la informacion
								if(pinpad.estado === pinpad.OK){success(pinpad.estado + " - "+pinpad.mensaje);}
								else{pinpad.callbackError(response, fail);}	
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanAbort.ACCIONABORT,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanAbort.ACCIONABORT,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	/**
	 * funcion que ejecuta el comando para cancelar (abortar) la ultima operacion emv solicictada
	 * @returns (Promise) la promesa para procesar el resultado (abort)
	 */
	getAbortEmvOperation:function(success, fail){
		//limpiamos los parametros de respuestas
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando de cancelacion");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando de cancelacion de operacion procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								//entregamos la informacion
								if(pinpad.estado === pinpad.OK){success(pinpad.estado + " - "+pinpad.mensaje);}
								else{pinpad.callbackError(response, fail);}		
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanAbort.ACCIONABORTEMV,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanAbort.ACCIONABORTEMV,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	//calibracions
	/**
	 * funcion que solicita la calibracion de los dispositivos con el pinpad
	 * @returns (Promise) la promesa para procesar el resultado
	 */
	getCalibracionDispositivo:function(success, fail){
		//limpiamos los parametros de respuesta
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando de calibración");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando de calibracion de operacion procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));

				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT //REVISAR
					if(count >= pinpad.configPlugin.timeout_int_calibracion){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//almacenamos los datos del pinpad
									pinpad.configuracion.modelo_pinpad = response[beanCalibracion.TAGDEVICECLASS];
									pinpad.configuracion.pinpad_nombre = response[beanCalibracion.TAGDEVICENAME];
									pinpad.configuracion.pinpad_direcc = response[beanCalibracion.TAGDEVICEADDRESS];
									
									//entregamos la informacion
									success(pinpad.estado + " - "+pinpad.mensaje);
								}
								else{pinpad.callbackError(response, fail);}	
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanCalibracion.ACCIONCALIBRACION,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanCalibracion.ACCIONCALIBRACION,[pinpad.EXECMODE,pinpad.configuracion]
		);
	},

	/*Carga de AID y CAPK*/
	cargarAids:function(date, aids, success, fail, update){
		//limpiamos los parametros de respuesta
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando de carga de AIDS");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando de carga de llaves procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT //REVISAR
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//actualizamos la fecha de la carga de aids
									pinpad.configuracion.pinpad_ver_aids = date;
									
									//entregamos la informacion de resultado de la ejecucion del comando
									success(pinpad.estado + " - "+pinpad.mensaje + " - "+ pinpad.configuracion.pinpad_ver_aids);
								}
								else{pinpad.callbackError(response, fail);}	
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
									
									//actualizamos el estatus en la aplicacion si aplica
									if(update !== undefined && typeof update === 'function'){
										update(errorResponse[beanBase.TAGMENSAJE]);
									}
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanBase.ACCIONAIDS,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanBase.ACCIONAIDS,[aids,pinpad.EXECMODE,pinpad.configuracion]
		);
	},
	
	cargarCapks:function(emvkeys, success, fail, update){
		
		//limpiamos los parametros de respuesta
		pinpad.clearInfo();
		
		//ejecutamos el comando
		console.log("ejecutando comando de carga de Llaves EMV");
		exec(
			function(data){
				//el comando se inicio, preguntar por su estado
				console.log("Comando de carga de llaves procesandose");
				console.log("Salida Exito: "+JSON.stringify(data));
				
				//iniciamos el intervalo de consulta
				var count = 0;
				pinpad.cronos = setInterval(function(){
					console.log("Ejecutando Intervalo Numero: "+count);
					
					//verificamos si el intervalo alcanzo el maximo permitido - TIMEOUT //REVISAR
					if(count >= pinpad.configPlugin.timeout_response){pinpad.callbackError(pinpad.TIMEOUT, fail);}
					
					//Ejecucion de comando de consulta
					else{
						//ejecutamos el comando de consulta
						exec(
							function(response){
								console.log("Respuesta recuperada: "+JSON.stringify(response));
								
								//seteamos el estado y el mensaje
								pinpad.setBase(response);
								
								if(pinpad.estado === pinpad.OK){
									//entregamos la informacion de resultado de la ejecucion del comando
									success(pinpad.estado + " - "+pinpad.mensaje);
								}
								else{pinpad.callbackError(response, fail);}	
								
								//detenemos el intervalo
								clearInterval(pinpad.cronos);
								
							},
							function(errorResponse){
								console.log("respuesta de error obtenida: "+JSON.stringify(errorResponse));
								
								//Not Found Yet, respuesta no encontrada
								if(errorResponse[beanBase.TAGESTADO]==pinpad.NOTFOUND){
									console.log("dato no recuperado, volviendo a preguntar");
									
									//actualizamos el estatus en la aplicacion si aplica
									if(update !== undefined && typeof update === 'function'){
										update(errorResponse[beanBase.TAGMENSAJE]);
									}
								}
								else{
									console.log("error interno, retornando error");
									pinpad.callbackError(errorResponse, fail);
								}
							},
							"cordova-megasoft-pinpad",beanBase.ACCIONLLAVESEMV,[pinpad.GETMODE,pinpad.configuracion]);
					}
					
					//incrementamos el contador
					count++;
					
				},pinpad.configPlugin.int_segundo);
			},
			function(errorData){pinpad.callbackError(errorData, fail);},
			"cordova-megasoft-pinpad",beanBase.ACCIONLLAVESEMV,[emvkeys,pinpad.EXECMODE,pinpad.configuracion]
		);
		
	}
	
};

//Cordova Module Export - publica la funcionalidad al aplicativo 
module.exports = pinpad;