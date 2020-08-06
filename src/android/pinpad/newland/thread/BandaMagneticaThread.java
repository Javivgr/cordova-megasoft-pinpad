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
import java.util.concurrent.TimeUnit;

//No existe la clase.
//import ve.com.megasoft.pinpad.newland.activity.N900KeyBoardNumberActivity;

public class BandaMagneticaThread extends BaseThread {

    //CONSTANTES
    private static final String TAG = BandaMagneticaThread.class.getName();

    //Constructor
    public BandaMagneticaThread(Handler handler, DeviceManager deviceManager) {
        super(handler, deviceManager);
    }

    @Override
    public void run() {
        super.run();

        try {

            Log.i(TAG,"Iniciando thread");
            //mensaje que se envia al handler
            Message msg = new Message();
            //lista para almacenar datos de respuesta
            List<Object> list = new ArrayList<Object>();

            //instanciamos listener
            K21CardReader cardReader = (K21CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
            EventHolder<K21CardReaderEvent> listener = new EventHolder<K21CardReaderEvent>();

            //abrimos lector de banda magnética
            cardReader.openCardReader(new ModuleType[]{ModuleType.COMMON_SWIPER}, true, 10, TimeUnit.SECONDS, listener, SearchCardRule.NORMAL);

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
                case MSCARD:

                    list.add("MSCARD");

                    boolean isCorrent = cardResult.isMSDDataCorrectly();
                    if (!isCorrent) {
                        throw new DeviceRTException(1003, "Swip failed!");
                    }

                    //obtenemos modulo para la interfaz de lectura de banda
                    Swiper swiper = (Swiper) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SWIPER);

                    //obtenemos data de lectura de tarjeta
                    SwipResult swipResult = swiper
                            .readPlainResult(new SwiperReadModel[] {
                                    SwiperReadModel.READ_FIRST_TRACK,
                                    SwiperReadModel.READ_SECOND_TRACK,
                                    SwiperReadModel.READ_THIRD_TRACK });

                    if (null != swipResult && swipResult.getRsltType() == SwipResultType.SUCCESS) {

                        //leemos track 1 y track 2
                        byte[] firstTrack = swipResult.getFirstTrackData();
                        byte[] secondTrack = swipResult.getSecondTrackData();
                        //byte[] thirdTrack = swipResult.getThirdTrackData();

                        if (firstTrack == null) {
                            list.add(new String(""));
                        }
                        else{
                            list.add(new String(firstTrack));
                        }

                        if (secondTrack == null) {
                            throw new Exception("No se pudo recuperar informacion del Track 2");
                        }

                        list.add(new String(secondTrack));
                        msg.obj = list;

                    } else { throw new Exception("Lectura incorrecta de tarjeta");}

                    break;

                default:
                    throw new DeviceRTException(1003, "Not support cardreader module:" + openedModuleTypes[0]);
            }

            msg.what = 0;
            //entregamos el mensaje al handler
            this.handler.sendMessage(msg);

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
