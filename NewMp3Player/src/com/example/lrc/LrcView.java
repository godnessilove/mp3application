package com.example.lrc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.example.newmp3player.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class LrcView extends TextView {
	private Paint mPaint;
	private HashMap<Long, String> lrcs;
	public int index = 0;
	private int max;
	private int textsize;
	private int midtextsize;
	private Long[] times;
	private String text;
	public float mTouchHistoryY;
	private static final int DY = 30; // 每一行的间隔
	private Context context;

	public LrcView(Context context) {
		super(context);
		init(context);
	}

	public LrcView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setLrcs(HashMap<Long, String> lrcs) {
		this.lrcs = lrcs;
		if (lrcs != null) {
			this.max = getMaxLength(lrcs);
			textsize = getWidth() / max < 30 ? 30 : getWidth() / max;
			midtextsize = getWidth() / max + 10 < 40 ? 40 : getWidth() / max
					+ 10;
		}
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setTimes(Long[] times) {
		this.times = times;
	}

	private void init(Context context) {
		this.context = context;
		setFocusable(true);
		// 非高亮部分
		mPaint = new Paint();
		// 设置Paint为无锯齿
		mPaint.setAntiAlias(true);
		// 设置字体大小
		mPaint.setTextSize(getHeight() / 9);
		// 颜色白色
		mPaint.setColor(Color.WHITE);
		// 风格
		mPaint.setTypeface(Typeface.SERIF);
		// 中间对齐
		mPaint.setTextAlign(Paint.Align.CENTER);

	}

	public static int getMaxLength(HashMap<Long, String> lrcs) {
		int max = 0;
		if (lrcs != null) {
			Collection<String> temp = lrcs.values();
			for (Iterator<String> iterator = temp.iterator(); iterator
					.hasNext();) {
				String string = (String) iterator.next();
				if (max <= string.length()) {
					max = string.length();
				}
			}
		}
		return max;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int allheight = getHeight();
		int allwidth = getWidth();
		if (lrcs != null && times != null) {
			// 中间当前歌词
			mPaint.setAlpha(255);
			mPaint.setTextSize(midtextsize);
			text = lrcs.get(times[index]);
			canvas.drawText(text, allwidth / 2, allheight / 2, mPaint);
			// 中间向上写歌词

			mPaint.setTextSize(textsize);
			int newheight = allheight / 2;
			for (int i = index - 1; i > 0; i--) {
				text = lrcs.get(times[i]);
				newheight -= (textsize + DY);
				if (newheight > 0) {
					float alpha = (float) 255 / allheight * newheight;
					mPaint.setAlpha((int) alpha + 10);
					canvas.drawText(text, allwidth / 2, newheight, mPaint);
				}
			}
			// 中间向下写歌词
			newheight = allheight / 2;
			for (int i = index + 1; i < times.length - 1; i++) {
				text = lrcs.get(times[i]);
				newheight += (textsize + DY);
				if (newheight < allheight) {
					float alpha = (float) 255 / (allheight)
							* (allheight - newheight);
					mPaint.setAlpha((int) alpha + 10);
					canvas.drawText(text, allwidth / 2, newheight, mPaint);
				}

			}

		} else {
			mPaint.setAlpha(255);
			mPaint.setTextSize(50);
			text = this.context.getString(R.string.nolrc);
			canvas.drawText(text, allwidth / 2, allheight / 2, mPaint);
		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
