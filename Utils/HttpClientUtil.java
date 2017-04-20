package com.ccx.credit.util;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * 
 * 封装了采用HttpClient发送HTTP请求的方法
 * 
 * 
 * @see 本工具所采用的是HttpComponents-Client-4.2.1
 * 
 * 
 * @see ========================================================================
 *      == =========================
 * 
 * 
 * @see 开发HTTPS应用时，时常会遇到两种情况
 * 
 * 
 * @see 1、测试服务器没有有效的SSL证书,客户端连接时就会抛异常
 * 
 * 
 * @see javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
 * 
 * 
 * @see 2、测试服务器有SSL证书,但可能由于各种不知名的原因,它还是会抛一堆烂码七糟的异常,诸如下面这两种
 * 
 * 
 * @see javax.net.ssl.SSLException: hostname in certificate didn't match:
 *      <123.125.97.66> != <123.125.97.241>
 * 
 * 
 * @see javax.net.ssl.SSLHandshakeException:
 *      sun.security.validator.ValidatorException: PKIX path building failed:
 *      sun.security.provider.certpath.SunCertPathBuilderException: unable to
 *      find valid certification path to requested target
 * 
 * 
 * @see ========================================================================
 *      == =========================
 * 
 * 
 * @see 这里使用的是HttpComponents-Client-4.2.1创建的连接,所以就要告诉它使用一个不同的TrustManager
 * 
 * 
 * @see 由于SSL使用的模式是X.509,对于该模式,Java有一个特定的TrustManager,称为X509TrustManager
 * 
 * 
 * @see TrustManager是一个用于检查给定的证书是否有效的类,所以我们自己创建一个X509TrustManager实例
 * 
 * 
 * @see 而在X509TrustManager实例中
 *      ,若证书无效,那么TrustManager在它的checkXXX()方法中将抛出CertificateException
 * 
 * 
 * @see 既然我们要接受所有的证书,那么X509TrustManager里面的方法体中不抛出异常就行了
 * 
 * 
 * @see 然后创建一个SSLContext并使用X509TrustManager实例来初始化之
 * 
 * 
 * @see 接着通过SSLContext创建SSLSocketFactory,最后将SSLSocketFactory注册给HttpClient就可以了
 * 
 * 
 * @create 2015/12/09
 * 
 * 
 * @author wbh
 */

@SuppressWarnings("deprecation")
public class HttpClientUtil {

	private HttpClientUtil() {
	}

	/**
	 * 
	 * 
	 * 发送HTTP_GET请求
	 * 
	 * 
	 * @see 1)该方法会自动关闭连接,释放资源
	 * 
	 * 
	 * @see 2)方法内设置了连接和读取超时时间,单位为毫秒,超时或发生其它异常时方法会自动返回"通信失败"字符串
	 * 
	 * 
	 * @see 3)请求参数含中文时,经测试可直接传入中文,HttpClient会自动编码发给Server,应用时应根据实际效果决定传入前是否转码
	 * 
	 * 
	 * @see 4)该方法会自动获取到响应消息头中[Content-Type:text/html;
	 *      charset=GBK]的charset值作为响应报文的解码字符集
	 * 
	 * 
	 * @see 若响应消息头中无Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1作为响应报文的解码字符集
	 * 
	 * 
	 * @param requestURL
	 *            请求地址(含参数)
	 * 
	 * 
	 * @return 远程主机响应正文
	 */

	public static String sendGetRequest(String reqURL, String encodeCharset) {

		String respContent = "通信失败"; // 响应内容

		CloseableHttpClient httpClient = HttpClients.createDefault();

		HttpGet httpGet = new HttpGet(reqURL); // 创建org.apache.http.client.methods.HttpGet

		// HttpClient httpClient = new DefaultHttpClient(); // 创建默认的httpClient实例

		// 设置代理服务器
		// httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// new HttpHost("10.0.0.4", 8080));

		// httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
		// 50000); // 连接超时50s（4.0 <= 版本 < 4.3 超时设置方式）
		//
		// httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
		// 50000); // 数据传输超时50s（4.0 <= 版本 < 4.3 超时设置方式）

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(50000).setConnectTimeout(30000).build();// 设置请求和传输超时时间（版本>=4.3设置超时方式）

		httpGet.setConfig(requestConfig);

		try {

			HttpResponse response = httpClient.execute(httpGet); // 执行GET请求

			HttpEntity entity = response.getEntity(); // 获取响应实体

			if (null != entity) {

				// respCharset=EntityUtils.getContentCharSet(entity)也可以获取响应编码,但从4.1.3开始不建议使用这种方式

				respContent = EntityUtils.toString(entity, encodeCharset);

				// Consume response content

				EntityUtils.consume(entity);

			}

			System.out.println("-------------------------------------------------------------------------------------------");
			
			System.out.println("请求地址：" + reqURL);

			StringBuilder respHeaderDatas = new StringBuilder();

			for (Header header : response.getAllHeaders()) {

				respHeaderDatas.append(header.toString()).append("\r\n");

			}
			
			System.out.println("HTTP应答完整报文");
			System.out.println(response.getStatusLine().toString()); // HTTP应答状态行信息
			System.out.println(respHeaderDatas.toString().trim());// HTTP应答报文头信息
			System.out.println(respContent);// HTTP应答报文体信息

			System.out.println("-------------------------------------------------------------------------------------------");

		} catch (ConnectTimeoutException cte) {
			cte.printStackTrace();
			// Should catch ConnectTimeoutException, and don`t catch
			// org.apache.http.conn.HttpHostConnectException

			// //LogUtil.getLogger().error("请求通信[" + reqURL + "]时连接超时,堆栈轨迹如下", cte);

		} catch (SocketTimeoutException ste) {
			ste.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时读取超时,堆栈轨迹如下", ste);

		} catch (ClientProtocolException cpe) {
			cpe.printStackTrace();
			// 该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等

			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时协议异常,堆栈轨迹如下", cpe);

		} catch (ParseException pe) {
			pe.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时解析异常,堆栈轨迹如下", pe);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			// 该异常通常是网络原因引起的,如HTTP服务器未启动等

			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时网络异常,堆栈轨迹如下", ioe);

		} catch (Exception e) {
			e.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时偶遇异常,堆栈轨迹如下", e);

		} finally {

			// 关闭连接,释放资源

			httpClient.getConnectionManager().shutdown();

		}

		return respContent;

	}

	/**
	 * 
	 * 
	 * 发送HTTP_POST请求
	 * 
	 * 
	 * @see 1)该方法允许自定义任何格式和内容的HTTP请求报文体
	 * 
	 * 
	 * @see 2)该方法会自动关闭连接,释放资源
	 * 
	 * 
	 * @see 3)方法内设置了连接和读取超时时间,单位为毫秒,超时或发生其它异常时方法会自动返回"通信失败"字符串
	 * 
	 * 
	 * @see 4)请求参数含中文等特殊字符时,可直接传入本方法,并指明其编码字符集encodeCharset参数,方法内部会自动对其转码
	 * 
	 * 
	 * @see 5)该方法在解码响应报文时所采用的编码,取自响应消息头中的[Content-Type:text/html;
	 *      charset=GBK]的charset值
	 * 
	 * 
	 * @see 若响应消息头中未指定Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1
	 * 
	 * 
	 * @param reqURL
	 *            请求地址
	 * 
	 * 
	 * @param reqData
	 *            请求参数,若有多个参数则应拼接为param11=value11&22=value22&33=value33的形式
	 * 
	 * 
	 * @param encodeCharset
	 *            编码字符集,编码请求数据时用之,此参数为必填项(不能为""或null)
	 * 
	 * 
	 * @return 远程主机响应正文
	 */

	@SuppressWarnings("resource")
	public static String sendPostRequest(String reqURL, String reqData, String encodeCharset, String contentType) {

		String respContent = "通信失败";

		HttpClient httpClient = new DefaultHttpClient();

		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);

		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 50000);

		HttpPost httpPost = new HttpPost(reqURL);

		// 由于下面使用的是new
		// StringEntity(....),所以默认发出去的请求报文头中CONTENT_TYPE值为text/plain;
		// charset=ISO-8859-1

		// 这就有可能会导致服务端接收不到POST过去的参数,比如运行在Tomcat6.0.36中的Servlet,所以我们手工指定CONTENT_TYPE头消息

		httpPost.setHeader(HTTP.CONTENT_TYPE, contentType + "; charset=" + encodeCharset);

		try {

			httpPost.setEntity(new StringEntity(reqData == null ? "" : reqData, encodeCharset));

			HttpResponse response = httpClient.execute(httpPost);

			HttpEntity entity = response.getEntity();

			if (null != entity) {

				respContent = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());

				EntityUtils.consume(entity);

			}
			
			System.out.println("-------------------------------------------------------------------------------------------");
			
			System.out.println("请求地址：" + reqURL);
			
			System.out.println("请求参数：" + reqData);
			

			StringBuilder respHeaderDatas = new StringBuilder();
			for (Header header : response.getAllHeaders()) {
				respHeaderDatas.append(header.toString()).append("\r\n");
			}

			System.out.println("HTTP应答完整报文");
			System.out.println(response.getStatusLine().toString()); // HTTP应答状态行信息
			System.out.println(respHeaderDatas.toString().trim());// HTTP应答报文头信息
			System.out.println(respContent);// HTTP应答报文体信息

			System.out.println("-------------------------------------------------------------------------------------------");

		} catch (ConnectTimeoutException cte) {
			cte.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时连接超时,堆栈轨迹如下",
			// cte);

		} catch (SocketTimeoutException ste) {
			ste.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时读取超时,堆栈轨迹如下",
			// ste);

		} catch (Exception e) {
			e.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时偶遇异常,堆栈轨迹如下", e);

		} finally {

			httpClient.getConnectionManager().shutdown();

		}

		return respContent;

	}

	/**
	 * 
	 * 
	 * 发送HTTP_POST_SSL请求
	 * 
	 * 
	 * @see 1)该方法会自动关闭连接,释放资源
	 * 
	 * 
	 * @see 2)该方法亦可处理普通的HTTP_POST请求
	 * 
	 * 
	 * @see 3)当处理HTTP_POST_SSL请求时,默认请求的是对方443端口,除非reqURL参数中指明了SSL端口
	 * 
	 * 
	 * @see 4)方法内设置了连接和读取超时时间,单位为毫秒,超时或发生其它异常时方法会自动返回"通信失败"字符串
	 * 
	 * 
	 * @see 5)请求参数含中文等特殊字符时,可直接传入本方法,并指明其编码字符集encodeCharset参数,方法内部会自动对其转码
	 * 
	 * 
	 * @see 6)方法内部会自动注册443作为SSL端口,若实际使用中reqURL指定的SSL端口非443,可自行尝试更改方法内部注册的SSL端口
	 * 
	 * 
	 * @see 7)该方法在解码响应报文时所采用的编码,取自响应消息头中的[Content-Type:text/html;
	 *      charset=GBK]的charset值
	 * 
	 * 
	 * @see 若响应消息头中未指定Content-Type属性,则会使用HttpClient内部默认的ISO-8859-1
	 * 
	 * 
	 * @param reqURL
	 *            请求地址
	 * 
	 * 
	 * @param params
	 *            请求参数
	 * 
	 * 
	 * @param encodeCharset
	 *            编码字符集,编码请求数据时用之,当其为null时,则取HttpClient内部默认的ISO-8859-1编码请求参数
	 * 
	 * 
	 * @return 远程主机响应正文
	 */

	@SuppressWarnings("resource")
	public static String sendPostSSLRequest(String reqURL, Map<String, String> params, String encodeCharset) {

		String responseContent = "通信失败";

		HttpClient httpClient = new DefaultHttpClient();

		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);

		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

		// 创建TrustManager()

		// 用于解决javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated

		X509TrustManager trustManager = new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};

		// 创建HostnameVerifier

		// 用于解决javax.net.ssl.SSLException: hostname in certificate didn't match:
		// <123.125.97.66> != <123.125.97.241>

		X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {

			public void verify(String host, SSLSocket ssl) throws IOException {
			}

			public void verify(String host, X509Certificate cert) throws SSLException {
			}

			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			}

			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}

		};

		try {

			// TLS1.0与SSL3.0基本上没有太大的差别,可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext

			SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);

			// 使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用

			sslContext.init(null, new TrustManager[] { trustManager }, null);

			// 创建SSLSocketFactory

			SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, hostnameVerifier);

			// 通过SchemeRegistry将SSLSocketFactory注册到HttpClient上

			httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

			// 创建HttpPost

			HttpPost httpPost = new HttpPost(reqURL);

			// 由于下面使用的是new
			// UrlEncodedFormEntity(....),所以这里不需要手工指定CONTENT_TYPE为application/x-www-form-urlencoded

			// 因为在查看了HttpClient的源码后发现,UrlEncodedFormEntity所采用的默认CONTENT_TYPE就是application/x-www-form-urlencoded

			// httpPost.setHeader(HTTP.CONTENT_TYPE,
			// "application/x-www-form-urlencoded; charset=" + encodeCharset);

			// 构建POST请求的表单参数

			if (null != params) {

				List<NameValuePair> formParams = new ArrayList<NameValuePair>();

				for (Map.Entry<String, String> entry : params.entrySet()) {

					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

				}

				httpPost.setEntity(new UrlEncodedFormEntity(formParams, encodeCharset));

			}

			HttpResponse response = httpClient.execute(httpPost);

			HttpEntity entity = response.getEntity();

			if (null != entity) {

				responseContent = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());

				EntityUtils.consume(entity);

			}

		} catch (ConnectTimeoutException cte) {
			cte.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时连接超时,堆栈轨迹如下",
			// cte);

		} catch (SocketTimeoutException ste) {
			ste.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时读取超时,堆栈轨迹如下",
			// ste);

		} catch (Exception e) {
			e.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时偶遇异常,堆栈轨迹如下", e);

		} finally {

			httpClient.getConnectionManager().shutdown();

		}

		return responseContent;

	}

	public static void main(String[] args) {
		String url = "https://192.168.10.211:443/data-service/identity/auth?name=%E5%BC%A0%E4%B8%89&cid=123456199001011233&account=lhp&sign=7D18E7C9C11B1BF63C31EBA82B3529E0";
		HttpClientUtil.sendGetSSLRequest(url, "UTF-8");

	}

	/**
	 * 
	 * @param reqURL
	 * @param params
	 * @param encodeCharset
	 * @return
	 */
	@SuppressWarnings("resource")
	public static String sendGetSSLRequest(String reqURL, String encodeCharset) {

		String responseContent = "通信失败";

		HttpClient httpClient = new DefaultHttpClient();

		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000);

		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 80000);

		// 创建TrustManager()

		// 用于解决javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated

		X509TrustManager trustManager = new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};

		// 创建HostnameVerifier

		// 用于解决javax.net.ssl.SSLException: hostname in certificate didn't match:
		// <123.125.97.66> != <123.125.97.241>

		X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {

			public void verify(String host, SSLSocket ssl) throws IOException {
			}

			public void verify(String host, X509Certificate cert) throws SSLException {
			}

			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			}

			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}

		};

		try {

			// TLS1.0与SSL3.0基本上没有太大的差别,可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext

			SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);

			// 使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用

			sslContext.init(null, new TrustManager[] { trustManager }, null);

			// 创建SSLSocketFactory

			SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, hostnameVerifier);

			// 通过SchemeRegistry将SSLSocketFactory注册到HttpClient上

			httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

			// 创建org.apache.http.client.methods.HttpGet
			HttpGet httpGet = new HttpGet(reqURL);

			// 由于下面使用的是new
			// UrlEncodedFormEntity(....),所以这里不需要手工指定CONTENT_TYPE为application/x-www-form-urlencoded

			// 因为在查看了HttpClient的源码后发现,UrlEncodedFormEntity所采用的默认CONTENT_TYPE就是application/x-www-form-urlencoded

			HttpResponse response = httpClient.execute(httpGet);

			HttpEntity entity = response.getEntity();

			if (null != entity) {

				responseContent = EntityUtils.toString(entity, encodeCharset);

				EntityUtils.consume(entity);

			}

			System.out.println("-------------------------------------------------------------------------------------------");

			StringBuilder respHeaderDatas = new StringBuilder();

			for (Header header : response.getAllHeaders()) {

				respHeaderDatas.append(header.toString()).append("\r\n");

			}

			String respStatusLine = response.getStatusLine().toString(); // HTTP应答状态行信息

			String respHeaderMsg = respHeaderDatas.toString().trim(); // HTTP应答报文头信息

			String respBodyMsg = responseContent; // HTTP应答报文体信息

			System.out.println("HTTP应答完整报文=[" + respStatusLine + "\r\n" + respHeaderMsg + "\r\n\r\n" + respBodyMsg + "]");

			System.out.println("-------------------------------------------------------------------------------------------");

		} catch (ConnectTimeoutException cte) {
			cte.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时连接超时,堆栈轨迹如下",
			// cte);

		} catch (SocketTimeoutException ste) {
			ste.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时读取超时,堆栈轨迹如下",
			// ste);

		} catch (Exception e) {
			e.printStackTrace();
			// LogUtil.getLogger().error("请求通信[" + reqURL + "]时偶遇异常,堆栈轨迹如下", e);

		} finally {

			httpClient.getConnectionManager().shutdown();

		}

		return responseContent;

	}
}
