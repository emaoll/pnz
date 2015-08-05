package com.acceleratedio.pac_n_zoom;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.VideoView;
import android.content.Intent;


//Implement SurfaceHolder interface to Play video
//Implement this interface to receive information about changes to the surface
public class ViewVideos extends Activity implements SurfaceHolder.Callback{

    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean pausing = false;;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewvideo);

        getWindow().setFormat(PixelFormat.UNKNOWN);

        //Displays a video file.   
        VideoView mVideoView = (VideoView)findViewById(R.id.vviidd);






        String uriPath = "com.acceleratedio.pac_n_zoom.ViewVideos";
        Uri uri = Uri.parse(uriPath);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
    }


    public void bbaacckk(View view){
        Intent intent = new Intent(this, PickAnmActivity.class);
        startActivity(intent);
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }
}