package client;

import java.util.concurrent.BlockingQueue;
import client.state.ClientStatemanagement;
import json.util.JSONNameandString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class MyWebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

	private Channel ch;
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private final ClientStatemanagement State = new ClientStatemanagement();

    public MyWebSocketClientHandler(WebSocketClientHandshaker handshaker, BlockingQueue<JSONNameandString> receque) {
        this.handshaker = handshaker;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        session = new ClientSession(ctx.channel());
        handshaker.handshake(ctx.channel());
        ch=ctx.channel();
        State.setChannel(ch);
    }
    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected!");
        ClientManage.hasSendCloseSign();
        ClientManage.waiteforclose();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            System.out.println("WebSocket Client connected!");
            State.handle(null);
            return;
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
        	String request = ((TextWebSocketFrame) frame).text();
        	State.handle(request);
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }
    
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
	
	public void waitforAccessSign(){
		State.waitforAccessSign();
	}

}
