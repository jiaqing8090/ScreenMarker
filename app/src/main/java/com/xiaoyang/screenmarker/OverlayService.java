package com.xiaoyang.screenmarker;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class OverlayService extends Service {
    WindowManager wm; FrameLayout root; Board board; LinearLayout toolbar; TextView toggle;
    int color=Color.RED, tool=0, colorIndex=0; boolean collapsed=false;
    final int[] colors={Color.RED,0xffffd400,Color.CYAN,Color.WHITE,0xff39d353,0xffff66cc};
    final String[] colorNames={"红","黄","青","白","绿","粉"};
    WindowManager.LayoutParams params;
    public IBinder onBind(Intent i){return null;}
    public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);
      root=new FrameLayout(this); board=new Board();root.addView(board,new FrameLayout.LayoutParams(-1,-1));
      toolbar=new LinearLayout(this);toolbar.setOrientation(LinearLayout.VERTICAL);toolbar.setBackgroundColor(0xee20242b);
      String[][] rows={{"笔","箭头","圆圈","矩形"},{"撤销","单擦","全擦","颜色"},{"收起","关闭"}};
      for(String[] row:rows){LinearLayout r=new LinearLayout(this); for(String label:row){Button b=new Button(this);b.setText(label);b.setTextSize(11);b.setMinWidth(0);b.setPadding(4,0,4,0);r.addView(b,new LinearLayout.LayoutParams(0,52,1));b.setOnClickListener(v->action(label));}toolbar.addView(r);}
      root.addView(toolbar);toggle=new TextView(this);toggle.setText("画笔\n≡");toggle.setTextColor(Color.WHITE);toggle.setTextSize(13);toggle.setGravity(17);toggle.setBackgroundColor(0xee30343b);FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(72,58,Gravity.RIGHT|Gravity.CENTER_VERTICAL);root.addView(toggle,tp);toggle.setVisibility(View.GONE);toggle.setOnClickListener(v->{collapsed=false;toolbar.setVisibility(View.VISIBLE);toggle.setVisibility(View.GONE);});
      params=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);wm.addView(root,params);
    }
    void action(String s){switch(s){case "笔":tool=0;break;case "箭头":tool=1;break;case "圆圈":tool=2;break;case "矩形":tool=3;break;case "撤销":if(!board.items.isEmpty())board.items.remove(board.items.size()-1);break;case "单擦":tool=4;break;case "全擦":board.items.clear();break;case "颜色":colorIndex=(colorIndex+1)%colors.length;color=colors[colorIndex];break;case "收起":collapsed=true;toolbar.setVisibility(View.GONE);toggle.setVisibility(View.VISIBLE);break;case "关闭":stopSelf();return;}board.invalidate();}
    public void onDestroy(){if(root!=null&&root.isAttachedToWindow())wm.removeView(root);super.onDestroy();}
    class Item {int kind,col;float x,y,ex,ey;ArrayList<PointF> points=new ArrayList<>();Item(int k,int c,float a,float b){kind=k;col=c;x=ex=a;y=ey=b;}}
    class Board extends View {
      ArrayList<Item> items=new ArrayList<>();Item current;Paint paint=new Paint(3);float sx,sy;
      Board(){super(OverlayService.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
      protected void onDraw(Canvas canvas){for(Item i:items)drawItem(canvas,i);if(current!=null)drawItem(canvas,current);}
      void drawItem(Canvas c,Item i){paint.setColor(i.col);paint.setStrokeWidth(i.kind==4?34:7);paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
        if(i.kind==0){for(int n=1;n<i.points.size();n++){PointF a=i.points.get(n-1),b=i.points.get(n);c.drawLine(a.x,a.y,b.x,b.y,paint);}}
        else if(i.kind==2)c.drawOval(Math.min(i.x,i.ex),Math.min(i.y,i.ey),Math.max(i.x,i.ex),Math.max(i.y,i.ey),paint);
        else if(i.kind==3)c.drawRect(Math.min(i.x,i.ex),Math.min(i.y,i.ey),Math.max(i.x,i.ex),Math.max(i.y,i.ey),paint);
        else if(i.kind==1){c.drawLine(i.x,i.y,i.ex,i.ey,paint);float a=(float)Math.atan2(i.ey-i.y,i.ex-i.x);float h=28;for(int d=-1;d<=1;d+=2)c.drawLine(i.ex,i.ey,i.ex-h*(float)Math.cos(a+d*.58f),i.ey-h*(float)Math.sin(a+d*.58f),paint);}
        else if(i.kind==4){paint.setStrokeWidth(34);for(int n=1;n<i.points.size();n++){PointF a=i.points.get(n-1),b=i.points.get(n);c.drawLine(a.x,a.y,b.x,b.y,paint);}}
      }
      void eraseAt(float x,float y){float limit=34f;for(int i=items.size()-1;i>=0;i--){Item a=items.get(i);float d;if(a.kind==0){d=pathDistance(a,x,y);}else{float cx=(a.x+a.ex)/2,cy=(a.y+a.ey)/2;d=(float)Math.hypot(x-cx,y-cy);if(a.kind==1)d=Math.min(d,segmentDistance(x,y,a.x,a.y,a.ex,a.ey));}if(d<limit){items.remove(i);return;}}}
      float pathDistance(Item a,float x,float y){float best=Float.MAX_VALUE;for(int n=1;n<a.points.size();n++){PointF p=a.points.get(n-1),q=a.points.get(n);best=Math.min(best,segmentDistance(x,y,p.x,p.y,q.x,q.y));}return best;}
      float segmentDistance(float x,float y,float x1,float y1,float x2,float y2){float dx=x2-x1,dy=y2-y1,t=((x-x1)*dx+(y-y1)*dy)/(dx*dx+dy*dy+0.0001f);t=Math.max(0,Math.min(1,t));return (float)Math.hypot(x-(x1+t*dx),y-(y1+t*dy));}
      public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getRawX(),y=e.getRawY();if(tool==4){if(e.getAction()==0||e.getAction()==2){eraseAt(x,y);invalidate();}return true;}if(e.getAction()==0){sx=x;sy=y;current=new Item(tool,color,x,y);current.points.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==2||e.getAction()==1){if(current==null)return true;current.ex=x;current.ey=y;if(tool==0)current.points.add(new PointF(x,y));if(e.getAction()==1){items.add(current);current=null;}invalidate();return true;}return true;}
    }
}
