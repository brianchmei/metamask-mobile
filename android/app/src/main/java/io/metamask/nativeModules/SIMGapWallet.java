package io.metamask.nativeModules;

import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.util.Log;

import android.app.Activity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.Vector;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import android.view.WindowManager;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.taisys.simgap.apps.wallet.OnCardWallet;
import com.taisys.simgap.apps.wallet.OnCardWalletException;


import com.taisys.simgap.utils.QCrypto;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;


public class SIMGapWallet extends ReactContextBaseJavaModule // implements ActivityCompat.OnRequestPermissionsResultCallback
{
static {
        Security.removeProvider("BC");
        // Confirm that positioning this provider at the end works for your needs!
        Security.addProvider(new BouncyCastleProvider());
	Log.i("------------ Keccak-256(00) -------------->", Util.a2hex(QCrypto.keccak(QCrypto.KECCAK_256, new byte[1])));

}


	private final static String ERR_TAG = "Error: ";
	private final static String RES_TAG = "Result:";

	private final static int FID_NEW_WALLET            = 0;
	private final static int FID_AVAILABLE             = 1;
	private final static int FID_SIGN_TRANSACTION      = 2;
	private final static int FID_SIGN_MESSAGE          = 3;
	private final static int FID_SIGN_PERSONAL_MESSAGE = 4;
	private final static int FID_SIGN_TYPED_DATA       = 5;
	private final static int FID_DECRYPT_MESSAGE       = 6;

	private final static String[] FNAMES = {
		"newWallet",
		"available",
		"signTransaction",
		"signMessage",
		"signPersonalMessage",
		"signTypedData",
		"decryptMessage"
	};


	private final static int WALLET_INF_SIZE = 100;
	private final static String WALLET_AID = "A0000000185078646A61636172642D31 ";
	private final static String WALLET_TAR = "584443";
	private final static String WALLET_PATH = "m/44'/60'/0'/0/0"; // null for non-static path, supported in stage 2

//////////////////////////////////////////////////////////////////////////////////////////////
// Bitcoin Address:
// Legacy (P2PKH) startsWith("1")            1Fh7ajXabJBpZPZw8bjD3QU4CuQ3pRty9u
// Nested SegWit (P2SH) startsWith("3")      3KF9nXowQ4asSGxRRzeiTpDjMuwM2nypAN
// Native SegWit (Bech32)  startsWith("bc1") bc1qf3uwcxaz779nxedw0wry89v9cjh9w2xylnmqc3
//------------------------------------------------------------------------------------------
// Ethereum Address:
// 42 chars, startsWith("0x")                0x7F9B8EE7Eb4a7c0f9e16BCE6FAADA7179E84F3B9
//////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////////////////////
// Format Define of simgapListWallets() Return Data
// - | Wallet ID (16) | Path (20 bytes) | LV of Address Text |
//////////////////////////////////////////////////////////////////////////////////////////////


	private static String FAKE_WALL_ID_1 = "A0123456789BCDEF0000000000000001";
	private static String FAKE_ADDRESS_1 = "1FAKE01abJBpZPZw8bjD3QU4CuQ3pRty9u";
	private static String FAKE_WALL_ID_2 = "B0123456789BCDEF0000000000000002";
	private static String FAKE_ADDRESS_2 = "3FAKE02wQ4asSGxRRzeiTpDjMuwM2nypAN";
	private static String FAKE_WALL_ID_3 = "B0123456789BCDEF0000000000000003";
	private static String FAKE_ADDRESS_3 = "bc1FAKE03qfw779nxedw0wry89v9cjh9w2xylnmqc3";
	private static String FAKE_WALL_ID_4 = "B0123456789BCDEF0000000000000004";
	private static String FAKE_ADDRESS_4 = "0xFACE04A2FE1A7F9B8EE7Eb4a7c0f9e16BCE6FAAD";
	private static String FAKE_KEYPATH_1 = "8000002C80000001800000000000000000000000";  // BTC:
	private static String FAKE_KEYPATH_2 = "8000002C80000001800000000000000000000000";  // BTC:
	private static String FAKE_KEYPATH_3 = "8000002C80000001800000000000000000000000";  // BTC:
	private static String FAKE_KEYPATH_4 = "8000002C8000003C800000000000000000000000";  // ETH: 0xAbcDe...28 (0x + 40 ASCII)

	private static String FAKE_ACCOUNT_1 = " " + FAKE_WALL_ID_1 // Wallet Identifier, 16 bytes
	                                     + " " + FAKE_KEYPATH_1 // PATH, 20 bytes
	                                     + " " + Util.LV((FAKE_ADDRESS_1).getBytes()); // Address, LV of address text

	private static String FAKE_ACCOUNT_2 = " " + FAKE_WALL_ID_2 // Wallet Identifier, 16 bytes
	                                     + " " + FAKE_KEYPATH_2 // PATH, 20 bytes
	                                     + " " + Util.LV((FAKE_ADDRESS_2).getBytes()); // Address, LV of address text

	private static String FAKE_ACCOUNT_3 = " " + FAKE_WALL_ID_3 // Wallet Identifier, 16 bytes
	                                     + " " + FAKE_KEYPATH_3 // PATH, 20 bytes
	                                     + " " + Util.LV((FAKE_ADDRESS_3).getBytes()); // Address, LV of address text

	private static String FAKE_ACCOUNT_4 = " " + FAKE_WALL_ID_4 // Wallet Identifier, 16 bytes
	                                     + " " + FAKE_KEYPATH_4 // PATH, 20 bytes
	                                     + " " + Util.LV((FAKE_ADDRESS_4).getBytes()); // Address, LV of address text

	private static String REAL_ACCOUNT_DUP_1 = "F514BF9B8210FDBEA3603FC7079722508000002C8000003C8000000000000000000000002a307861393133383265313838376639323631353934453538453436653232333831343730456530646533";


	private final static int FUNC_LIST_WALLET       = 0x00A17232;
	private final static int FUNC_SIGN_TRANSACTION 	= 0x001E1342;

	private static final String SIMGAP_WALLET_ERROR_CODE = "SIMGAP_WALLET_ERROR_CODE";
	private final ReactApplicationContext reactContext;

	private OnCardWallet _w;
	private SIMGapUI _ui;

	SIMGapWallet(ReactApplicationContext context) {
		super(context);

		reactContext = context;
		_ui = new SIMGapUI(context);
		_w = OnCardWallet.getInstance(_ui, context, null);


	}

	@Override
	public String getName() {
		return "SIMGapWallet";
	}

	public void log(String event, String msg) {
		if (event=="EVENT_SIMGAP_ERROR") Log.println(Log.ERROR, "SIMGapWallet.java", ">>>" + event + ">>> " + msg);
		else Log.println(Log.INFO, "SIMGapWallet.java", ">>>" + event + ">>> " + msg);
		if (reactContext == null) {
			Log.println(Log.ERROR, "SIMGapWallet.Java", "ReactContext is null");
			return;
		}
		reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
		.emit(event, msg);
	}

	public void log(String msg) {
		log("EVENT_SIMGAP_LOG", msg);
	}

	public void err(String msg) {
		log("EVENT_SIMGAP_ERROR", msg);
	}

	public void alert(String msg) {
		log("EVENT_SIMGAP_ALERT", msg);
	}

	public byte[] decodeUsingBigInteger(String hexString) {
		String tmp = hexString;
		if (hexString.startsWith("0x")) 
			tmp = hexString.substring(2);

		byte[] byteArray = new BigInteger(tmp, 16).toByteArray();
		if (byteArray[0] == 0) {
			byte[] output = new byte[byteArray.length - 1];
			System.arraycopy(
			byteArray, 1, output, 
			0, output.length);
			return output;
		}
		return byteArray;
	}

	public String encodeUsingBigIntegerToString(byte[] bytes) {
		BigInteger bigInteger = new BigInteger(1, bytes);
		return bigInteger.toString(16);
	}



	private String[] _results = new String[FNAMES.length];

	public Activity getActivity() {return getCurrentActivity();}

	@ReactMethod
	public void fetchResult(int funcID, Promise promise) {
		if (funcID<0 || funcID>=_results.length || _results[funcID]==null) {err("Error: Cannot fetch result.");  promise.resolve("null"); return;}
		String s = _results[funcID];
		if (s.startsWith(ERR_TAG)) {err(s);  promise.resolve("null"); return;}
		if (s.startsWith(RES_TAG)) {log(s);  promise.resolve(s.substring(RES_TAG.length())); return;}
		err("Invalid result information: " + s);
		promise.resolve("null");
	}

static boolean USE_FAKE_DATA_WHEN_ERROR = false;


	@ReactMethod
	public void available(Promise promise) {
		int fid = FID_AVAILABLE;
		_ui.setActivity(getCurrentActivity());
		if (!asyncWalletFunction(fid, new IJobs()
		{
			@Override
			public void doJobs()
			{
				_results[fid] = null;
				try {
					String res = _w.simgapListWallets();
					_results[fid] = RES_TAG + res;
				} catch (OnCardWalletException e)
				{
					Log.e("SIMGapWallet.java", "Exception during simgapListWallets()", e);
if (OnCardWallet.USE_REMOTE_READER) Log.e("----RM INFO----", com.taisys.simgap.drv.ChannelBasedSIMController.getServiceErrorInfoString(512));
else
{
Log.e("----CS INFO----", com.taisys.simgap.drv.ChannelBasedSIMController.getServiceErrorInfoString(com.taisys.simgap.SIMController.SERVICE_CARRIER_SERVICES));
Log.e("", "");
Log.e("----AB INFO----", com.taisys.simgap.drv.ChannelBasedSIMController.getServiceErrorInfoString(com.taisys.simgap.SIMController.SERVICE_OMAPI_ANDROID_BINDING));
Log.e("", "");
Log.e("----OM INFO----", com.taisys.simgap.drv.ChannelBasedSIMController.getServiceErrorInfoString(com.taisys.simgap.SIMController.SERVICE_OMAPI_SEEK_FOR_ANDROID));
Log.e("", "");
}


if (USE_FAKE_DATA_WHEN_ERROR)
{
				///// Fake Data /////
					Log.i("SIMGapWallet.java", "==== Use fake data ====");
//					Log.i("SIMGapWallet.java", "    ACCOUNT 1");
//					Log.i("SIMGapWallet.java", "             ID:" + FAKE_WALL_ID_1);
//					Log.i("SIMGapWallet.java", "           Path:" + FAKE_KEYPATH_1);
//					Log.i("SIMGapWallet.java", "        Address:" + FAKE_ADDRESS_1);
//					Log.i("SIMGapWallet.java", "    ACCOUNT 2");
//					Log.i("SIMGapWallet.java", "             ID:" + FAKE_WALL_ID_2);
//					Log.i("SIMGapWallet.java", "           Path:" + FAKE_KEYPATH_2);
//					Log.i("SIMGapWallet.java", "        Address:" + FAKE_ADDRESS_2);
//					Log.i("SIMGapWallet.java", "    ACCOUNT 3");
//					Log.i("SIMGapWallet.java", "             ID:" + FAKE_WALL_ID_3);
//					Log.i("SIMGapWallet.java", "           Path:" + FAKE_KEYPATH_3);
//					Log.i("SIMGapWallet.java", "        Address:" + FAKE_ADDRESS_3);
//					Log.i("SIMGapWallet.java", "    ACCOUNT 4");
//					Log.i("SIMGapWallet.java", "             ID:" + FAKE_WALL_ID_4);
//					Log.i("SIMGapWallet.java", "           Path:" + FAKE_KEYPATH_4);
//					Log.i("SIMGapWallet.java", "        Address:" + FAKE_ADDRESS_4);
//					Log.i("SIMGapWallet.java", "Fake String is: " + FAKE_ACCOUNT_1 + FAKE_ACCOUNT_2 + FAKE_ACCOUNT_3 + FAKE_ACCOUNT_4);
					_results[fid] = RES_TAG + REAL_ACCOUNT_DUP_1;
					return;
}
					_results[fid] = ERR_TAG + " " + e.getReasonName();
				}
			}

		}))  promise.resolve("null");
		else promise.resolve("" + fid);
	}

	@ReactMethod
	public void newWallet(Promise promise) {
		String func = "newWallet";
//		if (!asyncWalletFunction(func)) {promise.resolve("null"); return;}
		unsupport(func, promise);
	}

	@ReactMethod
	public void signMessage(String address, String data, Promise promise) {
		String func = "signMessage";
//		_para_address = address;
//		_para_data = data;
//		if (!asyncWalletFunction(func)) {promise.resolve("null"); return;}
		unsupport(func, promise);
	}

	@ReactMethod
	public void signPersonalMessage(String address, String data, Promise promise) {
		String func = "signPersonalMessage";
//		_para_address = address;
//		_para_data = data;
//		if (!asyncWalletFunction(func)) {promise.resolve("null"); return;}
		unsupport(func, promise);
	}

	@ReactMethod
	public void signTransaction(String address, int transType, String data, int chainId, Promise promise) {
		int fid = FID_SIGN_TRANSACTION;
		String func = FNAMES[fid];
		log("Function " + func + "() is calling.");
		_results[fid] = null;
		// TODO: Compose parameters
		int n = 1;
		// byte[] hashes = Util.hex2ba(data);
		byte[] hashes = decodeUsingBigInteger(data);
		try {
		    byte[] sig = _w.simgapSignData(address, n, hashes);
Log.i("SIMGapWallet.java", "Transaction Signature (" + sig.length + " bytes) = " + Util.a2hex(sig));
		    // promise.resolve(RES_TAG + Util.a2hex(sig));
			promise.resolve(Util.a2hex(sig) + "00");
		} catch (OnCardWalletException e) {
			Log.e("SIMGapWallet.java", "Wallet Services sign error. \n" + e.getReasonName());
			promise.resolve(ERR_TAG + "Wallet Services sign error. \n" + e.getReasonName());
		}
	}

	@ReactMethod
	public void signTypedData(String address, String typedData, Promise promise) {
		String func = "signTypedData";
//		_para_address = address;
//		_para_data = typedData;
//		if (!asyncWalletFunction(func)) {promise.resolve("null"); return;}
		unsupport(func, promise);
	 }

	@ReactMethod
	public void decryptMessage (String address, String encryptedData, Promise promise) {
		String func = "decryptMessage";
//		_para_address = address;
//		_para_data = encryptedData;
//		if (!asyncWalletFunction(func)) {promise.resolve("null"); return;}
		unsupport(func, promise);
	}

	@ReactMethod
	public void getEncryptionPublicKey (String address, Promise promise) {
		String func = "getEncryptionPublicKey";
//		_para_address = address;
//		if (!asyncWalletFunction(func)) {promise.resolve("null"); return;}
		unsupport(func, promise);

	}





	private interface IJobs {
		public void doJobs() throws Throwable;
	}


	private void unsupport(String funcName, Promise promise) { err("Unsupported Function " + funcName + "() in current version."); promise.resolve("null"); }



	private boolean asyncWalletFunction(int fid, IJobs jobs)
	{
		String func = FNAMES[fid];
		log("Function " + func + "() is calling.");
		Log.i("SIMGapWallet.java", "Calling connectWallet()");
		Thread tt = new Thread() {
			@Override
			public void run() {
				try {
					jobs.doJobs();
					log("EVENT_SIMGAPWALLET_WAIT_CLOSE", "" + fid + "|" + func);
				}catch(Throwable t)
				{
					Log.e("SIMGapWallet.java", "Function " + func + "() error", t);
					log("EVENT_SIMGAPWALLET_WAIT_ERROR", "" + fid + "|Error: " + t.getMessage() + ".");
				}
			}
		};
		log("EVENT_SIMGAPWALLET_WAITING", "" + fid + "|" + func);
		tt.start();
		return true;
	}


}
