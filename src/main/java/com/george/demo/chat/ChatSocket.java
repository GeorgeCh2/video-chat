package com.george.demo.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ChatSocket {

    private static EventLoopGroup parentGroup = new NioEventLoopGroup();

    private static EventLoopGroup childGroup = new NioEventLoopGroup();

    private static ChannelFuture channelFuture;

    @PostConstruct
    public static void startServer() {
        System.out.println("ChatSocket.startServer");

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/chat",
                                    null, true, Integer.MAX_VALUE, false));
                            pipeline.addLast(new MessageToMessageCodec<TextWebSocketFrame, String>() {
                                @Override
                                protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
                                    out.add(new TextWebSocketFrame(msg));
                                }

                                @Override
                                protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame msg, List<Object> out) throws Exception {
                                    out.add(msg.text());
                                }
                            });
                            pipeline.addLast(new ChatHandler());
                        }
                    });

            channelFuture = bootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println("ChatSocket.startServer exception: " + e.getMessage());
        } finally{
            shutdown();
        }
    }

    public static void shutdown() {
        System.out.println("ChatSocket.shutdown");
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println("ChatSocket.shutdown exception: " + e.getMessage());
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }
}
