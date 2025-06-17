package com.sinloingok.app.util.net;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DouYin {
	private static final String BOUNDARY_PREFIX = "--";
	private static final String LINE_END = "\r\n";

	public static void main(String[] args) throws IOException {
		// 请求 url
		String open_id = "8887b30f-25d4-496b-83c3-1bb13458e88a";
		String access_token = "act.8f286882bb2dd11a397946fddce0616cWpzLmusgJhw4b7hyP62kSKeOw6gB";
		String url = "https://open.douyin.com/video/upload/?open_id=" + open_id + "&access_token=" + access_token;

		// keyValues 保存普通参数
		Map<String, Object> keyValues = new HashMap<>();
//		keyValues.put("open_id", open_id);
//		keyValues.put("access_token", access_token);

		// filePathMap 保存文件类型的参数名和文件路径
		Map<String, String> filePathMap = new HashMap<>();
		String paramName = "video";
		String filePath = "D:\\123456.mp4";
		filePathMap.put(paramName, filePath);

		// headers
		Map<String, Object> headers = new HashMap<>();
		// COOKIE: Name=Value;Name2=Value2
		headers.put("COOKIE",
				"token=OUFFNzQ0OUU5RDc1ODM0Q0M3QUM5NzdENThEN0Q1NkVEMjhGNzJGNEVGRTNCN0JEODM5NzAyNkI0OEE0MDcxNUZCMjdGN1MxMzdGRUE4MTcwRjVDNkJBRTE2ODgzQURDRjNCQjdBMTdCODc0MzA4QzFFRjlBQkM1MTA0N0MzMUU=");

		HttpResponse response = postFormData(url, filePathMap, keyValues, headers);
		System.out.println(response);

	}

	public static HttpResponse postFormData(String urlStr, Map<String, String> filePathMap,
			Map<String, Object> keyValues, Map<String, Object> headers) throws IOException {
		HttpResponse response;
		HttpURLConnection conn = getHttpURLConnection(urlStr, headers);
		// 分隔符，可以任意设置，这里设置为 MyBoundary+ 时间戳（尽量复杂点，避免和正文重复）
		String boundary = "MyBoundary" + System.currentTimeMillis();
		// 设置 Content-Type 为 multipart/form-data; boundary=${boundary}
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		// 发送参数数据
		try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
			// 发送普通参数
			if (keyValues != null && !keyValues.isEmpty()) {
				for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
					writeSimpleFormField(boundary, out, entry);
				}
			}
			// 发送文件类型参数
			if (filePathMap != null && !filePathMap.isEmpty()) {
				for (Map.Entry<String, String> filePath : filePathMap.entrySet()) {
					writeFile(filePath.getKey(), filePath.getValue(), boundary, out);
				}
			}

			// 写结尾的分隔符--${boundary}--,然后回车换行
			String endStr = BOUNDARY_PREFIX + boundary + BOUNDARY_PREFIX + LINE_END;
			out.write(endStr.getBytes());
		} catch (Exception e) {
			response = new HttpResponse(500, e.getMessage());
			return response;
		}

		return getHttpResponse(conn);
	}

	/**
	 * 获得连接对象
	 *
	 * @param urlStr
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	private static HttpURLConnection getHttpURLConnection(String urlStr, Map<String, Object> headers)
			throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// 设置超时时间
		conn.setConnectTimeout(50000);
		conn.setReadTimeout(50000);
		// 允许输入流
		conn.setDoInput(true);
		// 允许输出流
		conn.setDoOutput(true);
		// 不允许使用缓存
		conn.setUseCaches(false);
		// 请求方式
		conn.setRequestMethod("POST");
		// 设置编码 utf-8
		conn.setRequestProperty("Charset", "UTF-8");
		// 设置为长连接
		conn.setRequestProperty("connection", "keep-alive");

		// 设置其他自定义 headers
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, Object> header : headers.entrySet()) {
				conn.setRequestProperty(header.getKey(), header.getValue().toString());
			}
		}

		return conn;
	}

	private static HttpResponse getHttpResponse(HttpURLConnection conn) {
		HttpResponse response;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			int responseCode = conn.getResponseCode();
			StringBuilder responseContent = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				responseContent.append(line);
			}
			response = new HttpResponse(responseCode, responseContent.toString());
		} catch (Exception e) {
			System.err.println("获取 HTTP 响应异常！");
			System.err.println(e);
			response = new HttpResponse(500, e.getMessage());
		}
		return response;
	}

	/**
	 * 写文件类型的表单参数
	 *
	 * @param paramName 参数名
	 * @param filePath  文件路径
	 * @param boundary  分隔符
	 * @param out
	 * @throws IOException
	 */
	private static void writeFile(String paramName, String filePath, String boundary, DataOutputStream out) {
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
			/**
			 * 写分隔符--${boundary}，并回车换行
			 */
			String boundaryStr = BOUNDARY_PREFIX + boundary + LINE_END;
			out.write(boundaryStr.getBytes());
			/**
			 * 写描述信息(文件名设置为上传文件的文件名)： 写 Content-Disposition: form-data; name="参数名";
			 * filename="文件名"，并回车换行 写 Content-Type: application/octet-stream，并两个回车换行
			 */
			String fileName = new File(filePath).getName();
			String contentDispositionStr = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"",
					paramName, fileName) + LINE_END;
			out.write(contentDispositionStr.getBytes());
			String contentType = "Content-Type: application/octet-stream" + LINE_END + LINE_END;
			out.write(contentType.getBytes());

			String line;
			while ((line = fileReader.readLine()) != null) {
				out.write(line.getBytes());
			}
			// 回车换行
			out.write(LINE_END.getBytes());
		} catch (Exception e) {
			System.err.println("写文件类型的表单参数异常");
			System.err.println(e);
		}
	}

	/**
	 * 写普通的表单参数
	 *
	 * @param boundary 分隔符
	 * @param out
	 * @param entry    参数的键值对
	 * @throws IOException
	 */
	private static void writeSimpleFormField(String boundary, DataOutputStream out, Map.Entry<String, Object> entry)
			throws IOException {
		// 写分隔符--${boundary}，并回车换行
		String boundaryStr = BOUNDARY_PREFIX + boundary + LINE_END;
		out.write(boundaryStr.getBytes());
		// 写描述信息：Content-Disposition: form-data; name="参数名"，并两个回车换行
		String contentDispositionStr = String.format("Content-Disposition: form-data; name=\"%s\"", entry.getKey())
				+ LINE_END + LINE_END;
		out.write(contentDispositionStr.getBytes());
		// 写具体内容：参数值，并回车换行
		String valueStr = entry.getValue().toString() + LINE_END;
		out.write(valueStr.getBytes());
	}

	/**
	 * 发送文本内容
	 *
	 * @param urlStr
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static HttpResponse postText(String urlStr, String filePath) throws IOException {
		HttpResponse response;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "text/plain");
		conn.setDoOutput(true);
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
				BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
			String line;
			while ((line = fileReader.readLine()) != null) {
				writer.write(line);
			}

		} catch (Exception e) {
			System.err.println("HttpUtils.postText 请求异常！");
			System.err.println(e);
			response = new HttpResponse(500, e.getMessage());
			return response;
		}

		return getHttpResponse(conn);
	}
}
