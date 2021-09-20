package com.clinton;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class Application {
    private static final String MEET_URL_ENV = "MEETUP_RSVP_WS";
    private static final String MEET_PORT_ENV = "MEETUP_RSVP_PORT";

    private static final String URL = Util.getEnv(MEET_URL_ENV);
    private static final int port = Integer.parseInt(Util.getEnv(MEET_PORT_ENV));

    private static boolean continueRunning = true;

    public static void main(String[] args) throws Exception {
        try {
            HybridMessageLogger.initialize();
        } catch (Exception exception) {
            System.err.println("Could not initialize HybridMessageLogger!");
            exception.printStackTrace();
            System.exit(-1);
        }

        EventLoopGroup group = new NioEventLoopGroup();

        final RSVPProducer rsvpProducer = new RSVPProducer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutdown started...");
                group.shutdownGracefully();
                rsvpProducer.close();
                HybridMessageLogger.close();
                continueRunning = false;
                System.out.println("Shutdown finished");
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }));

        URI uri = new URI(URL);

        try {
            final MeetupWebSocketClientHandler handler =
                    new MeetupWebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()), rsvpProducer);

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpClientCodec());
                            p.addLast(new HttpObjectAggregator(8192));
                            p.addLast(WebSocketClientCompressionHandler.INSTANCE);
                            p.addLast(handler);
                        }
                    });

            Channel ch = b.connect(uri.getHost(), port).sync().channel();
            handler.handshakeFuture().sync();

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            do {
                String msg = console.readLine();
                if (msg == null) {
                    continueRunning = false;
                } else if ("bye".equalsIgnoreCase(msg)) {
                    ch.writeAndFlush(new CloseWebSocketFrame());
                    ch.closeFuture().sync();
                    continueRunning = false;
                } else if ("ping".equalsIgnoreCase(msg)) {
                    WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
                    ch.writeAndFlush(frame);
                } else {
                    WebSocketFrame frame = new TextWebSocketFrame(msg);
                    ch.writeAndFlush(frame);
                }
            } while (continueRunning);
        } finally {
            group.shutdownGracefully();
        }
    }
}
