package org.github.httpserver.http.server;

import org.github.httpserver.lifecycle.Lifecycle;
import org.github.httpserver.log.Logger;
import org.github.httpserver.log.LoggerFactory;
import org.github.httpserver.utils.Singleton;
import org.github.httpserver.utils.StringUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpServer extends Lifecycle {
	private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
	
	private HttpConfig config;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ChannelFuture channelFuture;
	public HttpServer(HttpConfig config) {
		this.config = config;
		// url ext
		if(StringUtils.isNotBlank(config.getUrlExt())) {
			String[] exts = config.getUrlExt().split(",");
			for(String ext : exts) {
				HttpDispatcher dispatcher = Singleton.get(HttpDispatcher.class);
				dispatcher.addUrlExt(ext);
			}
		}
		
		if(config.getBossThreads() > 0) {
			bossGroup = new NioEventLoopGroup(config.getBossThreads());
		} else {
			bossGroup = new NioEventLoopGroup();
		}
		if(config.getWorkerThreads() > 0) {
			workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
		} else {
			workerGroup = new NioEventLoopGroup();
		}
	}

	@Override
	protected void doStart() {
		logger.info("Http server starting.");
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new HttpChannelInitializer())
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			channelFuture = bootstrap.bind(config.getPort()).sync();
			logger.info("Http server started. listen at [{}].", channelFuture.channel().localAddress());
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error(e, "Http server start error.");
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	@Override
	protected void doStop() {
		logger.info("Http server stopping.");
		channelFuture.channel().close();
		logger.info("Http server stoped.");
	}
}
