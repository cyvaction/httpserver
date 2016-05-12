package org.github.httpserver.http.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.github.httpserver.http.Consts;
import org.github.httpserver.http.HttpRequest;
import org.github.httpserver.http.HttpResponse;
import org.github.httpserver.log.Logger;
import org.github.httpserver.log.LoggerFactory;
import org.github.httpserver.utils.Singleton;
import org.github.httpserver.utils.StringUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

	private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (msg instanceof FullHttpRequest) {
				FullHttpRequest nettyRequest = (FullHttpRequest) msg;
				if (HttpHeaders.is100ContinueExpected(nettyRequest)) {
					FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
					ctx.write(response);
					return;
				}

				HttpRequest request = new HttpRequest();
				request.setRemoteAddress(ctx.channel().remoteAddress());
				request.setLocalAddress(ctx.channel().localAddress());
				request.setUri(nettyRequest.getUri());
				request.setMethod(org.github.httpserver.http.HttpMethod.valueOf(nettyRequest.getMethod().name()));
				if (HttpMethod.OPTIONS.equals(nettyRequest.getMethod())) {
					ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
					return;
				}
				if (HttpMethod.POST.equals(nettyRequest.getMethod()) 
						|| HttpMethod.PUT.equals(nettyRequest.getMethod())
						|| HttpMethod.DELETE.equals(nettyRequest.getMethod())
						|| HttpMethod.PATCH.equals(nettyRequest.getMethod())) {
					String contentType = nettyRequest.headers().get(HttpHeaders.Names.CONTENT_TYPE);
					// json first
					if(StringUtils.isBlank(contentType) || contentType.startsWith("application/json") || contentType.startsWith("text/plain")) {
						String body = nettyRequest.content().toString(Consts.UTF_8);
						request.setBody(body);
					} else {
						// fix http post(Content-Type=x-www-form-urlencoded) bug: 必须以换行结尾
						if(!HttpPostRequestDecoder.isMultipart(nettyRequest)) {
	                        ByteBuf content = nettyRequest.content();
	                        if(content.getByte(content.writerIndex()-1) != HttpConstants.LF) {
	                            content.writeByte(HttpConstants.CR);
	                            content.writeByte(HttpConstants.LF);
	                        }
	                    }
						HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, nettyRequest);
						while (decoder.hasNext()) {
							InterfaceHttpData data = decoder.next();
							if (data.getHttpDataType() == HttpDataType.Attribute) {
								Attribute attr = (Attribute) data;
								if(request.getParameters() == null) {
									request.setParameters(new HashMap<String, List<String>>());
								}
								request.getParameters().put(attr.getName(), Arrays.asList(attr.getValue()));
							} else if(data.getHttpDataType() == HttpDataType.FileUpload) {
								// TODO : not support file
								logger.warn("Http server do not suport file upload.");
//								FileUpload fileUpload = (FileUpload) data;
//					            if (fileUpload.isCompleted()) {
//
//					                logger.info("data - " + data);
//					                logger.info("File name: " + fileUpload.getFilename()+", length - "+fileUpload.length());
//					                logger.info("File isInMemory - " + fileUpload.isInMemory());
//
//					                logger.info("File rename to ...");
//					                File dest = new File(Configuration.FILE_DIR, fileUpload.getFile().getName());
//					                fileUpload.renameTo(dest);
//					                decoder.removeHttpDataFromClean(fileUpload);
//
//					                logger.info("File rename over .");
//					            }else {
//					                logger.debug("File to be continued!");
//					            }
							}
						}
						decoder.destroy();
					}
				}

				// http cookies
				Set<Cookie> nettyCookies = Collections.emptySet();
				String cookieValue = nettyRequest.headers().get(HttpHeaders.Names.COOKIE);
				if (cookieValue != null) {
					nettyCookies = ServerCookieDecoder.STRICT.decode(cookieValue);
				}
				Set<org.github.httpserver.http.Cookie> cookies = new HashSet<org.github.httpserver.http.Cookie>();
				for (Cookie c : nettyCookies) {
					org.github.httpserver.http.Cookie cookie = new org.github.httpserver.http.Cookie(c.name(), c.value());
					cookie.setDomain(c.domain());
					cookie.setHttpOnly(c.isHttpOnly());
					cookie.setMaxAge(c.maxAge());
					cookie.setPath(c.path());
					cookie.setSecure(c.isSecure());
					cookies.add(cookie);
				}
				request.setCookies(cookies);

				// query string decode
				QueryStringDecoder decoderQuery = new QueryStringDecoder(nettyRequest.getUri());
				Map<String, List<String>> parameters = decoderQuery.parameters();
				request.setPath(decoderQuery.path());
				if(request.getParameters() == null) {
					request.setParameters(new HashMap<String, List<String>>());
				}
				request.getParameters().putAll(parameters);

				HttpResponse response = new HttpResponse();
				// dispatch request
				HttpDispatcher dispatcher = Singleton.get(HttpDispatcher.class);
				dispatcher.dispatchRequest(request, response);

				// Decide whether to close the connection or not.
				// Build the response object.
				FullHttpResponse nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.valueOf(response.getStatusCode().value()), Unpooled.copiedBuffer(response.getBody()));
				nettyResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, response.getContentType());

				boolean keepAlive = HttpHeaders.isKeepAlive(nettyRequest);
				if (keepAlive) {
					// Add 'Content-Length' header only for a keep-alive
					// connection.
					nettyResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, nettyResponse.content().readableBytes());
					// Add keep alive header as per:
					// -
					// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
					nettyResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
				}

				// Write the response.
				ctx.write(nettyResponse);

				if (!keepAlive) {
					// If keep-alive is off, close the connection once the
					// content
					// is fully written.
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		} catch (Throwable t) {
			logger.error(t);
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.INTERNAL_SERVER_ERROR);
			ctx.write(response);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		logger.error(t);
		ctx.close();
	}

}
