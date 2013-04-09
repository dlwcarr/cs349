package com.example.cs349a5;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class AnimationView extends View {
	
	private Paint paint;
	
	private Color background;
	
	private ArrayList<Path> shapes;

	public AnimationView(Context context) {
		super(context);
		init();
		}

	public AnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AnimationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3.0f);
		
		shapes = new ArrayList<Path>();
	}
	
	public void setShapes(ArrayList<Path> s) {
		shapes = s;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		for (Path p : shapes) {
			canvas.drawPath(p, paint);
		}
	}

	public void setBackgroundColor(Color c) {
		background = c;
	}
	
	public Color getBackgroundColor() {
		return background;
	}
}
