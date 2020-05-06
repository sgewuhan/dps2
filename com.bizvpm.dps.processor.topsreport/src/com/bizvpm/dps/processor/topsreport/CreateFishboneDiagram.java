package com.bizvpm.dps.processor.topsreport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;


public class CreateFishboneDiagram implements IProcessorRunable  {

	private static final String DRAW_LINE_X1="drawLinex1";//获取线的第一个点的x轴
	private static final String DRAW_LINE_X2="drawLinex2";//获取线的第二个点的x轴
	private static final String DRAW_LINE_Y1="drawLiney1";//获取线的第一个点的Y轴
	private static final String DRAW_LINE_Y2="drawLiney2";//获取线的第二个点的Y轴
	private static final String DRAW_LINE_LEN="drawLineLen";//线长
	
	private static final String DRAW_TRIANGLE_X="drawtrianglex";//获取三角形箭头起始点的x轴
	private static final String DRAW_TRIANGLE_Y="drawtriangley";//获取三角形箭头起始点的Y轴
	private static final String DRAW_TRIANGLE_TYPE="drawtriangleType";//三角形箭头所对应的方向
	private static final String DRAW_TRIANGLE_SIZI="drawtriangleTsizi";//三角形箭头大小

	private static final String DRAW_STRING_X="drawstringx";//获取文字的第一个点的x轴
	private static final String DRAW_STRING_Y="drawstringy";//获取线文字第一个点的Y轴
	
	private static final Integer DRAW_FONT_SIZI=12;//字体大小

	@Override
	@SuppressWarnings("unchecked")
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		ProcessResult r = new ProcessResult();
		File file=new File("鱼骨图.jpg");
		int width = 600;
		int height = 400;

		//获取数据源
		Map<String,Object> map=(Map<String,Object>)processTask.get("data");
		String name=(String) processTask.get("name");
		
		//计算图片长宽
		for (String key : map.keySet()) {
			if(isRecursion(map.get(key))){
				height=height+((Map<String,Object>)map.get(key)).size()*50;
			}
		}
		width=width+(map.size()*250);
		
		BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		//创建画笔
		Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(bufferedImage.getScaledInstance(bufferedImage.getWidth(null), bufferedImage.getHeight(null), Image.SCALE_SMOOTH), 0, 0, null);
		//将背景涂成白色
		g2.setColor(Color.WHITE);
		g2.fillRect(0,0,width,height);
		
        Stroke stroke = new BasicStroke(2.0f,   // 线宽
                BasicStroke.CAP_SQUARE,   		// 端点样式
                BasicStroke.JOIN_BEVEL,  		// 接头样式
                15.0f,       					// 拼接限制
                null,             				// 虚线
                5.0f);      					// 虚线的设置
        g2.setStroke(stroke);
        //画鱼骨图
		getMainBone(height,width,name,g2,map);
		try {
			ImageIO.write(bufferedImage, "jpg", file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    r.putByteArray("file", file);
	    file.delete();
		return r;
	}
	
	/**
	 * 	绘制鱼骨图主干骨
	 * @param height
	 * @param width
	 * @param name
	 * @param g2
	 * @param map
	 */
	private void getMainBone(int height,int width,String name,Graphics2D g2,Map<String,Object> map){

		int x1=300;
		int y1=height/2;
		int x2=width-(100+(name.length()*10));
		int y2=height/2;
		int len=(height/2-100);
       
		g2.setColor(new Color(0, 0, 0));
		g2.drawLine(x1,y1,x2, y2);

        int[] xPoints1 = {width-(100+(name.length()*10)),width-(100+(name.length()*10)),width-(75+(name.length()*10))};  
        int[] yPoints1 = {height/2-25,height/2+25,height/2};  
        g2.drawPolygon(xPoints1, yPoints1, 3);
        //设置文字内容、位置  
        g2.drawString(name,width-(100+(name.length()*10)-50),height/2+5);
        getBigBone(x1,y1,x2,y2,len,g2,map);
	}
	
	/**
	 * 	绘制鱼骨图大骨
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param len
	 * @param g2
	 * @param map
	 */
	private void  getBigBone(int x1,int y1,int x2,int y2,int len,Graphics2D g2,Map<String,Object> map){
		 int i =1;
	        int x=x2-x1!=0?(x2-x1)/(map.size()+1):0;
	        int y=y2-y1!=0?(y2-y1)/(map.size()+1):0;

	        Map<String, Integer> parameter=new HashMap<String, Integer>();
	        parameter.put(DRAW_LINE_LEN, (x/7)*5);
	        parameter.put(DRAW_TRIANGLE_SIZI, 10);
	        
	        for (String key : map.keySet()) {
	        	parameter.put(DRAW_LINE_X1, x2-(x*i)-len);
	            parameter.put(DRAW_LINE_X2, x2-(x*i));
	            parameter.put(DRAW_LINE_Y2, y2-(y*i));
	            parameter.put(DRAW_TRIANGLE_X, x2-(x*i));
	            parameter.put(DRAW_TRIANGLE_Y, y2-(y*i));
	            if(key.length()>16){
	        		parameter.put(DRAW_STRING_X, x2-(x*i)-len-10-(8*DRAW_FONT_SIZI));
				}else{
	        		parameter.put(DRAW_STRING_X, x2-(x*i)-len-10-(key.length()/2*DRAW_FONT_SIZI));
				}
	        	if(i%2==0){
	                parameter.put(DRAW_LINE_Y1,	y2-(y*i)-len);
	                parameter.put(DRAW_TRIANGLE_TYPE, 5);
	        		parameter.put(DRAW_STRING_Y, y2-(y*i)-len-17);
	        	}else{
	                parameter.put(DRAW_LINE_Y1,	y2-(y*i)+len);
	                parameter.put(DRAW_TRIANGLE_TYPE, 8);
	        		parameter.put(DRAW_STRING_Y, y2-(y*i)+len+3);
	        		
	        	}
	        	newLine(parameter,key,map.get(key),g2);
				i++;	
			}
	}
	
	/**
	 * 	绘制鱼骨图 小骨
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param g2
	 * @param list
	 */
	private void getFishBone(int x1,int y1,int x2,int y2,int len,Graphics2D g2,Map<String,Object> map){
		int i =1;
        int x=x2-x1!=0?(x2-x1)/(map.size()+1):0; //获取x轴每根线之间的间距
        int y=y2-y1!=0?((y2-y1)/(map.size()+1)):0; //获取y轴每根线之间的间距
       
        Map<String, Integer> parameter=new HashMap<String, Integer>();

        parameter.put(DRAW_LINE_LEN, (x/7)*5);
        parameter.put(DRAW_TRIANGLE_SIZI, 10);
        // 根据线方向不同,x 与y轴获取方式也不同
        if(x2>x1&&y2==y1){//表示横向直线 右往左     例: <------
        	for (String key : map.keySet()) {
                parameter.put(DRAW_LINE_X1, x1+(x*i));
                parameter.put(DRAW_LINE_Y1,	y1);
                parameter.put(DRAW_LINE_X2, x1+(x*i)+len);
                parameter.put(DRAW_TRIANGLE_X, x1+(x*i));
                parameter.put(DRAW_TRIANGLE_Y, y1);
            	if(i%2==0){
                    parameter.put(DRAW_LINE_Y2, y1+len);
                    parameter.put(DRAW_TRIANGLE_TYPE, 7);
            		parameter.put(DRAW_STRING_X, x1+(x*i)+len+2);
            		parameter.put(DRAW_STRING_Y, y1+len+10);
            	}else{
                    parameter.put(DRAW_LINE_Y2, y1-len);
                    parameter.put(DRAW_TRIANGLE_TYPE, 6);
            		parameter.put(DRAW_STRING_X, x1+(x*i)+len+2);
            		parameter.put(DRAW_STRING_Y, y1-len-2);
            	}
            	newLine(parameter,key,map.get(key),g2);
    			i++;	
    		}
        }if(x2<x1&&y2==y1){//表示横向直线  左往右   例: ----->
          	for (String key : map.keySet()) {
                parameter.put(DRAW_LINE_X1, x2-(x*i));
                parameter.put(DRAW_LINE_Y1,	y2);
                parameter.put(DRAW_LINE_X2, x2-(x*i)-len);
                parameter.put(DRAW_TRIANGLE_X, x2-(x*i));
                parameter.put(DRAW_TRIANGLE_Y, y2);
            	if(i%2!=0){
                    parameter.put(DRAW_LINE_Y2, y2+len);
                    parameter.put(DRAW_TRIANGLE_TYPE, 8);

                    if(key.length()>16){
        				parameter.put(DRAW_STRING_X,x2-(x*i)-len-5-(8*DRAW_FONT_SIZI));
                		parameter.put(DRAW_STRING_Y, y2+len+15);
        			}else{
        				parameter.put(DRAW_STRING_X,x2-(x*i)-len-5-(key.length()/2*DRAW_FONT_SIZI));
                		parameter.put(DRAW_STRING_Y, y2+len+3);
        			}
            	}else{
                    parameter.put(DRAW_LINE_Y2, y2-len);
                    parameter.put(DRAW_TRIANGLE_TYPE, 5);
                    if(key.length()>16){
        				parameter.put(DRAW_STRING_X,x2-(x*i)-len-5-(8*DRAW_FONT_SIZI));
        			}else{
        				parameter.put(DRAW_STRING_X,x2-(x*i)-len-5-(key.length()/2*DRAW_FONT_SIZI));
        			}
            		parameter.put(DRAW_STRING_Y, y2-len-17);
            	}	
        		newLine(parameter,key,map.get(key),g2);
    			i++;	
    		}
        }else if(y2!=y1&&x2!=x1){//斜线
        	for (String key : map.keySet()) {
                parameter.put(DRAW_LINE_X1, x2-(x*i));
                parameter.put(DRAW_LINE_Y1,	y2-(y*i));
                parameter.put(DRAW_LINE_Y2, y2-(y*i));
                parameter.put(DRAW_TRIANGLE_X, x2-(x*i));
                parameter.put(DRAW_TRIANGLE_Y, y2-(y*i));
            	if(i%2==0){
                    parameter.put(DRAW_LINE_X2, x2-(x*i)-len);
                    parameter.put(DRAW_TRIANGLE_TYPE, 1);
        			if(key.length()>16){
        				parameter.put(DRAW_STRING_X,x2-(x*i)-len-(16*DRAW_FONT_SIZI));
        			}else{
        				parameter.put(DRAW_STRING_X,x2-(x*i)-len-(key.length()*DRAW_FONT_SIZI));
        			}
            		parameter.put(DRAW_STRING_Y, y2-(y*i)-7);
            	}else{
                    parameter.put(DRAW_LINE_X2, x2-(x*i)+len);
                    parameter.put(DRAW_TRIANGLE_TYPE, 3);
            		parameter.put(DRAW_STRING_X,x2-(x*i)+len+5);
            		parameter.put(DRAW_STRING_Y,y2-(y*i)-7);
            	}
            	newLine(parameter,key,map.get(key),g2);
    			i++;	
    		}
        }
	}
	
	@SuppressWarnings("unchecked")
	private void newLine(Map<String, Integer> parameter,String value,Object data,Graphics2D g2){
    	getDrawString(value,parameter.get(DRAW_STRING_X),parameter.get(DRAW_STRING_Y),g2);			//显示字
    	getDrawLine(parameter.get(DRAW_LINE_X1),parameter.get(DRAW_LINE_Y1),parameter.get(DRAW_LINE_X2),parameter.get(DRAW_LINE_Y2),g2);
		getTriangle(parameter.get(DRAW_TRIANGLE_X),parameter.get(DRAW_TRIANGLE_Y),parameter.get(DRAW_TRIANGLE_TYPE),parameter.get(DRAW_TRIANGLE_SIZI),g2);
		if(isRecursion(data)){
    		getFishBone(parameter.get(DRAW_LINE_X1),parameter.get(DRAW_LINE_Y1),parameter.get(DRAW_LINE_X2),parameter.get(DRAW_LINE_Y2),parameter.get(DRAW_LINE_LEN),g2,(Map<String,Object>)data);
    	}
	}
	
	private boolean isRecursion(Object object){
		if(object instanceof Map)
			return true;
		return false;
	}
	
	private void getDrawLine(int x1,int y1,int x2,int y2,Graphics2D g2){
		g2.drawLine(x1,y1,x2, y2);
	}
	
	private void getDrawString(String val,int x,int y,Graphics2D g2){
		if(val.length()>32){
			val=val.substring(0,32);
		}
		if(val.length()>16){
			g2.drawString(val.substring(0,16),x,y);
			g2.drawString(val.substring(16),x,y+14);
		}else{
			g2.drawString(val,x,y+DRAW_FONT_SIZI);
		}
	}
	
	/**
	 *    通过一个点获取一个三角形箭头
	 * @param x
	 * @param y
	 * @param type  三角形箭头朝向方向
	 * @param sizi  三角形箭头大小
	 * @param g2	画笔
	 */
	private void getTriangle(int x,int y,int type,int sizi,Graphics2D g2){
		GeneralPath triangle = new GeneralPath();
		int x1=0,y1=0,x2=0,y2=0;
		
		if(type==1){//右
			x1=x-sizi*2;
			y1=y-sizi/2;
			x2=x-sizi*2;
			y2=y+sizi/2;
		}else if(type==2){//下
			x1=x-sizi/2;
			y1=y-sizi*2;
			x2=x+sizi/2;
			y2=y-sizi*2;
		}else if(type==3){//左
			x1=x+sizi*2;
			y1=y-sizi/2;
			x2=x+sizi*2;
			y2=y+sizi/2;
		}else if(type==4){//上
			x1=x-sizi/2;
			y1=y+sizi*2;
			x2=x+sizi/2;
			y2=y+sizi*2;
		}else if(type==5){//右下
			x1=x-(sizi+sizi/2);
			y1=y-(sizi-sizi/2);
			x2=x-(sizi-sizi/2);
			y2=y-(sizi+sizi/2);
		}else if(type==6){//左下
			x1=x+(sizi-sizi/2);
			y1=y-(sizi+sizi/2);
			x2=x+(sizi+sizi/2);
			y2=y-(sizi-sizi/2);
		}else if(type==7){//左上
			x1=x+(sizi-sizi/2);
			y1=y+(sizi+sizi/2);
			x2=x+(sizi+sizi/2);
			y2=y+(sizi-sizi/2);
		}else if(type==8){//右下
			x1=x-(sizi+sizi/2);
			y1=y+(sizi-sizi/2);
			x2=x-(sizi-sizi/2);
			y2=y+(sizi+sizi/2);
		}

		triangle.moveTo(x, y);
		triangle.lineTo(x1, y1);
		triangle.lineTo(x2, y2);
		triangle.closePath();
		g2.fill(triangle);
	}



}
