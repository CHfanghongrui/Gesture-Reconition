package tools.userinterface;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * <p>統一快速處理物件 大小/間距 /點擊狀態/點擊顏色 </p>
 * @author Robert Chou didi31139@gmail.com
 * @date 2015/5/28 下午2:50:52
 * @version 
 */
public class UserInterfaceTool {
	
	
	/**
	* <p>取得螢幕寬度 單位為整數(pixel)</p>
	* @author Robert Chou didi31139@gmail.com
	* @date 2015/5/29 下午1:37:13
	* @version 
	*/
	public static int getScreenWidthPixels(Context context){
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	/**
	* <p>取得螢幕高度 單位為整數(pixel)</p>
	* @author Robert Chou didi31139@gmail.com
	* @date 2015/5/29 下午1:37:13
	* @version 
	*/
	public static int getScreenHeightPixels(Context context){
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	/**
	 * 設定 view的長寬 單位為畫素(pixel)
	 * @param view
	 * @param w
	 * @param h
	 * @author Wang / Robert
	 * @date 2015/5/8 下午3:13:42
	 * @version 
	 */
	public static void setViewSize(View view , int w , int h){
		try {
			view.getLayoutParams().width=w;
			view.getLayoutParams().height=h;
		} catch (Exception e) {
			//如果prams不存在 則重新建立
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(w, h);
			params.width=w;
			params.height=h;
			view.setLayoutParams(params);
		}									
	}
	
	/**
	 * 設定 view的長寬 單位為dp 
	 * @param view 
	 * @param w 
	 * @param h
	 * @author Wang / Robert
	 * @date 2015/5/8 下午3:13:42
	 * @version 
	 */
	public static void setViewSizeByDpUnit(View view , int w , int h){					
		setViewSize(view, getPixelFromDpByDevice(view.getContext(), w), getPixelFromDpByDevice(view.getContext(), h));
	}
	
	/**
	* <p>設定物件間距  單位為畫素(pixel)
	* 上層類別須為 RelativeLayout or LinearLayout </p>
	* @author Wang / Robert Chou didi31139@gmail.com
	* @date 2015/5/26 下午3:25:33
	* @version 
	*/
	public static void setMargin(View view,int leftMargin , int topMargin , int rightMargin , int bottomMargin){
		ViewGroup.LayoutParams params = view.getLayoutParams();		
		if(params instanceof LinearLayout.LayoutParams){			
			((LinearLayout.LayoutParams) params).setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
		}else if (params instanceof RelativeLayout.LayoutParams) {
			((RelativeLayout.LayoutParams) params).setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
		}
		
		view.setLayoutParams(params);
	}
	/**
	* <p>設定物件間距  單位為畫素(pixel)
	* 上層類別須為 RelativeLayout or LinearLayout </p>
	* @author Wang / Robert Chou didi31139@gmail.com
	* @date 2015/5/26 下午3:25:33
	* @version 
	*/
	public static void setMarginByDpUnit(View view,int leftMargin , int topMargin , int rightMargin , int bottomMargin){
		setMargin(view
				,getPixelFromDpByDevice(view.getContext(), leftMargin)
				,getPixelFromDpByDevice(view.getContext(), topMargin) 
				,getPixelFromDpByDevice(view.getContext(), rightMargin) 
				,getPixelFromDpByDevice(view.getContext(), bottomMargin));
	}
	
	/**
	* <p>根據版本加入背景: </p>
	* @author Robert Chou didi31139@gmail.com
	* @date 2015/5/26 下午2:48:26
	* @param rootView 物件存在的集合
	* @param viewID 要尋找的ViewID ex: R.id.viewname
	* @param drawable 要設定的背景圖片
	* @version 
	*/
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setBackground(View view,Drawable drawable){		
		if (VERSION.SDK_INT>=16) {
			view.setBackground(drawable);	
		}else{
			view.setBackgroundDrawable(drawable);
		}				
	}
	
	/**
	* <p> 設定View的文字大小(以360DP寬的比例)</p>
	* @author Robert Chou didi31139@gmail.com
	* @date 2014/11/19 上午12:07:08
	* @param sp 為當前的sp整數下去做轉換 
	*/
	public static void setTextSize(View view,int sp){		
		DisplayMetrics displayMetrics=view.getContext().getResources().getDisplayMetrics();		
		int realSpSize=(int) (sp*displayMetrics.widthPixels/displayMetrics.density/360);
		if (view instanceof TextView) {
			((TextView)view).setTextSize(TypedValue.COMPLEX_UNIT_SP, realSpSize);
		} else if (view instanceof Button) {
			((Button)view).setTextSize(TypedValue.COMPLEX_UNIT_SP, realSpSize);
		} else if (view instanceof EditText) {
			((EditText)view).setTextSize(TypedValue.COMPLEX_UNIT_SP, realSpSize);
		}else{
			((TextView)view).setTextSize(TypedValue.COMPLEX_UNIT_SP, realSpSize);
		}	
	}
	/**取得換算的文字大小單位pixel*/
	public static int getTextSize(Context context , int sp){
		DisplayMetrics displayMetrics=context.getResources().getDisplayMetrics();		
		int realSpSize=(int) (sp*displayMetrics.widthPixels/displayMetrics.density/360);
		return realSpSize;
	}
	/**
	* <p>輸入DP單位數值 根據裝置動態 回傳像素: </p>
	* @author Robert Chou didi31139@gmail.com
	* @param dpSize 整數 單位為dp
	* @date 2015/6/17 下午5:25:39
	* @return dp根據裝置動態計算 回傳pixel
	* @version 
	*/
	public static int getPixelFromDpByDevice(Context context,int dpSize){		
		int pixel = 0;
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		int realSpSize = (int) (dpSize * displayMetrics.widthPixels
				/ displayMetrics.density / 360);
		pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				realSpSize, context.getResources().getDisplayMetrics());
		return pixel;
	}
	/**
	 * 設定 壓下的圖片切換效果
	 * @param unPressedDrawable 未按下的圖片 R.drawable.image
	 * @param pressedDrawable 未按下的圖片 R.drawable.pressedimage 
	 * */		
	public static void setPressedImage(View view,Drawable unPressedDrawable,Drawable pressedDrawable){			
		if (pressedDrawable==null) {
			setBackground(view, unPressedDrawable);
			return;
		}				
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] {android.R.attr.state_pressed}, pressedDrawable);
		states.addState(new int[] {android.R.attr.state_focused}, pressedDrawable);
		states.addState(new int[] {android.R.attr.state_checked}, pressedDrawable);
		states.addState(new int[] {}, unPressedDrawable);				
		if (view instanceof Button) {
			setBackground(view, states);
		}else{
			((ImageView)view).setImageDrawable(states);
		}
					
	}
	/**
	 * 設定 壓下的圖片切換效果
	 * @param unPressedDrawableID 未按下的圖片 R.drawable.image
	 * @param pressedDrawableID 未按下的圖片 R.drawable.pressedimage 
	 * */		
	public static void setPressedImage(View view,int unPressedDrawableID,int pressedDrawableID){
		setPressedImage(view, view.getContext().getResources().getDrawable(unPressedDrawableID), view.getContext().getResources().getDrawable(pressedDrawableID));				
	}
	
	/**
	 * 設定 壓下的圖片切換效果
	 * @param unPressedDrawable 未按下的圖片 R.drawable.image
	 * @param pressedDrawable 未按下的圖片 R.drawable.pressedimage 
	 * */
	public static void setPressedBackground(View view,Drawable unPressedDrawable,Drawable pressedDrawable){		
		if (pressedDrawable==null) {
			setBackground(view, unPressedDrawable);
			return;
		}
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] {android.R.attr.state_pressed}, pressedDrawable);
		states.addState(new int[] {android.R.attr.state_focused}, pressedDrawable);
		states.addState(new int[] {android.R.attr.state_checked}, pressedDrawable);
		states.addState(new int[] {}, unPressedDrawable);				
		setBackground(view, states);
	}
	/**
	 * 設定 壓下的圖片切換效果
	 * @param unPressedDrawable 未按下的圖片 R.drawable.image
	 * @param pressedDrawable 未按下的圖片 R.drawable.pressedimage 
	 * */
	public static void setPressedBackground(View view,int unPressedDrawable,int pressedDrawable){
		setPressedBackground(view, view.getContext().getResources().getDrawable(unPressedDrawable), view.getContext().getResources().getDrawable(pressedDrawable));
	}
	
	/**
	 * check box 狀態設定
	 * @param basedrawable 未按下的圖片 R.drawable.image
	 * @param presseddrawable 未按下的圖片 R.drawable.pressedimage 0為不給
	 * */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setCheckDrawable(Context context,View view,int basedrawable,int checkeddrawable){
		if (checkeddrawable==0) {
			view.setBackgroundResource(basedrawable);
			return;
		}
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] {android.R.attr.state_checkable}, context.getResources().getDrawable(basedrawable));
		states.addState(new int[] {android.R.attr.state_checked}, context.getResources().getDrawable(checkeddrawable));
		states.addState(new int[] {}, context.getResources().getDrawable(basedrawable));
		
		if (view instanceof CheckBox) {
			((CheckBox) view).setButtonDrawable(states);
		}else if (VERSION.SDK_INT>=16) {
			view.setBackground(states);
		}else{
			view.setBackgroundDrawable(states);
		}			
	}
	
	/**
	 * 設定按鈕 被按住的顏色背景
	 * @param unPressedColor 未按下的顏色背景 R.color.color1
	 * @param pressedColor 按下的顏色 R.color.color 0為不給
	 * */	
	public static void setPressedBackgroundColor(View view,int unPressedColor,int pressedColor){
		Context context=view.getContext();
		if (pressedColor==0) {			
			view.setBackgroundResource(context.getResources().getColor(unPressedColor));
			return;
		}
		ColorDrawable unPressedcolorDrawable=new ColorDrawable(context.getResources().getColor(unPressedColor));
		ColorDrawable pressedcolorDrawable=new ColorDrawable(context.getResources().getColor(pressedColor));
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] {android.R.attr.state_pressed}, pressedcolorDrawable);
		states.addState(new int[] {android.R.attr.state_focused}, pressedcolorDrawable);
		states.addState(new int[] {android.R.attr.state_checked}, pressedcolorDrawable);
		states.addState(new int[] {},unPressedcolorDrawable);
		setBackground(view, states);		
	}
	
	
	/**
	 * 設定按鈕 被按住的顏色背景
	 * @param unPressedColor 未按下的顏色背景 R.color.color1
	 * @param pressedColor 按下的顏色 R.color.color 0為不給
	 * */	
	public static void setPressedTextColor(View view,int unPressedColor,int pressedColor){
		Context context=view.getContext();
		if (pressedColor==0) {
			((TextView)view).setTextColor(context.getResources().getColor(unPressedColor));	
			return;
		}		
		ColorStateList colorStateList=new ColorStateList(new int[][]{				
                new int[]{android.R.attr.state_pressed}, 
                new int[]{android.R.attr.state_focused}, 
                new int[]{android.R.attr.state_checked},
                new int[]{}
        }, new int[]{
               context.getResources().getColor(pressedColor),
               context.getResources().getColor(pressedColor),
               context.getResources().getColor(pressedColor),
               context.getResources().getColor(unPressedColor)
                });		
		((TextView)view).setTextColor(colorStateList);							
	}
	
	public static void setTextView(TextView textView,int width,int height,int sp,int stringid,int unPressedColor,int pressedColor){
		UserInterfaceTool.setViewSize(textView, width, height);
		UserInterfaceTool.setTextSize(textView, sp);
		textView.setText(textView.getContext().getString(stringid));
		UserInterfaceTool.setPressedTextColor(textView, unPressedColor, pressedColor);		
	}
	public static void setTextView(TextView textView,int width,int height,int sp,String string,int unPressedColor,int pressedColor){
		UserInterfaceTool.setViewSize(textView, width, height);
		UserInterfaceTool.setTextSize(textView, sp);
		textView.setText(string);
		UserInterfaceTool.setPressedTextColor(textView, unPressedColor, pressedColor);		
	}
}
