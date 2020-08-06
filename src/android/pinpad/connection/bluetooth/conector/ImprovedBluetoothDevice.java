/**
 * 
 */
package ve.com.megasoft.pinpad.connection.bluetooth.conector;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.ParcelUuid;

/**
 * @author adasilva
 *
 */
public class ImprovedBluetoothDevice {
	public final BluetoothDevice mDevice;
    private static final Method _createRfcommSocket;
    private static final Method _createInsecureRfcommSocket;
    private static final Method _setPin;
    private static final Method _setPasskey;
    private static final Constructor<?> _socketConstructor;

    static {
        _createRfcommSocket = getMethod(BluetoothDevice.class, "createRfcommSocket", new Class[]{Integer.TYPE});
        _createInsecureRfcommSocket = getMethod(BluetoothDevice.class, "createInsecureRfcommSocket", new Class[]{Integer.TYPE});
        _setPin = getMethod(BluetoothDevice.class, "setPin", new Class[]{byte[].class});
        _setPasskey = getMethod(BluetoothDevice.class, "setPasskey", new Class[]{Integer.TYPE});
        _socketConstructor = getConstructor(BluetoothSocket.class, new Class[]{Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE, BluetoothDevice.class, Integer.TYPE, ParcelUuid.class});
    }

    private static Method getMethod(Class<?> cls, String name, Class<?>[] args) {
        try {
            return cls.getMethod(name, args);
        } catch (Exception var4) {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
	private static Constructor<?> getConstructor(Class<?> cls, Class<?>[] args) {
        try {
            Constructor ex = cls.getDeclaredConstructor(args);
            if(!ex.isAccessible()) {
                ex.setAccessible(true);
            }

            return ex;
        } catch (Exception var3) {
            return null;
        }
    }

    public ImprovedBluetoothDevice(BluetoothDevice base) {
        if(base == null) {
            throw new NullPointerException();
        } else {
            this.mDevice = base;
        }
    }

    protected BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        return this.mDevice.createRfcommSocketToServiceRecord(uuid);
    }

    public int describeContents() {
        return this.mDevice.describeContents();
    }

    public String getAddress() {
        return this.mDevice.getAddress();
    }

    public BluetoothClass getBluetoothClass() {
        return this.mDevice.getBluetoothClass();
    }

    public int getBondState() {
        return this.mDevice.getBondState();
    }

    public String getName() {
        return this.mDevice.getName();
    }

    public String toString() {
        return this.mDevice.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mDevice.writeToParcel(out, flags);
    }

    public BluetoothSocket createRfcommSocket(int channel) throws Exception {
        if(_createRfcommSocket == null) {
            throw new NoSuchMethodException("createRfcommSocket");
        } else {
            try {
                return (BluetoothSocket)_createRfcommSocket.invoke(this.mDevice, new Object[]{Integer.valueOf(channel)});
            } catch (InvocationTargetException var3) {
                if(var3.getCause() instanceof Exception) {
                    throw (Exception)var3.getCause();
                } else {
                    throw var3;
                }
            }
        }
    }

    public BluetoothSocket createInsecureRfcommSocket(int channel) throws Exception {
        if(_createInsecureRfcommSocket == null) {
            throw new NoSuchMethodException("createInsecureRfcommSocket");
        } else {
            try {
                return (BluetoothSocket)_createInsecureRfcommSocket.invoke(this.mDevice, new Object[]{Integer.valueOf(channel)});
            } catch (InvocationTargetException var3) {
                if(var3.getCause() instanceof Exception) {
                    throw (Exception)var3.getCause();
                } else {
                    throw var3;
                }
            }
        }
    }

    public BluetoothSocket createLCAPSocket(int channel) throws Exception {
        if(_socketConstructor == null) {
            throw new NoSuchMethodException("new BluetoothSocket");
        } else {
            try {
                return (BluetoothSocket)_socketConstructor.newInstance(new Object[]{Integer.valueOf(3), Integer.valueOf(-1), Boolean.valueOf(true), Boolean.valueOf(true), this.mDevice, Integer.valueOf(channel), null});
            } catch (InvocationTargetException var3) {
                if(var3.getCause() instanceof Exception) {
                    throw (Exception)var3.getCause();
                } else {
                    throw var3;
                }
            }
        }
    }

    public BluetoothSocket createInsecureLCAPSocket(int channel) throws Exception {
        if(_socketConstructor == null) {
            throw new NoSuchMethodException("new BluetoothSocket");
        } else {
            try {
                return (BluetoothSocket)_socketConstructor.newInstance(new Object[]{Integer.valueOf(3), Integer.valueOf(-1), Boolean.valueOf(false), Boolean.valueOf(false), this.mDevice, Integer.valueOf(channel), null});
            } catch (InvocationTargetException var3) {
                if(var3.getCause() instanceof Exception) {
                    throw (Exception)var3.getCause();
                } else {
                    throw var3;
                }
            }
        }
    }

    public boolean setPin(byte[] pin) throws Exception {
        if(_setPin == null) {
            throw new NoSuchMethodException("setPin");
        } else {
            try {
                return ((Boolean)_setPin.invoke(this.mDevice, new Object[]{pin})).booleanValue();
            } catch (InvocationTargetException var3) {
                if(var3.getCause() instanceof Exception) {
                    throw (Exception)var3.getCause();
                } else {
                    throw var3;
                }
            }
        }
    }

    public boolean setPasskey(int passkey) throws Exception {
        if(_setPasskey == null) {
            throw new NoSuchMethodException("setPasskey");
        } else {
            try {
                return ((Boolean)_setPasskey.invoke(this.mDevice, new Object[]{Integer.valueOf(passkey)})).booleanValue();
            } catch (InvocationTargetException var3) {
                if(var3.getCause() instanceof Exception) {
                    throw (Exception)var3.getCause();
                } else {
                    throw var3;
                }
            }
        }
    }

}
