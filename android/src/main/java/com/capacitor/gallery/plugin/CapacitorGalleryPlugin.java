package com.capacitor.gallery.plugin;

import android.Manifest;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.provider.MediaStore;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Objects;


@CapacitorPlugin(
  name = "CapacitorGallery",
  permissions = {
      @Permission(
          strings = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
          alias = "publicStorage"
      )
  }
)
public class CapacitorGalleryPlugin extends Plugin {
    @PluginMethod
    public void getGalleryItems(PluginCall call) {
        if (isStoragePermissionGranted()) {
            Log.d("DEBUG LOG", "HAS PERMISSION");
            _getGalleryItems(call);
        } else {
            this.bridge.saveCall(call);
            requestAllPermissions(call, "permissionCallback");
        }
    }

    private void _getGalleryItems(PluginCall call) {
        JSObject response = new JSObject();
        JSArray assets = new JSArray();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
        };

        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media._ID + " DESC");

        int columnIndexId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        int columnIndexURI = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

        int offset = call.getInt("offset", 0);
        int quantity = call.getInt("quantity", 20);

        if (offset >= cursor.getCount()) {
            response.put("results", assets);
            response.put("count", cursor.getCount());
            response.put("nextOffset", Math.min((offset + quantity), cursor.getCount()));
            response.put("nextMaxQuantity", (offset + quantity) > cursor.getCount() ? 0 : cursor.getCount() - (offset + quantity));
            cursor.close();
            call.resolve(response);
            return;
        }

        cursor.moveToPosition(offset);
        while (cursor.getPosition() < offset + quantity) {
            JSObject asset = new JSObject();
            asset.put("id", cursor.getString(columnIndexId));
            asset.put("base64Image", encodeImage(cursor.getString(columnIndexURI)));
            asset.put("creationDate", getCreationDate(cursor.getString(columnIndexURI)));

            assets.put(asset);
            if (!cursor.moveToNext()) {
                break;
            }
        }

        response.put("results", assets);
        response.put("count", cursor.getCount());
        response.put("nextOffset", Math.min((offset + quantity), cursor.getCount()));
        response.put("nextMaxQuantity", (offset + quantity) > cursor.getCount() ? 0 : cursor.getCount() - (offset + quantity));
        cursor.close();
        call.resolve(response);
    }

    @PermissionCallback
    private void permissionCallback(PluginCall call) {
        if (!isStoragePermissionGranted()) {
            Logger.debug(getLogTag(), "User denied storage permission");
            call.reject("Unable to do file operation, user denied permission request");
            return;
        }

        switch (call.getMethodName()) {
            case "getGalleryItems":
                _getGalleryItems(call);
                break;
        }
    }

    private boolean isStoragePermissionGranted() {
        return getPermissionState("publicStorage") == PermissionState.GRANTED;
    }

    private String encodeImage(String path) {
        File imageFile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imageFile);
        } catch(FileNotFoundException error) {
            error.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] bites = baos.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(bites, Base64.DEFAULT);
    }

    private String getCreationDate(String path) {
        File file = new File(path);
        Date lastModDate = new Date(file.lastModified());

        return lastModDate.toString();
    }
}
