package com.androidadvance.opensourcebitcoinwallet.qrscanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.androidadvance.opensourcebitcoinwallet.BaseActivity;
import com.androidadvance.opensourcebitcoinwallet.R;
import com.androidadvance.opensourcebitcoinwallet.activities.EnterPinActivity;
import com.androidadvance.opensourcebitcoinwallet.activities.ImportWalletActivity;
import com.androidadvance.opensourcebitcoinwallet.activities.MainActivity;
import com.androidadvance.opensourcebitcoinwallet.utils.DialogFactory;
import com.androidadvance.opensourcebitcoinwallet.utils.SecurityHolder;
import com.google.zxing.Result;
import com.socks.library.KLog;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class QRScannerActivity extends BaseActivity implements ZXingScannerView.ResultHandler {

  private static final int REQUEST_CAMERA = 1;
  private ZXingScannerView mScannerView;
  private String type;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mScannerView = new ZXingScannerView(this);
    setContentView(mScannerView);
    int currentapiVersion = Build.VERSION.SDK_INT;
    if (currentapiVersion >= Build.VERSION_CODES.M) {
      if (checkPermission()) {
        KLog.d("We have the camera permission!");
      } else {
        requestPermission();
      }
    }

    Bundle extras = getIntent().getExtras();
    type = extras.getString("type");
  }

  private boolean checkPermission() {
    return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
  }

  private void requestPermission() {
    ActivityCompat.requestPermissions(this, new String[] { CAMERA }, REQUEST_CAMERA);
  }

  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CAMERA:
        if (grantResults.length > 0) {

          boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          if (cameraAccepted) {
            KLog.d("Permission granted. you can now access the camera...");
          } else {
            DialogFactory.error_toast(QRScannerActivity.this, getString(R.string.we_need_camera_permission)).show();
            finish();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              if (shouldShowRequestPermissionRationale(CAMERA)) {
                DialogFactory.warning_toast(QRScannerActivity.this, getString(R.string.we_need_camera_permission)).show();
                new DialogInterface.OnClickListener() {
                  @Override public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                      requestPermissions(new String[] { CAMERA }, REQUEST_CAMERA);
                    }
                  }
                };
                return;
              }
            }
          }
        }
        break;
    }
  }

  @Override public void onResume() {
    super.onResume();

    KLog.d("on resume on QRScanner Activity");
    int currentapiVersion = Build.VERSION.SDK_INT;
    if (currentapiVersion >= Build.VERSION_CODES.M) {
      if (checkPermission()) {
        if (mScannerView == null) {
          mScannerView = new ZXingScannerView(this);
          setContentView(mScannerView);
        }
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
      } else {
        requestPermission();
      }
    } else {
      //There's no need of permission since it's a fucking old phone
      if (mScannerView == null) {
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
      }
      mScannerView.setResultHandler(this);
      mScannerView.startCamera();
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    try {
      mScannerView.stopCamera();
    } catch (Exception ignored) {
    }
  }

  @Override public void handleResult(Result rawResult) {

    final String rawData = rawResult.getText();
    KLog.d("QRCodeScanner", rawData);
    KLog.d("QRCodeScanner", rawResult.getBarcodeFormat().toString());

    KLog.d("WE HAVE " + type + " = " + rawData);

    SecurityHolder.btcAddress = rawData;
    SecurityHolder.storeBTCAddress(QRScannerActivity.this, rawData);

    startActivity(new Intent(QRScannerActivity.this, EnterPinActivity.class));
    finish();
  }
}