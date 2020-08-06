package ve.com.megasoft.pinpad.newland.thread;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.newland.me.DeviceManager;
import com.newland.mtype.DeviceRTException;
import com.newland.mtype.ModuleType;
import com.newland.mtype.common.ExCode;
import com.newland.mtype.event.AbstractProcessDeviceEvent;
import com.newland.mtype.event.DeviceEvent;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.CommonCardType;
import com.newland.mtype.module.common.cardreader.K21CardReader;
import com.newland.mtype.module.common.cardreader.K21CardReaderEvent;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.cardreader.OpenCardReaderResult;
import com.newland.mtype.module.common.cardreader.SearchCardRule;
import com.newland.mtype.module.common.pin.PinInputEvent;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwipResultType;
import com.newland.mtype.module.common.swiper.Swiper;
import com.newland.mtype.module.common.swiper.SwiperReadModel;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.emv.EmvCardInfo;
import ve.com.megasoft.pinpad.newland.data.NlUtils;
import ve.com.megasoft.pinpad.newland.data.SimpleTransferListener;
import com.newland.mtype.common.InnerProcessingCode;
import com.newland.mtype.common.ProcessingCode;

//No existe la clase.
//import ve.com.megasoft.pinpad.newland.activity.N900KeyBoardNumberActivity;

public class StartEmvThread extends BaseThread {

    //CONSTANTES
    private static final String TAG = StartEmvThread.class.getName();

    private EmvTransController controller;
    private SimpleTransferListener simpleTransferListener;
    private EmvModule emvModule;
    private Handler handlerSegCertEmv;
    private List sArgs;

    //Constructor
    public StartEmvThread(List sArgs, Handler handlerStartEmv, Handler handlerSegCertEmv, DeviceManager deviceManager) {
        super(handlerStartEmv, deviceManager);
        this.handlerSegCertEmv=handlerSegCertEmv;
        this.sArgs=sArgs;
    }

    @Override
    public void run() {
        super.run();

        try {
            //instanciamos listener
            K21CardReader cardReader = (K21CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
            EventHolder<K21CardReaderEvent> listener = new EventHolder<K21CardReaderEvent>();

            //abrimos lector de chip
            cardReader.openCardReader(new ModuleType[]{ModuleType.COMMON_ICCARDREADER}, true, 10, TimeUnit.SECONDS, listener, SearchCardRule.NORMAL);

            Log.i(TAG,"Iniciando listener");
            //iniciamos listener
            try {
                listener.startWait();
            } catch (InterruptedException e) {
                cardReader.cancelCardRead();
            }

            OpenCardReaderEvent event = listener.event;
            event = preEvent(event, 1003);
            if (event == null) {
                return;
            }

            //obtengo respuestas de lectura
            OpenCardReaderResult cardResult = event.getOpenCardReaderResult();
            CommonCardType[] openedModuleTypes = cardResult.getResponseCardTypes();

            //No se retorno ninguna accion de lectura de tarjeta
            if (cardResult == null || openedModuleTypes == null ||openedModuleTypes.length <= 0) {
                throw new Exception("No se retorno ninguna accion de lectura de tarjeta");
            }

            //Se retorno mas de 1 accion de lectura de tarjeta
            else if (openedModuleTypes.length > 1) {
                throw new DeviceRTException(1003, "Should return only one type of cardread action! But is " + openedModuleTypes.length);
            }

            switch (openedModuleTypes[0]) {
                case ICCARD:

                    //obtenemos instancia de EmvModule
                    emvModule=NlUtils.getEmvModule();

                    //obtenemos listener de transacción emv
                    simpleTransferListener = new SimpleTransferListener(NlUtils.getCordovaPlugin().cordova.getActivity(), emvModule, handler, handlerSegCertEmv);

                    //obtenemos controlador para transacción emv
                    Log.d(TAG,"Obteniendo controlador EMV");
                    controller = emvModule.getEmvTransController(simpleTransferListener);
                    if (controller != null) {
                        BigDecimal tradeAmount = null;
                        BigDecimal cashbackAmount = null;

                        //obtenemos monto y filtramos cualquier caracter no numérico
                        String sAmount=(String)sArgs.get(0);
                        if (!(sAmount.charAt(0)>='0' && sAmount.charAt(0)<='9')){
                            sAmount = sAmount.substring(1,sAmount.length());
                        }

                        //obtenemos monto de cashback y filtramos cualquier caracter no numérico
                        String sCashbackAmount=(String)sArgs.get(1);
                        if (!(sCashbackAmount.charAt(0)>='0' && sCashbackAmount.charAt(0)<='9')){
                            sCashbackAmount = sCashbackAmount.substring(1,sCashbackAmount.length());
                        }

                        Log.d(TAG,"sAmount:"+sAmount+".");
                        Log.d(TAG,"sCashbackAmount:"+sCashbackAmount+".");

                        tradeAmount = new BigDecimal(Long.valueOf(sAmount)).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
                        cashbackAmount = new BigDecimal(Long.valueOf(sCashbackAmount)).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);

                        //iniciamos transacción EMV
                        Log.d(TAG,"Iniciando transacción EMV");
                        controller.startEmv(
                                ProcessingCode.GOODS_AND_SERVICE,
                                InnerProcessingCode.USING_STANDARD_PROCESSINGCODE,
                                tradeAmount,
                                cashbackAmount,
                                true,
                                true);
                    }
                    break;

                default:
                    throw new DeviceRTException(1003, "Not support cardreader module:" + openedModuleTypes[0]);
            }
        }catch (Exception e){

            Message msg = new Message();
            msg.what = 1;
            msg.obj = e.getMessage();

            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);
        }
    }


    /**
     * EventListener
     * <p>
     *
     * @author lance
     *
     * @param <T>
     */
    private class EventHolder<T extends DeviceEvent> implements DeviceEventListener<T> {

        private T event;

        private final Object syncObj = new Object();

        private boolean isClosed = false;

        public void onEvent(T event, Handler handler) {
            this.event = event;
            if (event instanceof PinInputEvent && ((PinInputEvent) event).isProcessing()) {
                return;
            } //Thread waiting until the password input is completed,
            synchronized (syncObj) {
                isClosed = true;
                syncObj.notify();
            }
        }

        public Handler getUIHandler() {
            return null;
        }

        void startWait() throws InterruptedException {
            synchronized (syncObj) {
                if (!isClosed)
                    syncObj.wait();
            }
        }

    }

    private <T extends AbstractProcessDeviceEvent> T preEvent(T event, int defaultExCode) {
        if (!event.isSuccess()) {
            if (event.isUserCanceled()) {
                return null;
            }
            if (event.getException() != null) {
                if (event.getException() instanceof RuntimeException) {// Throw  the Runtime Exception
                    throw (RuntimeException) event.getException();
                }
                throw new DeviceRTException(1003, "Open card reader meet error!", event.getException());
            }
            throw new DeviceRTException(ExCode.UNKNOWN, "Unknown exception!defaultExCode:" + defaultExCode);
        }
        return event;
    }
}
