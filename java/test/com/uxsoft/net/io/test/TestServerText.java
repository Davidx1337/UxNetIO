/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.uxsoft.net.io.test;

import com.uxsoft.net.io.common.ByteBuffer;
import com.uxsoft.net.io.common.IoListener;
import com.uxsoft.net.io.common.IoSession;
import com.uxsoft.net.io.protocol.DecoderOutput;
import com.uxsoft.net.io.protocol.EncoderOutput;
import com.uxsoft.net.io.protocol.Protocol;
import com.uxsoft.net.io.protocol.ProtocolDecoder;
import com.uxsoft.net.io.protocol.ProtocolEncoder;
import com.uxsoft.net.io.socket.SocketAcceptor;

/**
 *
 * @author David
 */
public class TestServerText {

    /**
     * @param args the command line arguments
     */
public static void main(String[] argv)
            throws Throwable {
        SocketAcceptor acceptor = new SocketAcceptor(1111, 10);
        //acceptor.setHostAddress(InetAddress.getLocalHost());
        acceptor.bind(new Protocol() {
            ProtocolEncoder encoder = new ProtocolEncoder() {
                public void dispose() {

                }

                public void encode(IoSession session, Object message, EncoderOutput output) {
                    String str = (String) message;
                    str = "{" + str;
                    str += "}\n";
                    ByteBuffer bb = ByteBuffer.allocate(str.length());
                    bb.put(str.getBytes());
                    bb.flip();
                    output.write(bb);
                }
            };
            ProtocolDecoder decoder = new ProtocolDecoder() {
                public void dispose() {

                }

                public void decode(IoSession session, ByteBuffer message, DecoderOutput output) {
                    byte[] data = message.array();
                    output.write(new String(data));
                }
            };

            public ProtocolEncoder getEncoder() {
                return encoder;
            }

            public ProtocolDecoder getDecoder() {
                return decoder;
            }
        }, new IoListener() {

            public void onSessionCreated(IoSession session) {
                System.err.println("SessionCreated");
            }

            public void onSessionOpened(IoSession session) {
                System.err.println("SessionOpened");
            }

            public void onMessageSent(IoSession session, Object message) {
                System.err.println("MessageSent: " + message);
            }

            public void onMessageReceived(IoSession session, Object message) {
                System.err.println("MessageReceived: " + message);
                for (int i = 0; i < 100; i++) {
                    StringBuilder b = new StringBuilder("Hello from UxNetIO! This is line:"+i);
                    /*for (int z = 0; z < 100000; z++) {
                        b.append("a");
                    }*/
                    session.write(b.toString());
                }
                session.close();
            }

            public void onSessionClosed(IoSession session) {
                System.err.println("SessionClosed");
            }

            public void onExceptionOccurred(IoSession session, Throwable ex) {
                ex.printStackTrace();
            }
        });
        
        System.err.println("LISTENING ON PORT: " + acceptor.getPort());
    }
    
}
