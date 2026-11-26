package com.xiaoyang.screenmarker;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.Settings;import android.widget.*;import android.view.*;

public class MainActivity extends Activity {
 public void onCreate(Bundle b){super.onCreate(b); LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(32,40,32,24); TextView t=new TextView(this);t.setText("屏幕画笔\n\n开启悬浮画笔后，启动系统录屏即可录下屏幕标注。支持自由画、箭头、圆圈、矩形、橡皮擦和颜色切换。\n\n首次使用需允许‘显示在其他应用上层’。录屏时请将工具栏收起，标注仍会保留。\");t.setTextSize(18);l.addView(t);Button go=new Button(this);go.setText("开启悬浮画笔");l.addView(go);go.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));startActivity(i);}else startService(new Intent(this,OverlayService.class));});setContentView(l);}
 protected void onResume(){super.onResume(); if(Settings.canDrawOverlays(this)&&getIntent().getBooleanExtra("start",false))startService(new Intent(this,OverlayService.class));}
}
