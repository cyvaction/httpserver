package org.github.httpserver.http.server;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.github.httpserver.lifecycle.Lifecycle;
import org.github.httpserver.log.Logger;
import org.github.httpserver.log.LoggerFactory;
import org.github.httpserver.utils.Singleton;
import org.github.httpserver.utils.StringUtils;

public class HttpServer extends Lifecycle {
	private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
	
	private HttpConfig config;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	public HttpServer(HttpConfig config) {
		this.config = config;
		if(StringUtils.isNotBlank(config.getUrlExt())) {
			String[] exts = config.getUrlExt().split(",");
			for(String ext : exts) {
				HttpDispatcher dispatcher = Singleton.get(HttpDispatcher.class);
				dispatcher.addUrlExt(ext);
			}
		}
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
	}

	@Override
	protected void doStart() {
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new HttpChannelInitializer())
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = bootstrap.bind(config.getPort()).sync();
			
			logger.info("Http server started at port[{}].", config.getPort());
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error(e, "Http server start error, port[{}].", config.getPort());
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	@Override
	protected void doStop() {
		workerGroup.shutdownGracefully(20, 20, TimeUnit.SECONDS);
		bossGroup.shutdownGracefully(20, 20, TimeUnit.SECONDS);
	}
}
