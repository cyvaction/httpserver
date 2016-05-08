package org.github.httpserver.http.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.github.httpserver.http.HttpRequest;
import org.github.httpserver.http.HttpResponse;
import org.github.httpserver.log.Logger;
import org.github.httpserver.log.LoggerFactory;
import org.github.httpserver.utils.Singleton;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk

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
				} else if (HttpMethod.GET.equals(nettyRequest.getMethod()) || HttpMethod.HEAD.equals(nettyRequest.getMethod())) {
					// NOOP
				} else if (HttpMethod.POST.equals(nettyRequest.getMethod()) || HttpMethod.PUT.equals(nettyRequest.getMethod())
						|| HttpMethod.DELETE.equals(nettyRequest.getMethod())) {
					HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, nettyRequest);
					while (decoder.hasNext()) {
						InterfaceHttpData data = decoder.next();
						if (data instanceof Attribute) {
							Attribute attr = (Attribute) data;
							request.getParameters().put(attr.getName(), Arrays.asList(attr.getValue()));
						}
					}
					decoder.destroy();
				} else {
					FullHttpResponse nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
							HttpResponseStatus.BAD_REQUEST);
					ctx.write(nettyResponse);
					return;
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
				request.getParameters().putAll(parameters);;

				HttpResponse resp = new HttpResponse();
				// dispatch request
				HttpDispatcher dispatcher = Singleton.get(HttpDispatcher.class);
				dispatcher.dispatchRequest(request, resp);

				// Decide whether to close the connection or not.
				// Build the response object.
				FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.valueOf(resp.getStatusCode().value()), Unpooled.copiedBuffer(resp.getBody()));
				response.headers().set(HttpHeaders.Names.CONTENT_TYPE, resp.getContentType());

				boolean keepAlive = HttpHeaders.isKeepAlive(nettyRequest);
				if (keepAlive) {
					// Add 'Content-Length' header only for a keep-alive
					// connection.
					response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
					// Add keep alive header as per:
					// -
					// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
					response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
				}

				// Write the response.
				ctx.write(response);

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
