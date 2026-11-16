package com.hyeoni.pocketsnake;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        setContentView(new Game());
    }
    class Game extends View {
        final int cells=20;
        final ArrayList<Integer> snake=new ArrayList<>();
        final Random random=new Random();
        final Paint p=new Paint(3);
        int dx=1,dy=0,nextDx=1,nextDy=0,food,score=0,best=0;
        boolean over=false,started=false;
        long last=0;
        float touchX,touchY;
        Game(){super(MainActivity.this);reset();}
        void reset(){
            snake.clear();
            snake.add(10*cells+9);snake.add(10*cells+8);snake.add(10*cells+7);
            dx=1;dy=0;nextDx=1;nextDy=0;score=0;over=false;started=false;
            best=getPreferences(0).getInt("best",0);spawn();invalidate();
        }
        void spawn(){
            if(snake.size()==cells*cells){over=true;return;}
            do{food=random.nextInt(cells*cells);}while(snake.contains(food));
        }
        void turn(int x,int y){if(x!=-dx||y!=-dy){nextDx=x;nextDy=y;}}
        void step(){
            dx=nextDx;dy=nextDy;
            int head=snake.get(0),x=head%cells+dx,y=head/cells+dy;
            if(x<0||x>=cells||y<0||y>=cells){over=true;return;}
            int newHead=y*cells+x;
            boolean eat=newHead==food;
            int limit=snake.size()-(eat?0:1);
            for(int i=0;i<limit;i++)if(snake.get(i)==newHead){over=true;return;}
            snake.add(0,newHead);
            if(eat){score+=10;if(score>best){best=score;getPreferences(0).edit().putInt("best",best).apply();}spawn();}
            else snake.remove(snake.size()-1);
        }
        void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);}
        void text(Canvas c,String s,float x,float y,float size,int color){
            color(color);p.setTextSize(size);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD));c.drawText(s,x,y,p);
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(),h=getHeight(),unit=Math.min(w/cells,(h*.70f)/cells);
            float left=(w-unit*cells)/2,top=(h-unit*cells)/2+10;
            c.drawColor(Color.rgb(12,20,34));
            text(c,"POCKET SNAKE",w/2,Math.max(58,top-78),Math.min(36,w*.083f),Color.rgb(133,238,181));
            text(c,"SCORE  "+score+"     BEST  "+best,w/2,Math.max(88,top-38),Math.min(19,w*.044f),Color.WHITE);
            color(Color.rgb(26,43,58));c.drawRoundRect(new RectF(left,top,left+unit*cells,top+unit*cells),16,16,p);
            color(Color.rgb(34,53,67));
            for(int i=1;i<cells;i++){c.drawRect(left+i*unit-0.4f,top,left+i*unit+0.4f,top+unit*cells,p);c.drawRect(left,top+i*unit-0.4f,left+unit*cells,top+i*unit+0.4f,p);}
            color(Color.rgb(255,112,125));
            c.drawCircle(left+(food%cells+.5f)*unit,top+(food/cells+.5f)*unit,unit*.34f,p);
            for(int i=snake.size()-1;i>=0;i--){
                int pos=snake.get(i);color(i==0?Color.rgb(173,255,172):Color.rgb(67,208,147));
                float x=left+(pos%cells)*unit,y=top+(pos/cells)*unit;
                c.drawRoundRect(new RectF(x+2,y+2,x+unit-2,y+unit-2),unit*.23f,unit*.23f,p);
            }
            if(!started||over){
                color(0xD90C1422);c.drawRoundRect(new RectF(left,top+unit*6,left+unit*cells,top+unit*14),20,20,p);
                text(c,over?"GAME OVER":"READY?",w/2,top+unit*9,Math.min(34,w*.085f),Color.WHITE);
                text(c,over?"Tap to restart":"Swipe to start",w/2,top+unit*11,Math.min(19,w*.052f),0xFF9BE9BC);
            }
            text(c,"SWIPE TO STEER",w/2,Math.min(h-25,top+unit*cells+48),Math.min(17,w*.045f),0xFFB0C6D5);
            long now=System.currentTimeMillis();
            if(started&&!over&&now-last>=Math.max(75,165-score/20)){step();last=now;}
            postInvalidateDelayed(25);
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){touchX=e.getX();touchY=e.getY();if(over){reset();}return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){
                float x=e.getX()-touchX,y=e.getY()-touchY;
                if(Math.max(Math.abs(x),Math.abs(y))>20){
                    if(Math.abs(x)>Math.abs(y))turn(x>0?1:-1,0);else turn(0,y>0?1:-1);
                    started=true;last=System.currentTimeMillis();
                }else if(!over){started=true;last=System.currentTimeMillis();}
                invalidate();return true;
            }
            return true;
        }
    }
}
