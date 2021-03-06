package com.qingyou.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.qingyou.qingyouclient.Log;

public class HttpUtility {

	private static HttpParameters mRequestHeader = new HttpParameters();

	public static final String BOUNDARY = "7cd4a6d158c";
	public static final String MP_BOUNDARY = "--" + BOUNDARY;
	public static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";

	public static final String HTTPMETHOD_POST = "POST";
	public static final String HTTPMETHOD_GET = "GET";
	public static final String HTTPMETHOD_DELETE = "DELETE";

	private static final int SET_CONNECTION_TIMEOUT = 50000;
	private static final int SET_SOCKET_TIMEOUT = 200000;

	private static HashMap<String, String> CookieContiner = new HashMap<String, String>();

	public static int cookieSize() {
		return CookieContiner.size();
	}

	public static boolean isBundleEmpty(HttpParameters bundle) {
		if (bundle == null || bundle.size() == 0) {
			return true;
		}
		return false;
	}

	// 填充request bundle
	public static void setRequestHeader(String key, String value) {
		// mRequestHeader.clear();
		mRequestHeader.add(key, value);
	}

	public static void setRequestHeader(HttpParameters params) {
		mRequestHeader.addAll(params);
	}

	public static void clearRequestHeader() {
		mRequestHeader.clear();

	}

	public static String encodePostBody(Bundle parameters, String boundary) {
		if (parameters == null)
			return "";
		StringBuilder sb = new StringBuilder();

		for (String key : parameters.keySet()) {
			if (parameters.getByteArray(key) != null) {
				continue;
			}

			sb.append("Content-Disposition: form-data; name=\"" + key
					+ "\"\r\n\r\n" + parameters.getString(key));
			sb.append("\r\n" + "--" + boundary + "\r\n");
		}

		return sb.toString();
	}

	public static String encodeUrl(HttpParameters parameters)
			throws UnsupportedEncodingException {
		if (parameters == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int loc = 0; loc < parameters.size(); loc++) {
			if (first)
				first = false;
			else
				sb.append("&");
			sb.append(URLEncoder.encode(parameters.getKey(loc), "UTF-8") + "="
					+ URLEncoder.encode(parameters.getValue(loc), "UTF-8"));
		}
		return sb.toString();
	}

	public static Bundle decodeUrl(String s)
			throws UnsupportedEncodingException {
		Bundle params = new Bundle();
		if (s != null) {
			String array[] = s.split("&");
			for (String parameter : array) {
				String v[] = parameter.split("=");
				params.putString(URLDecoder.decode(v[0], "UTF-8"),
						URLDecoder.decode(v[1], "UTF-8"));
			}
		}
		return params;
	}

	/**
	 * Parse a URL query and fragment parameters into a key-value bundle.
	 * 
	 * @param url
	 *            the URL to parse
	 * @return a dictionary bundle of keys and values
	 */
	public static Bundle parseUrl(String url) {
		// hack to prevent MalformedURLException
		url = url.replace("weiboconnect", "http");
		try {
			URL u = new URL(url);
			Bundle b = decodeUrl(u.getQuery());
			b.putAll(decodeUrl(u.getRef()));
			return b;
		} catch (MalformedURLException e) {
			return new Bundle();
		} catch (UnsupportedEncodingException e) {
			return new Bundle();
		}
	}

	/**
	 * Construct a url encoded entity by parameters .
	 * 
	 * @param bundle
	 *            :parameters key pairs
	 * @return UrlEncodedFormEntity: encoed entity
	 */
	public static UrlEncodedFormEntity getPostParamters(Bundle bundle)
			throws ProtocolException {
		if (bundle == null || bundle.isEmpty()) {
			return null;
		}
		try {
			List<NameValuePair> form = new ArrayList<NameValuePair>();
			for (String key : bundle.keySet()) {
				form.add(new BasicNameValuePair(key, bundle.getString(key)));
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form,
					"UTF-8");
			return entity;
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException(e);
		}
	}

	/**
	 * Implement http request and return results .
	 * 
	 * @param context
	 *            : context of activity
	 * @param url
	 *            : request url
	 * @param method
	 *            : HTTP METHOD.GET, POST, DELETE
	 * @param params
	 *            : Http params , query or postparameters
	 * @return UrlEncodedFormEntity: encoed entity
	 */

	public static String openUrl(Context context, String url, String method,
			HttpParameters params) throws ProtocolException {
		String rlt = "";
		String file = "";
		for (int loc = 0; loc < params.size(); loc++) {
			String key = params.getKey(loc);
			if (key.equals("pic")) {
				file = params.getValue(key);
				params.remove(key);
			}
		}
		if (TextUtils.isEmpty(file)) {
			rlt = openUrl(context, url, method, params, null);
		} else {
			rlt = openUrl(context, url, method, params, file);
		}
		return rlt;
	}

	static DefaultRedirectHandler redirect_handler = new DefaultRedirectHandler() {

		@Override
		public boolean isRedirectRequested(HttpResponse response,
				HttpContext context) {
			parseCookies(response);
			return super.isRedirectRequested(response, context);
		}
	};

	public static String openUrl(Context context, String url, String method,
			HttpParameters params, String file) throws ProtocolException {
		String result = "";
		try {
			System.out.println("HTTP method = " + method);
			HttpClient client = getNewHttpClient(context);
			((DefaultHttpClient) client).setRedirectHandler(redirect_handler);
			HttpUriRequest request = null;
			ByteArrayOutputStream bos = null;
			if (method.equals("GET")) {
				url = url + "?" + encodeUrl(params);
				HttpGet get = new HttpGet(url);
				request = get;
				System.out.println("url = " + url);
			} else if (method.equals("POST")) {
				HttpPost post = new HttpPost(url);
				byte[] data = null;
				bos = new ByteArrayOutputStream(1024 * 50);
				if (!TextUtils.isEmpty(file)) {
					HttpUtility.paramToUpload(bos, params);
					post.setHeader("Content-Type", MULTIPART_FORM_DATA
							+ "; boundary=" + BOUNDARY);
					Bitmap bf = BitmapFactory.decodeFile(file);

					HttpUtility.imageContentToUpload(bos, bf);

				} else {
					post.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					String postParam = encodeParameters(params);
					System.out.println(postParam);
					data = postParam.getBytes("UTF-8");
					bos.write(data);
				}
				data = bos.toByteArray();
				bos.close();
				// UrlEncodedFormEntity entity = getPostParamters(params);
				ByteArrayEntity formEntity = new ByteArrayEntity(data);
				post.setEntity(formEntity);
				request = post;
			} else if (method.equals("DELETE")) {
				request = new HttpDelete(url);
			}
			// setHeader(method, request, params, url);
			request.setHeader("User-Agent",
					System.getProperties().getProperty("http.agent"));
			if (CookieContiner.size() > 0)
				AddCookies(request);
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			int statusCode = status.getStatusCode();

			parseCookies(response);

			if (statusCode != 200) {
				result = read(response);
				String err = null;
				int errCode = 0;
				try {
					JSONObject json = new JSONObject(result);
					if (json.has("errormessage") && json.has("errorcode")) {
						err = json.getString("errormessage");
						errCode = json.getInt("errorcode");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				ProtocolException dse = new ProtocolException("errno:"
						+ errCode + " ," + err + ",statusCode:" + statusCode);
				dse.setStatus(statusCode);
				throw dse;
			}
			// parse content stream from response
			result = read(response);
			return result;
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	private static void parseCookies(HttpResponse response) {
		Header respHeaders[] = response.getAllHeaders();
		for (int i = 0; i < respHeaders.length; i++) {
			// Log.v(respHeaders[i].getName() + ":" +
			// respHeaders[i].getValue());
			if (respHeaders[i].getName().equals("Set-Cookie")) {
				String cookie = respHeaders[i].getValue();
				Log.v(cookie);
				String[] cookievalues = cookie.split(";");
				for (int j = 0; j < cookievalues.length; j++) {
					String[] keyPair = cookievalues[j].split("=");
					String key = keyPair[0].trim();
					String value = keyPair.length > 1 ? keyPair[1].trim() : "";
					CookieContiner.put(key, value);
				}
			}
		}
	}

	public static void AddCookies(HttpUriRequest request) {
		StringBuilder sb = new StringBuilder();
		Iterator iter = CookieContiner.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = entry.getKey().toString();
			String val = entry.getValue().toString();
			sb.append(key);
			if (val.length() > 0) {
				sb.append("=");
				sb.append(val);
			}
			sb.append(";");
		}

		if (sb.length() > 0) {
			request.addHeader("cookie", sb.toString());
			Log.v("Cookie: " + sb.toString());
		}
	}

	public static void ClearCookies() {
		CookieContiner.clear();
	}

	public static HttpClient getNewHttpClient(Context context) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			// Set the default socket timeout (SO_TIMEOUT) // in
			// milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setConnectionTimeout(params,
					HttpUtility.SET_CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params,
					HttpUtility.SET_SOCKET_TIMEOUT);
			HttpClient client = new DefaultHttpClient(ccm, params);
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			if (!wifiManager.isWifiEnabled()) {
				// 获取当前正在使用的APN接入点
				Uri uri = Uri.parse("content://telephony/carriers/preferapn");
				Cursor mCursor = context.getContentResolver().query(uri, null,
						null, null, null);
				if (mCursor != null && mCursor.moveToFirst()) {
					// 游标移至第一条记录，当然也只有一条
					String proxyStr = mCursor.getString(mCursor
							.getColumnIndex("proxy"));
					if (proxyStr != null && proxyStr.trim().length() > 0) {
						HttpHost proxy = new HttpHost(proxyStr, 80);
						client.getParams().setParameter(
								ConnRouteParams.DEFAULT_PROXY, proxy);
					}
					mCursor.close();
				}
			}

			return client;
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	/**
	 * Get a HttpClient object which is setting correctly .
	 * 
	 * @param context
	 *            : context of activity
	 * @return HttpClient: HttpClient object
	 */
	public static HttpClient getHttpClient(Context context) {
		BasicHttpParams httpParameters = new BasicHttpParams();
		// Set the default socket timeout (SO_TIMEOUT) // in
		// milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				HttpUtility.SET_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters,
				HttpUtility.SET_SOCKET_TIMEOUT);
		HttpClient client = new DefaultHttpClient(httpParameters);
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			// 获取当前正在使用的APN接入点
			Uri uri = Uri.parse("content://telephony/carriers/preferapn");
			Cursor mCursor = context.getContentResolver().query(uri, null,
					null, null, null);
			if (mCursor != null && mCursor.moveToFirst()) {
				// 游标移至第一条记录，当然也只有一条
				String proxyStr = mCursor.getString(mCursor
						.getColumnIndex("proxy"));
				if (proxyStr != null && proxyStr.trim().length() > 0) {
					HttpHost proxy = new HttpHost(proxyStr, 80);
					client.getParams().setParameter(
							ConnRouteParams.DEFAULT_PROXY, proxy);
				}
				mCursor.close();
			}
		}
		return client;
	}

	/**
	 * Upload image into output stream .
	 * 
	 * @param out
	 *            : output stream for uploading weibo
	 * @param imgpath
	 *            : bitmap for uploading
	 * @return void
	 */
	private static void imageContentToUpload(OutputStream out, Bitmap imgpath)
			throws ProtocolException {
		StringBuilder temp = new StringBuilder();

		temp.append(MP_BOUNDARY).append("\r\n");
		temp.append("Content-Disposition: form-data; name=\"pic\"; filename=\"")
				.append("news_image").append("\"\r\n");
		String filetype = "image/png";
		temp.append("Content-Type: ").append(filetype).append("\r\n\r\n");
		byte[] res = temp.toString().getBytes();
		BufferedInputStream bis = null;
		try {
			out.write(res);
			imgpath.compress(CompressFormat.PNG, 75, out);
			out.write("\r\n".getBytes());
			out.write(("\r\n" + END_MP_BOUNDARY).getBytes());
		} catch (IOException e) {
			throw new ProtocolException(e);
		} finally {
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
					throw new ProtocolException(e);
				}
			}
		}
	}

	/**
	 * Upload weibo contents into output stream .
	 * 
	 * @param baos
	 *            : output stream for uploading weibo
	 * @param params
	 *            : post parameters for uploading
	 * @return void
	 */
	private static void paramToUpload(OutputStream baos, HttpParameters params)
			throws ProtocolException {
		String key = "";
		for (int loc = 0; loc < params.size(); loc++) {
			key = params.getKey(loc);
			StringBuilder temp = new StringBuilder(10);
			temp.setLength(0);
			temp.append(MP_BOUNDARY).append("\r\n");
			temp.append("content-disposition: form-data; name=\"").append(key)
					.append("\"\r\n\r\n");
			temp.append(params.getValue(key)).append("\r\n");
			byte[] res = temp.toString().getBytes();
			try {
				baos.write(res);
			} catch (IOException e) {
				throw new ProtocolException(e);
			}
		}
	}

	/**
	 * Read http requests result from response .
	 * 
	 * @param response
	 *            : http response by executing httpclient
	 * 
	 * @return String : http response content
	 */
	private static String read(HttpResponse response) throws ProtocolException {
		String result = "";
		HttpEntity entity = response.getEntity();
		InputStream inputStream;
		try {
			inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			Header header = response.getFirstHeader("Content-Encoding");
			if (header != null
					&& header.getValue().toLowerCase().indexOf("gzip") > -1) {
				inputStream = new GZIPInputStream(inputStream);
			}

			// Read response into a buffered stream
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			// Return result from buffered stream
			result = new String(content.toByteArray());
			return result;
		} catch (IllegalStateException e) {
			throw new ProtocolException(e);
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	/**
	 * Read http requests result from inputstream .
	 * 
	 * @param inputstream
	 *            : http inputstream from HttpConnection
	 * 
	 * @return String : http response content
	 */
	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	/**
	 * Clear current context cookies .
	 * 
	 * @param context
	 *            : current activity context.
	 * 
	 * @return void
	 */
	public static void clearCookies(Context context) {
		@SuppressWarnings("unused")
		CookieSyncManager cookieSyncMngr = CookieSyncManager
				.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
	}

	/**
	 * Display a simple alert dialog with the given text and title.
	 * 
	 * @param context
	 *            Android context in which the dialog should be displayed
	 * @param title
	 *            Alert dialog title
	 * @param text
	 *            Alert dialog message
	 */
	public static void showAlert(Context context, String title, String text) {
		Builder alertBuilder = new Builder(context);
		alertBuilder.setTitle(title);
		alertBuilder.setMessage(text);
		alertBuilder.create().show();
	}

	public static String encodeParameters(HttpParameters httpParams) {
		if (null == httpParams || HttpUtility.isBundleEmpty(httpParams)) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		int j = 0;
		for (int loc = 0; loc < httpParams.size(); loc++) {
			String key = httpParams.getKey(loc);
			if (j != 0) {
				buf.append("&");
			}
			try {
				buf.append(URLEncoder.encode(key, "UTF-8"))
						.append("=")
						// .append(httpParams.getValue(key));
						.append(URLEncoder.encode(httpParams.getValue(key),
								"UTF-8"));
			} catch (java.io.UnsupportedEncodingException neverHappen) {
			}
			j++;
		}
		return buf.toString();

	}

	/**
	 * Base64 encode mehtod for request.Refer to weibo development document.
	 * 
	 */
	public static char[] base64Encode(byte[] data) {
		final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
				.toCharArray();
		char[] out = new char[((data.length + 2) / 3) * 4];
		for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
			boolean quad = false;
			boolean trip = false;
			int val = (0xFF & (int) data[i]);
			val <<= 8;
			if ((i + 1) < data.length) {
				val |= (0xFF & (int) data[i + 1]);
				trip = true;
			}
			val <<= 8;
			if ((i + 2) < data.length) {
				val |= (0xFF & (int) data[i + 2]);
				quad = true;
			}
			out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 1] = alphabet[val & 0x3F];
			val >>= 6;
			out[index + 0] = alphabet[val & 0x3F];
		}
		return out;
	}

	public static Bitmap getHttpBitmap(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setConnectTimeout(0);
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
}