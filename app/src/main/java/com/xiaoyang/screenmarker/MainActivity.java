package com.xiaoyang.screenmarker;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.*;
import android.view.View;
import android.os.Build;
import androidx.core.content.FileProvider;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    static final int REQ_OVERLAY=71;
    TextView updateStatus;
    Button updateButton;
    int installedCode;
    String installedName;
    String latestUrl;
    public void onCreate(Bundle b){
        super.onCreate(b);
        try{android.content.pm.PackageInfo pi=getPackageManager().getPackageInfo(getPackageName(),0);installedCode=pi.versionCode; if(Build.VERSION.SDK_INT>=28)installedCode=(int)pi.getLongVersionCode(); installedName=pi.versionName;}catch(Exception e){installedCode=1;installedName="1.0";}
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setPadding(32,40,32,24);
        TextView text=new TextView(this);text.setText("屏幕画笔  v"+installedName+"\n\n小型悬浮工具框：画笔、箭头、圆圈、矩形、单擦、撤销和一键全擦。可拖动工具框，收起后用右侧按钮恢复；穿透模式可操作下面应用。\n\n首次使用请允许‘显示在其他应用上层’。部分手机录屏可能不包含悬浮窗，请先录制短视频测试。\n\n新版会从 GitHub Releases 检查并下载，安装仍需你确认。");text.setTextSize(17);layout.addView(text);
        Button start=new Button(this);start.setText("开启悬浮画笔");layout.addView(start);start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()));startActivityForResult(i,REQ_OVERLAY);}else{startService(new Intent(this,OverlayService.class));Toast.makeText(this,"画笔悬浮窗已开启",Toast.LENGTH_SHORT).show();}});
        updateStatus=new TextView(this);updateStatus.setText("版本：v"+installedName);updateStatus.setPadding(0,24,0,8);layout.addView(updateStatus);
        updateButton=new Button(this);updateButton.setText("检查更新");layout.addView(updateButton);updateButton.setOnClickListener(v->checkUpdate());
        setContentView(layout);checkUpdate();
    }
    void checkUpdate(){updateStatus.setText("正在检查 GitHub 最新版本…");new Thread(()->{HttpURLConnection c=null;try{URL u=new URL("https://api.github.com/repos/jiaqing8090/ScreenMarker/releases/latest");c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(12000);c.setReadTimeout(12000);c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","ScreenMarker-Android");if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());ByteArrayOutputStream o=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)o.write(buf,0,n);}JSONObject release=new JSONObject(o.toString("UTF-8"));String tag=release.optString("tag_name","");long code=parseVersionCode(tag);String url="";org.json.JSONArray assets=release.optJSONArray("assets");if(assets!=null)for(int i=0;i<assets.length();i++){JSONObject a=assets.getJSONObject(i);String name=a.optString("name","");if(name.toLowerCase().endsWith(".apk")){url=a.optString("browser_download_url","");break;}}final String ftag=tag, furl=url;runOnUiThread(()->{if(code>installedCode&&!furl.isEmpty()){latestUrl=furl;updateStatus.setText("发现新版 "+ftag+"（当前 v"+installedName+"）");updateButton.setText("下载并安装更新");updateButton.setOnClickListener(v->downloadUpdate(latestUrl));}else{updateStatus.setText("已是最新版本 · v"+installedName);updateButton.setText("重新检查");}});}catch(Exception e){runOnUiThread(()->{updateStatus.setText("检查失败，请检查网络；也可打开 GitHub Releases");updateButton.setText("打开版本发布页");updateButton.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/jiaqing8090/ScreenMarker/releases"))));});}finally{if(c!=null)c.disconnect();}}).start();}
    long parseVersionCode(String s){try{String[] p=s.replaceFirst("^[vV]","").split("[.+-]");long a=Long.parseLong(p[0]),b=p.length>1?Long.parseLong(p[1]):0,d=p.length>2?Long.parseLong(p[2]):0;return a*10000+b*100+d;}catch(Exception e){return 0;}}
    void downloadUpdate(String url){updateButton.setEnabled(false);updateStatus.setText("正在下载更新…");new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(30000);c.connect();if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());File dir=new File(getCacheDir(),"updates");dir.mkdirs();File apk=new File(dir,"ScreenMarker-update.apk");try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(apk)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}c.disconnect();Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",apk);runOnUiThread(()->{updateButton.setEnabled(true);updateStatus.setText("下载完成，请在系统安装确认页完成更新");Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(uri,"application/vnd.android.package-archive");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);try{startActivity(i);}catch(Exception e){updateStatus.setText("无法启动安装器，请从 GitHub Releases 手动下载");}});}catch(Exception e){runOnUiThread(()->{updateButton.setEnabled(true);updateStatus.setText("下载失败：请检查网络或到 GitHub Releases 下载");});}}).start();}
    protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==REQ_OVERLAY){if(Settings.canDrawOverlays(this)){startService(new Intent(this,OverlayService.class));Toast.makeText(this,"画笔悬浮窗已开启",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"未获得悬浮窗权限",Toast.LENGTH_LONG).show();}}
}
