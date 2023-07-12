package com.taisys.simgap.apps.wallet;

import com.taisys.simgap.SIMControllerException;
import com.taisys.simgap.utils.HeavyDuty;
import com.taisys.simgap.utils.Util;

public class OnCardWalletException extends Throwable{
    public final static int RES_OK = 0;
    public final static int RES_IO_FAILED  = -1;
    public final static int RES_RESP_ERROR  = -3;
    public final static int RES_NO_ROLE  = -4;
    public final static int RES_PIN_ERROR = -8;
    public final static int RES_PARAM_ERROR = -10;
    public final static int RES_NO_ROLE_SPACE = -16;
    public final static int RES_ROLE_EXIST = -18;
    public final static int RES_SIM_BUSY = -20;
    public final static int RES_NO_LANG = -31;
    public final static int RES_CANCEL_OR_TIMEOUT = -98;
    public final static int RES_UNKNOWN_ERROR = -100;
    public final static int RES_EXTEND_ERROR = -200;



    private int _reason;
    private String _ext = null;

    private OnCardWalletException(int reason){super("Reason Code: " + reason); _reason = reason; _ext = null;}
    private OnCardWalletException(String ext){super("Reason Code: " + RES_EXTEND_ERROR); _reason = RES_EXTEND_ERROR; _ext = ext;}
    static void throwIt(int reason) throws OnCardWalletException {throw (new OnCardWalletException(reason));}
    static void throwIt(String ext) throws OnCardWalletException {throw (new OnCardWalletException(ext));}

    /**
     * Get reason code of the exception.
     * @return    Reason code.
     */
    public int getReason() {return _reason;}

    /**
     * Get brief name of reason code
     * @return        Brief name pf reason code
     */
    public String getReasonName()
    {
        switch (_reason) {
            case RES_IO_FAILED: return "Issue with transmitting data";
            case RES_RESP_ERROR: return "Response data error";
            case RES_NO_ROLE: return "Appointed role invalid";
            case RES_PIN_ERROR: return "PIN verification fail";
            case RES_PARAM_ERROR: return "Parameter error";
            case RES_NO_ROLE_SPACE: return "Role does not have enough space";
            case RES_ROLE_EXIST: return "Role already exists";
            case RES_SIM_BUSY: return "SIM Busy";
            case RES_CANCEL_OR_TIMEOUT: return "Operation cancel or timeout";
            case RES_UNKNOWN_ERROR: return "Unknown error";
            case RES_EXTEND_ERROR: return (_ext==null?"Unknown error":_ext);
        }
        return "UNKNOWN 0x" + Util.hex(_reason);
    }
}
