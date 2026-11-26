package com.xiaoyang.screenmarker;

import android.app.Service;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class OverlayService extends Service {
    WindowManager wm; Board board; LinearLayout panel; TextView bubble,status,colorDot;
    WindowManager.LayoutParams boardParams,panelParams,bubbleParams;
    int tool=0,colorIndex=0; boolean drawMode=true;
    final int[] colors={0xffff4d4f,0xffffc107,0xff00bcd4,0xffffffff,0xff4caf50,0xffff66cc};
    final String[] colorNames={"红","黄","青","白","绿","粉"};
    public IBinder onBind(Intent i){return null;}
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    GradientDrawable bg(int color,float radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));g.setStroke(dp(1),0x44ffffff);return g;}

    public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);createBoard();createPanel();createBubble();}
    void createBoard(){board=new Board();boardParams=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);wm.addView(board,boardParams);}
    void createPanel(){panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(10),dp(8),dp(10),dp(10));panel.setBackground(bg(0xee1f232b,18));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView title=label("✦ 屏幕画笔",15);title.setTypeface(null,1);head.addView(title,new LinearLayout.LayoutParams(0,dp(34),1));colorDot=label("●",20);colorDot.setTextColor(colors[colorIndex]);head.addView(colorDot,new LinearLayout.LayoutParams(dp(32),dp(34)));TextView mini=label("—",20);head.addView(mini,new LinearLayout.LayoutParams(dp(38),dp(34)));panel.addView(head);
        addRow(new String[]{"画笔","箭头","圆圈","矩形"});addRow(new String[]{"撤销","单擦","全擦","颜色"});addRow(new String[]{"穿透","关闭"});
        status=label("● 绘制模式 · 可在屏幕上标注",11);status.setTextColor(0xffaeb7c4);status.setGravity(Gravity.CENTER);panel.addView(status,new LinearLayout.LayoutParams(-1,dp(28)));
        panelParams=new WindowManager.LayoutParams(dp(300),-2,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);panelParams.gravity=Gravity.TOP|Gravity.LEFT;panelParams.x=dp(16);panelParams.y=dp(90);wm.addView(panel,panelParams);
        final float[] down=new float[4];head.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){down[0]=e.getRawX();down[1]=e.getRawY();down[2]=panelParams.x;down[3]=panelParams.y;return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){panelParams.x=(int)(down[2]+e.getRawX()-down[0]);panelParams.y=(int)(down[3]+e.getRawY()-down[1]);wm.updateViewLayout(panel,panelParams);return true;}return true;});mini.setOnClickListener(v->collapse());}
    TextView label(String text,float size){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    void addRow(String[] names){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);for(String name:names){TextView b=label(name,12);b.setGravity(Gravity.CENTER);b.setBackground(bg(0xff303641,10));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,p);b.setOnClickListener(v->action(name,b));}panel.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));}
    void createBubble(){
        bubble=label("✎",25);bubble.setGravity(Gravity.CENTER);bubble.setBackground(bg(0xeeff4d4f,24));
        bubbleParams=new WindowManager.LayoutParams(dp(52),dp(52),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        bubbleParams.gravity=Gravity.TOP|Gravity.LEFT;bubbleParams.x=dp(10);bubbleParams.y=dp(300);
        final float[] down=new float[4];final boolean[] moved=new boolean[1];
        bubble.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN){down[0]=e.getRawX();down[1]=e.getRawY();down[2]=bubbleParams.x;down[3]=bubbleParams.y;moved[0]=false;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){float dx=e.getRawX()-down[0],dy=e.getRawY()-down[1];if(Math.abs(dx)>dp(4)||Math.abs(dy)>dp(4))moved[0]=true;if(moved[0]){bubbleParams.x=(int)(down[2]+dx);bubbleParams.y=(int)(down[3]+dy);wm.updateViewLayout(bubble,bubbleParams);}return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){if(!moved[0])expand();return true;}return true;
        });
        bubble.setVisibility(View.GONE);wm.addView(bubble,bubbleParams);
    }
    void collapse(){panel.setVisibility(View.GONE);bubble.setVisibility(View.VISIBLE);}
    void expand(){bubble.setVisibility(View.GONE);panel.setVisibility(View.VISIBLE);}
    void action(String s,TextView button){switch(s){case "画笔":tool=0;setDraw(true);break;case "箭头":tool=1;setDraw(true);break;case "圆圈":tool=2;setDraw(true);break;case "矩形":tool=3;setDraw(true);break;case "撤销":if(!board.items.isEmpty())board.items.remove(board.items.size()-1);break;case "单擦":tool=4;setDraw(true);break;case "全擦":board.items.clear();break;case "颜色":colorIndex=(colorIndex+1)%colors.length;colorDot.setTextColor(colors[colorIndex]);Toast.makeText(this,"颜色："+colorNames[colorIndex],Toast.LENGTH_SHORT).show();break;case "穿透":setDraw(!drawMode);button.setText(drawMode?"穿透":"绘制");break;case "关闭":stopSelf();return;}board.invalidate();}
    void setDraw(boolean enabled){drawMode=enabled;int f=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;if(!enabled)f|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;boardParams.flags=f;if(board.isAttachedToWindow())wm.updateViewLayout(board,boardParams);status.setText(enabled?"● 绘制模式 · 可在屏幕上标注":"○ 穿透模式 · 可操作下面应用");status.setTextColor(enabled?0xff86e89b:0xffaeb7c4);}
    public void onDestroy(){if(board!=null&&board.isAttachedToWindow())wm.removeView(board);if(panel!=null&&panel.isAttachedToWindow())wm.removeView(panel);if(bubble!=null&&bubble.isAttachedToWindow())wm.removeView(bubble);super.onDestroy();}

    class Item{int kind,col;float x,y,ex,ey;ArrayList<PointF> pts=new ArrayList<>();Item(int k,int c,float a,float b){kind=k;col=c;x=ex=a;y=ey=b;}}
    class Board extends View{ArrayList<Item> items=new ArrayList<>();Item cur;Paint p=new Paint(3);Board(){super(OverlayService.this);setLayerType(View.LAYER_TYPE_HARDWARE,null);}protected void onDraw(Canvas c){for(Item i:items)draw(c,i);if(cur!=null)draw(c,cur);}void draw(Canvas c,Item i){p.setColor(i.col);p.setStrokeWidth(dp(4));p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);if(i.kind==0){for(int n=1;n<i.pts.size();n++){PointF a=i.pts.get(n-1),b=i.pts.get(n);c.drawLine(a.x,a.y,b.x,b.y,p);}}else if(i.kind==2)c.drawOval(Math.min(i.x,i.ex),Math.min(i.y,i.ey),Math.max(i.x,i.ex),Math.max(i.y,i.ey),p);else if(i.kind==3)c.drawRect(Math.min(i.x,i.ex),Math.min(i.y,i.ey),Math.max(i.x,i.ex),Math.max(i.y,i.ey),p);else if(i.kind==1){c.drawLine(i.x,i.y,i.ex,i.ey,p);float a=(float)Math.atan2(i.ey-i.y,i.ex-i.x);for(int d=-1;d<=1;d+=2)c.drawLine(i.ex,i.ey,i.ex-dp(18)*(float)Math.cos(a+d*.55f),i.ey-dp(18)*(float)Math.sin(a+d*.55f),p);}}
      public boolean onTouchEvent(MotionEvent e){float x=e.getRawX(),y=e.getRawY();if(tool==4){if(e.getAction()==0||e.getAction()==2){erase(x,y);invalidate();}return true;}if(e.getAction()==0){cur=new Item(tool,colors[colorIndex],x,y);cur.pts.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==2||e.getAction()==1){if(cur==null)return true;cur.ex=x;cur.ey=y;if(tool==0)cur.pts.add(new PointF(x,y));if(e.getAction()==1){items.add(cur);cur=null;}invalidate();return true;}return true;}
      void erase(float x,float y){for(int j=items.size()-1;j>=0;j--){Item i=items.get(j);float d=(float)Math.hypot(x-(i.x+i.ex)/2,y-(i.y+i.ey)/2);if(i.kind==1)d=Math.min(d,seg(x,y,i.x,i.y,i.ex,i.ey));if(i.kind==0)for(int n=1;n<i.pts.size();n++){PointF a=i.pts.get(n-1),b=i.pts.get(n);d=Math.min(d,seg(x,y,a.x,a.y,b.x,b.y));}if(d<dp(25)){items.remove(j);return;}}}float seg(float x,float y,float a,float b,float c,float d){float dx=c-a,dy=d-b,t=((x-a)*dx+(y-b)*dy)/(dx*dx+dy*dy+.001f);t=Math.max(0,Math.min(1,t));return(float)Math.hypot(x-a-t*dx,y-b-t*dy);}}
}
