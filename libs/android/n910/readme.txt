1、copy  the meSdk-3.6.1-SNAPSHOT.jar to libs file directory

2、copy  libemvjni.so  and  libintelligentLib.so and  libndkapi.so  and libndkapism.so to libs/armeabi  file directory

3、copy aid_capk.app to assets file directory


4、change the connection parameters to new NS3ConnParams() when connect the device
   for example: 
   deviceManager = ConnUtils.getDeviceManager();
   deviceManager.init(baseActivity, K21_DRIVER_NAME, new NS3ConnParams(), new DeviceEventListener<ConnectionCloseEvent>() {
				@Override
				public void onEvent(ConnectionCloseEvent event, Handler handler) {
					if (event.isSuccess()) {
						baseActivity.showMessage("Device is disconnected by customers!", MessageTag.NORMAL);
					}
					if (event.isFailed()) {
						baseActivity.showMessage("Device is disconnected abnormally！", MessageTag.ERROR);
					}
				}

				@Override
				public Handler getUIHandler() {
					return null;
				}
			});
			baseActivity.showMessage("N900 device controller is initialized!", MessageTag.TIP);
			deviceManager.connect();
			deviceManager.getDevice().setBundle(new NS3ConnParams());
			baseActivity.showMessage("Device is connected successfully!", MessageTag.TIP);

5. Add permission：
<uses-permission android:name="android.permission.MANAGE_NEWLAND"/>  

