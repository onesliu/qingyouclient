package com.qingyou.qingyouclient;

import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

public class SoundPlayer {
	private static SoundPlayer soundManager;
	private Context context;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;

	private SoundPlayer(Context context) {
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				Log.d("sampleId: " + sampleId + " status: " + status );
			}
		});
		
		soundPoolMap = new HashMap<Integer, Integer>();
		soundPoolMap.put(1, soundPool.load(context, R.raw.neworder, 1));
		soundPoolMap.put(2, soundPool.load(context, R.raw.modify, 2));
		soundPoolMap.put(3, soundPool.load(context, R.raw.complete, 3));
		this.context = context;
	}

	public static SoundPlayer newInstance(Context context) {
		if (soundManager == null) {
			soundManager = new SoundPlayer(context);
		}
		return soundManager;
	}
	
	public static SoundPlayer getInstance() {
		return soundManager;
	}

	public void playSound(int type, int vibrate) {
		AudioManager mgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = streamVolumeCurrent / streamVolumeMax;

		/* 使用正确音量播放声音 */
		soundPool.play(soundPoolMap.get(type), volume, volume, 1, 0, 1f);
		
		//震动
		if (vibrate > 0) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
			vibrator.vibrate(new long[] {100, 10, 10, 100}, -1);  //-1短震动
		}
	}

}
