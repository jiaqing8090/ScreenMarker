package com.xiaoyang.screenmarker;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.Settings;import android.widget.*;import android.view.*;

public class MainActivity extends Activity {
 static final int REQ_OVERLAY=71; boolean waitingPermission=false;
 public void onCreate(Bundle b){super.onCreate(b); LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(32,40,32,24); TextView t=new TextView(this);t.setText("屏幕画笔\n\n开启悬浮画笔后，启动系统录屏即可录下屏幕标注。支持自由画、箭头、圆圈、矩形、橡皮擦和颜色切换。\n\n首次使用需允许‘显示在其他应用上层’。录屏时请将工具栏收起，标注仍会保留。\");t.setTextSize(18);l.addView(t);Button go=new Button(this);go.setText("开启悬浮画笔");l.addView(go);go.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){waitingPermission=true;Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));startActivityForResult(i,REQ_OVERLAY);}else{startService(new Intent(this,OverlayService.class));Toast.makeText(this,"画笔悬浮窗已开启",Toast.LENGTH_SHORT).show();}});setContentView(l);}
 protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==REQ_OVERLAY){if(Settings.canDrawOverlays(this)){startService(new Intent(this,OverlayService.class));Toast.makeText(this,"画笔悬浮窗已开启",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"未获得悬浮窗权限",Toast.LENGTH_LONG).show();}}
}
