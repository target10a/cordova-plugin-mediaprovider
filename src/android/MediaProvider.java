package com.target10a.cordova.mediaprovider;

import android.text.TextUtils;
import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.content.res.AssetFileDescriptor;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class MediaProvider extends CordovaPlugin {

    private static final String TAG = "[MediaProvider plugin]: ";
    private static final int INVALID_ACTION_ERROR_CODE = -1;
    private static CallbackContext callback;
    private static String uriStr;
    public static final int READ_REQ_CODE = 0;
    public static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;

    protected void getReadPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, READ);
    }

    public void initialize(CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackContext The callback context through which to return stuff to caller.
     * @return              A PluginResult object with a status and message.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callback = callbackContext;
        this.uriStr = args.getString(0);

        if (action.equals("readText")) {
            if (PermissionHelper.hasPermission(this, READ)) {
                readText();
            }
            else {
                getReadPermission(READ_REQ_CODE);
            }

            return true;
        }
        else {
            JSONObject resultObj = new JSONObject();

            resultObj.put("code", INVALID_ACTION_ERROR_CODE);
            resultObj.put("message", "Invalid action.");

            callbackContext.error(resultObj);
        }

        return false;
    }

    public void readText() throws JSONException {
        JSONObject resultObj = new JSONObject();
        /* content:///... */
        Uri pvUrl = Uri.parse(this.uriStr);

        Log.d(TAG, "URI: " + this.uriStr);

        Context appContext = this.cordova.getActivity().getApplicationContext();

        String content = readFile(pvUrl, appContext);

        this.callback.success(content);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                JSONObject resultObj = new JSONObject();
                resultObj.put("code", 3);
                resultObj.put("message", "Filesystem permission was denied.");

                this.callback.error(resultObj);
                return;
            }
        }

        if (requestCode == READ_REQ_CODE) {
            readText();
        }
    }

    private static String readFile(Uri uri, Context context) {
        try {
            AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            FileChannel channel = new FileInputStream(assetFileDescriptor.getFileDescriptor()).getChannel();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int bufferSize = 2048;
            if(bufferSize >channel.size())

            {
                bufferSize = (int) channel.size();
            }

            ByteBuffer buff = ByteBuffer.allocate(bufferSize);

            while(channel.read(buff)>0)

            {
                out.write(buff.array(), 0, buff.position());
                buff.clear();
            }

            String fileContent = new String(out.toByteArray(), StandardCharsets.UTF_8);
            return fileContent;
        }catch (IOException e) {
            e.printStackTrace();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}