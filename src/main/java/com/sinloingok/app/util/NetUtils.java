package com.sinloingok.app.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * 网络相关方法
 * @author kaptenkabu
 * 所属权归 Sinloingok
 *
 */
public class NetUtils {

	/**
	 * 获得访问IP
	 * @param request
	 * @return
	 */
	public static String getRequestIP(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * HTTP GET 方式请求
	 * @return
	 */
	public static String http_get(String url) throws Exception{
		String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
		StringBuffer sbf = new StringBuffer();
		HttpURLConnection urlcon = (HttpURLConnection) new URL(url).openConnection();
		urlcon.setRequestMethod("GET");
		urlcon.setReadTimeout(30000);
		urlcon.setConnectTimeout(30000);
		urlcon.setRequestProperty("User-agent", userAgent);
		urlcon.connect();
		InputStream is = urlcon.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
		String strRead = null;
		while ((strRead = reader.readLine()) != null) {
			sbf.append(strRead);
			sbf.append("\r\n");
		}
		reader.close();
		return sbf.toString();
	}
	
	/**
	 * URL参数拼接方法
	 * @param url
	 * @param params
	 * @return
	 */
	public static String getUrl(String url,Map<String,String> params){
		StringBuilder sb = new StringBuilder(url).append("?");
		Set<Entry<String,String>> entries = params.entrySet();
		for(Entry<String,String> e : entries){
			sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
		}
		return sb.substring(0,sb.length()-1);
	}
	
	/**
	 * http get请求
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String http_get(String url,Map<String,String> params) throws Exception{
		String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
		StringBuffer sbf = new StringBuffer();
		HttpURLConnection urlcon = (HttpURLConnection) new URL(getUrl(url,params)).openConnection();
		urlcon.setRequestMethod("GET");
		urlcon.setReadTimeout(30000);
		urlcon.setConnectTimeout(30000);
		urlcon.setRequestProperty("User-agent", userAgent);
		urlcon.connect();
		InputStream is = urlcon.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
		String strRead = null;
		while ((strRead = reader.readLine()) != null) {
			sbf.append(strRead);
			sbf.append("\r\n");
		}
		reader.close();
		return sbf.toString();
	}
	
	/**
	 * POST请求
	 * @param url 基础URL
	 * @param params 参数map
	 * @param noKeyParam 无key参数
	 * @return
	 * @throws Exception
	 */
	public static String http_post(String url,Map<String,Object> params, String noKeyParam) throws Exception {
		String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
		StringBuffer sbf = new StringBuffer();
		HttpURLConnection urlcon = (HttpURLConnection) new URL(url).openConnection();
		urlcon.setRequestMethod("POST");
		urlcon.setReadTimeout(30000);
		urlcon.setConnectTimeout(30000);
		urlcon.setRequestProperty("User-agent", userAgent);
		for(String key : params.keySet()){
			urlcon.setRequestProperty(key, params.get(key)+"");
		}
		if(StringUtils.isNotEmpty(noKeyParam)){
			urlcon.setDoOutput(true);
	        urlcon.getOutputStream().write(noKeyParam.getBytes("UTF-8"));
		}
        urlcon.connect();
		InputStream is = urlcon.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
		String strRead = null;
		while ((strRead = reader.readLine()) != null) {
			sbf.append(strRead);
			sbf.append("\r\n");
		}
		reader.close();
		return sbf.toString();
	}
	
	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	/*
	 * post请求返回InputStream的图片数据
	 * */
	//
	public static InputStream sendPost2(String url, String param) {    
		PrintWriter out = null;
		InputStream inputStream=null;
		//请求数据，自行拼接
//		String param="{ \"scene\":\"19014\" ,\"page\":\"pages/mine/pages/regiest/regiest\",\"width\":430}";
        try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
//			conn.setRequestProperty("accept", "*/*");
//			conn.setRequestProperty("connection", "Keep-Alive");
//			conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//			conn.setCharacterEncoding("gbk");
			conn.setRequestProperty("Content-Type", "application/json;charset-gbk");
			conn.setRequestProperty("responseType", "arraybuffer");
			
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();     
			//获取流数据
			inputStream = conn.getInputStream();
			
			// 将获取流转为base64格式
//			byte[] data = null;
//			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
//			byte[] buff = new byte[100];
//			int rc = 0;
//			while ((rc = inputStream.read(buff, 0, 100)) > 0) {
//				swapStream.write(buff, 0, rc);
//			}
//			data = swapStream.toByteArray();
			
//			result = new String(Base64.getEncoder().encode(data));
//			当import java.util.Base64;无法导入时，只能在网上找找其他的jar包，写法换成下面这种
//			result = new String(Base64.encodeBase64(data));
			
			
        }catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！"+e);
			e.printStackTrace();
		}
			//使用finally块来关闭输出流、输入流
		finally{
			if(out!=null){
				out.close();
			}
//				if(inputStream!=null){
//					inputStream.close();
//				}
		}
        return inputStream;
	}
	
	/*
	 * post请求返回base64的图片数据
	 * */
	//
	public static String sendPost3(String url, String param) {    
		PrintWriter out = null;
		String result = "";
		InputStream inputStream=null;
		//请求数据，自行拼接
//		String param="{ \"scene\":\"19014\" ,\"page\":\"pages/mine/pages/regiest/regiest\",\"width\":430}";
        try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
//			conn.setRequestProperty("accept", "*/*");
//			conn.setRequestProperty("connection", "Keep-Alive");
//			conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//			conn.setCharacterEncoding("gbk");
			conn.setRequestProperty("Content-Type", "application/json;charset-gbk");
			conn.setRequestProperty("responseType", "arraybuffer");
			
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();     
			//获取流数据
			inputStream = conn.getInputStream();
			
			// 将获取流转为base64格式
			byte[] data = null;
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			byte[] buff = new byte[100];
			int rc = 0;
			while ((rc = inputStream.read(buff, 0, 100)) > 0) {
				swapStream.write(buff, 0, rc);
			}
			data = swapStream.toByteArray();
			
//			result = new String(Base64.getEncoder().encode(data));
//			当import java.util.Base64;无法导入时，只能在网上找找其他的jar包，写法换成下面这种
			result = new String(Base64.encodeBase64(data));
			
			
        }catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！"+e);
			e.printStackTrace();
		}
			//使用finally块来关闭输出流、输入流
		finally{
			try{
				if(out!=null){
					out.close();
				}
				if(inputStream!=null){
					inputStream.close();
				}
				
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
        return result;
	}
}


































