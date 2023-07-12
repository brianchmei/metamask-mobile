package com.taisys.simgap.apps.wallet;

import com.taisys.simgap.OnCardApplication;
import com.taisys.simgap.ui.UI;
import com.taisys.simgap.utils.QCrypto;
import com.taisys.simgap.utils.Util;
import android.util.Log;

import java.util.Hashtable;


public class OnCardWallet {
    private final static int IDX_WALLET_ID     = 0;
    private final static int IDX_ACCOUNT_PATH  = 1;
    private final static int IDX_ACCOUNT_INDEX = 2;

    private static OnCardWallet _me = null;
    private OnCardApplication _app = null;
    private byte[] _apdu = new byte[261];

	private UI _ui;
    private Hashtable<String, String[]> _map = null;

    public final static boolean USE_REMOTE_READER = false;

    public static OnCardWallet getInstance(UI ui, Object para1, Object para2) {
        if (_me==null) {
            OnCardApplication app = OnCardApplication.getInstance(null, Util.hex2ba("584443"), ui, para1, para2);
            if (app==null) return null;
            _me = new OnCardWallet(app);
com.taisys.simgap.drv.ChannelBasedSIMController.setRemoteReaderAddress("192.168.31.77");
com.taisys.simgap.drv.ChannelBasedSIMController.setRemoteReaderPort(9999);
            if (USE_REMOTE_READER) app.specificServiceType(512);
            ui.setDebugLevel(3);
            ui.setVerbose(true);
            _me._ui = ui;
        }
        return _me;
    }
    private OnCardWallet(OnCardApplication app) {_app = app;}

    private int checkResponse(int length) throws Throwable {
        if (length<2 || length>258) {
            String ext = OnCardApplication.getErrorInfo(length);
            OnCardWalletException.throwIt(ext);
        }
        int sw = Util.getU2(_apdu, length-2);
        if (sw!=0x9000) OnCardWalletException.throwIt("Error SW:" + Util.hex((short)sw));
        return length;
    }

    private String getAccountList() throws Throwable {
        _map = new Hashtable<String, String[]>();
        int length = Util.fetchBytes("80F0000000", _apdu);
        length = checkResponse(_app.exchange(_apdu, length))-2;
        if ((length&0x0F)!=0) OnCardWalletException.throwIt("Error data length of Wallet List.");
        String s = null;
        String dat = Util.a2hex(_apdu, 0, length);
        while (dat.length()>0) {
            String inf = getAccountInfo(dat.substring(0, 32));
            if (s==null) s = inf; else s += "|" + inf;
            dat = dat.substring(32);
        }
        return s;
    }

    private String getAccountInfo(String sWalletID) throws Throwable {
        String[] inf = new String[3];
        int idx = 0;
        inf[IDX_WALLET_ID] = sWalletID;
        inf[IDX_ACCOUNT_PATH] = "8000002C8000003C8000000000000000000000" + Util.hex((byte)idx);
        inf[IDX_ACCOUNT_INDEX] = "" + idx;
        int length = Util.fetchBytes("80F3000024" + sWalletID + inf[IDX_ACCOUNT_PATH], _apdu);
        length = checkResponse(_app.exchange(_apdu, length)) - 2;
        if (length<64) OnCardWalletException.throwIt("Error data length of Public Key.");
        byte[] k = new byte[64];
        System.arraycopy(_apdu, 0, k, 0, 64);
//        String addr = Util.a2hex(QCrypto.eip55(QCrypto.pubkey2Address(QCrypto.ADDRESS_TYPE_ETH, k)).getBytes());
        String addr = QCrypto.eip55(QCrypto.pubkey2Address(QCrypto.ADDRESS_TYPE_ETH, k));
        Log.i("OnCardWallet.java", "getAccountInfo put addr: " + addr.toLowerCase());
        _map.put(addr.toLowerCase(), inf);
        //{ address: string; index: number; balance: string }
        return addr + "@" + inf[IDX_ACCOUNT_INDEX] + "@0";
    }


    private String[] findAddressInMap(String addr)
    {
        Log.i("OnCardWallet.java", "findAddressInMap addr: " + addr);
        String addrL = addr.toLowerCase();
        if (_map==null) return null;
        String index = "0";
        int idx = addr.indexOf("@");
        if (idx>=0) {index=addr.substring(idx+1); addr=addr.substring(0, idx);}

        Log.i("OnCardWallet.java", "findAddressInMap addr2: " + addrL);

        String[] inf = _map.get(addrL);
        if (inf!=null) {
            Log.i("OnCardWallet.java", "inf[IDX_ACCOUNT_INDEX]: " + inf[IDX_ACCOUNT_INDEX] + ", index: " + index);
        } else {
            Log.i("OnCardWallet.java", "inf is null!!!!");
        }
        if (inf!=null && inf[IDX_ACCOUNT_INDEX].equalsIgnoreCase(index)) return inf;
        Log.i("OnCardWallet.java", "return null!!!!");
        return null;
    }

    public String simgapListWallets() throws OnCardWalletException{
        int res;
        String [] ss = new String[1];
        res = _app.runSession(new OnCardApplication.ApplicationSession() {
            @Override
            public int inSession() throws Throwable {
            	try {
           	    	ss[0] = getAccountList();
            	} catch (Throwable t) {
            		_ui.err("simgapListWallets()->getAccountList()", Util.getStackTrace(t));
            		throw t;
            	}
                return 0;
            }
        });
        if (res!=0) OnCardWalletException.throwIt(OnCardApplication.getErrorInfo(res));
        return ss[0];
    }

    public byte[] simgapSignData(String addr, int numPrecomputedHashes, byte[] precomputedHashesArray) throws OnCardWalletException{
        int res;
        String [] ss = new String[1];
        res = _app.runSession(new OnCardApplication.ApplicationSession() {
            @Override
            public int inSession() throws Throwable {
                if (_map==null) getAccountList();
                String[] inf = findAddressInMap(addr);
                if (inf==null) OnCardWalletException.throwIt("Cannot find account of address " + addr + ".");
                String cmd = "80F400002C0631313131313125" + inf[IDX_WALLET_ID] + inf[IDX_ACCOUNT_PATH]  + Util.hex((byte)numPrecomputedHashes);
                int length = Util.fetchBytes(cmd, _apdu);
                length = checkResponse(_app.exchange(_apdu, length))-2;
                if (length!=0) OnCardWalletException.throwIt("Error data length of set signData.");
                if (numPrecomputedHashes>14) OnCardWalletException.throwIt("Supported Max number of hashes is 14.");

                // Set Hashes
                int MAX = 7;
                int idx = 0;
                while (idx<numPrecomputedHashes) {
                    int end = idx+MAX-1;
                    if (end>=numPrecomputedHashes) end = numPrecomputedHashes-1;
                    cmd = "80F401" + Util.hex((byte)((idx<<4)+end)) + Util.LV(precomputedHashesArray, idx*32, (end+1-idx)*32);
                    length = Util.fetchBytes(cmd, _apdu);
                    length = checkResponse(_app.exchange(_apdu, length))-2;
                    if (length!=0) OnCardWalletException.throwIt("Error data length of set hash for signData.");
                    idx = end+1;
                }

                // Fetch Signatures
                MAX = 3;
                idx = 0;
                String s = "";
                while (idx<numPrecomputedHashes) {
                    int end = idx+MAX-1;
                    if (end>=numPrecomputedHashes) end = numPrecomputedHashes-1;
                    cmd = "80F402" + Util.hex((byte)((idx<<4)+end)) + "00";
                    length = Util.fetchBytes(cmd, _apdu);
                    length = checkResponse(_app.exchange(_apdu, length))-2;
                    if (length==0) OnCardWalletException.throwIt("Error data length of signature for signData.");
                    s += Util.a2hex(_apdu, 0, length);
                    idx = end+1;
                }
                ss[0] = s;
                return 0;
            }
        });
        if (res!=0) OnCardWalletException.throwIt(OnCardApplication.getErrorInfo(res));
        return Util.hex2ba(ss[0]);
    }

}