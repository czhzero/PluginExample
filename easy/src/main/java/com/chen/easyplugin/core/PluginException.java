package com.chen.easyplugin.core;


/**
 * Created by chenzhaohua on 17/4/7.
 */
public class PluginException extends Exception {

    public final static int	ERROR_CODE_NONE			= 0; // 成功
    public final static int ERROR_CODE_COPY_FILE_APK = 40;
    public final static int ERROR_CODE_COPY_FILE_SO = 41;
	
	int errorCode;

	public PluginException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public PluginException(int errorCode, String detailMessage) {
		super(detailMessage);
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
