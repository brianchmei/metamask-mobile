package io.metamask.nativeModules;

import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.app.Activity;

import com.taisys.simgap.ui.UI;

public class SIMGapUI extends UI {

	private ReactApplicationContext _ctx;
	private Context _act = null;

    public SIMGapUI(ReactApplicationContext context) {
    	super();
    	_ctx = context;
    }

	public void setActivity(Activity act) {_act = act;}
    @Override
	public void log(boolean force, String title, String msg)
	{
		if (!force && !_verbose) return;
		Log.println(Log.INFO, title, msg);
		if (msg==null) msg = "";
		if (title!=null&&title.length()>0) msg = title + ": " + msg;
		_ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("EVENT_SIMGAP_INFO", msg);
		//updateUI(EVENT_UI_LOG_INFO, Util.getCompactTimeStamp() + " [i] " + msg, null);
	}

	@Override
	public void err(String title, String msg)
	{
		Log.println(Log.ERROR, title, msg);
		if (msg==null) msg = "";
		if (title!=null&&title.length()>0) msg = title + ": " + msg;
		_ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("EVENT_SIMGAP_ERROR", msg);
		//updateUI(EVENT_UI_LOG_INFO, Util.getCompactTimeStamp() + " [e] " + msg, null);
	}

	@Override
	public void dbg(int level, String title, String msg)
	{
		if (level<1) level = 1;
		if (level<=_debug_level)
		{
			Log.println(Log.DEBUG, title, msg);
			if (msg==null) msg = "";
			if (title!=null&&title.length()>0) msg = title + ": " + msg;
			_ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("EVENT_SIMGAP_INFO", "(d)" + msg);
			//updateUI(EVENT_UI_LOG_INFO, Util.getCompactTimeStamp() + " [d] " + msg, null);
		};
	}

	@Override
	public void updateUI(int event, String msg, Object others) {
		_ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("EVENT_SIMGAP_UI_" + Util.hex(event), msg);
	}


    @Override
    public Object getParameter(int id) {
    	if (id<0) return _act;
        return _ctx;
    }

    @Override
    public void popup(String title, String msg, int timeout_ms, ICaller caller, Object... args) {

    }

    @Override
    public void popup(String title, String msg, int buttonFlags, String[] buttonInfo, ICaller caller, Object... args) {

    }

    @Override
    public void popup(String title, String msg, String defaultInput, ICaller caller, Object... args) {

    }

    @Override
    public void popup(String title, String msg, int viewID, int param, ICaller caller, Object... args) {

    }

    @Override
    public void popupWait(String title, String msg, ICaller caller, Object... args) {

    }

	@Override
	public boolean checkSystemFeature(Object obj, String feature)
	{
		// Context context = _act; // (Context)obj;		
		// PackageManager packageManager = context.getPackageManager();
		if (_ctx == null) {
			Log.e("SIMGapUI.java", "_ctx is null !!!!!!");
			return false;
		}

		PackageManager packageManager = _ctx.getPackageManager();
		FeatureInfo[] featuresList = packageManager.getSystemAvailableFeatures();
		for (FeatureInfo f : featuresList) if (f.name != null && f.name.equals(feature)) return true;
		return false;
	}

    @Override
    public void requestPermissions(int para0, Object para1, Object para2, PermissionsRequestListener listener) {

    }

    @Override
    public String lang(int stringID) {
        return null;
    }
}
