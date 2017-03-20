package com.example.inwon.opencvtest;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private Mat img_imput;
    private Mat img_result;
    private static final String TAG = "opencv";
    private CameraBridgeViewBase opencvcameraview;

    static final int PERMISSION_REQUST_CODE = 1;
    String[] PERMISSION = {"android.permission.CAMERA"};

    public native int convertNativeLib(long matAddrInput, long marAddrResult);

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
    private boolean haspermissions(String[] permissions){
        int ret = 0;
        for(String perms: permissions){
            ret = checkCallingOrSelfPermission(perms);
            if(!(ret== PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된경우
                return false;
            }
        }
        return true;
    }
    private void requestNecessaryPermissions(String[] permissions){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
           requestPermissions(permissions,PERMISSION_REQUST_CODE);
        }
    }

    private BaseLoaderCallback mLoderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface.SUCCESS:{
                    opencvcameraview.enableView();
                }break;
                default:{
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(!cameraAccepted){
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }else {
                            // 퍼미션 허가받음
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("알림");
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    requestPermissions(PERMISSION,PERMISSION_REQUST_CODE);
                }
            }
        });
        dialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!haspermissions(PERMISSION)){
            requestNecessaryPermissions(PERMISSION);
        }else{
            //이미 퍼미션 허가받음
        }
        opencvcameraview = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        opencvcameraview.setVisibility(SurfaceView.VISIBLE);
        opencvcameraview.setCvCameraViewListener(this);
        opencvcameraview.setCameraIndex(0);mLoderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(opencvcameraview != null){
            opencvcameraview.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoderCallback);
        }else {
            mLoderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(opencvcameraview != null){
            opencvcameraview.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        img_imput = inputFrame.rgba();
        img_result = new Mat();
        convertNativeLib(img_imput.getNativeObjAddr(),img_result.getNativeObjAddr());
        return img_result;
    }
}
