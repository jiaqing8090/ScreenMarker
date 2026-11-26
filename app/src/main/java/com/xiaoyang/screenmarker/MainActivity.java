package com.xiaoyang.screenmarker;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.*;

public class MainActivity extends Activity {
    static final int REQ_OVERLAY=71;
    public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32,40,32,24);
        TextView text=new TextView(this);
        text.setText("屏幕画笔\n\n开启悬浮画笔后，启动系统录屏即可录下屏幕标注。支持自由画、箭头、圆圈、矩形、单擦、撤销和一键全擦。\n\n首次使用请允许‘显示在其他应用上层’。工具栏收起后可继续操作屏幕，右侧标签可恢复工具栏。\n\n提示：部分手机录屏可能不包含悬浮窗，请先录一段短视频测试。");
        text.setTextSize(18);
        layout.addView(text);
        Button start=new Button(this);
        start.setText("开启悬浮画笔");
        layout.addView(start);
        start.setOnClickListener(v->{
            if(!Settings.canDrawOverlays(this)){
                Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
                startActivityForResult(intent,REQ_OVERLAY);
            }else{
                startService(new Intent(this,OverlayService.class));
                Toast.makeText(this,"画笔悬浮窗已开启",Toast.LENGTH_SHORT).show();
            }
        });
        setContentView(layout);
    }
    protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==REQ_OVERLAY){
            if(Settings.canDrawOverlays(this)){
                startService(new Intent(this,OverlayService.class));
                Toast.makeText(this,"画笔悬浮窗已开启",Toast.LENGTH_SHORT).show();
            }else Toast.makeText(this,"未获得悬浮窗权限",Toast.LENGTH_LONG).show();
        }
    }
}
