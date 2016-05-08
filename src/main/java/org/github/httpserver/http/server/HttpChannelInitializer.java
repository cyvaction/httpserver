package org.github.httpserver.http.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
	
    private static final String[] DEFAULT_CORS_METHODS = { "OPTIONS", "HEAD", "GET", "POST", "PUT", "DELETE" };
    private static final String[] DEFAULT_CORS_HEADERS = { "X-Requested-With", "Content-Type", "Content-Length" };
    private static final int DEFAULT_CORS_MAX_AGE = 1728000;
    
    @Override
    public void initChannel(SocketChannel ch) {
    	ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
  
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast("cors", new CorsHandler(buildCorsConfig()));
        /**
         * Compresses an HttpMessage and an HttpContent in gzip or deflate encoding
         * while respecting the "Accept-Encoding" header.
         * If there is no matching encoding, no compression is done.
         */
        pipeline.addLast("deflater", new HttpContentCompressor());
  
        pipeline.addLast("handler", new HttpServerHandler());
    }
    
    private CorsConfig buildCorsConfig() {
        final CorsConfig.Builder builder = new CorsConfig.Builder();
        builder.allowCredentials();
        String[] strMethods =  DEFAULT_CORS_METHODS;
        HttpMethod[] methods = new HttpMethod[strMethods.length];
        for (int i = 0; i < methods.length; i++) {
            methods[i] = HttpMethod.valueOf(strMethods[i]);
        }
        return builder.allowedRequestMethods(methods)
                      .maxAge( DEFAULT_CORS_MAX_AGE)
                      .allowedRequestHeaders(DEFAULT_CORS_HEADERS)
                      .shortCurcuit()
                      .build();
    }
}