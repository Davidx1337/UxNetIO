/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import com.uxsoft.net.io.common.ByteBuffer;
import com.uxsoft.net.io.common.IoListener;
import com.uxsoft.net.io.common.IoSession;
import com.uxsoft.net.io.protocol.DecoderOutput;
import com.uxsoft.net.io.protocol.EncoderOutput;
import com.uxsoft.net.io.protocol.Protocol;
import com.uxsoft.net.io.protocol.ProtocolDecoder;
import com.uxsoft.net.io.protocol.ProtocolEncoder;
import com.uxsoft.net.io.socket.SocketConnector;

/**
 *
 * @author David
 */
public class TestClientText {

    public static void main(String[] argv)
            throws Throwable {
        SocketConnector con = new SocketConnector();
        con.connect(new InetSocketAddress(InetAddress.getLocalHost(), 1111), new Protocol() {
            ProtocolEncoder encoder = new ProtocolEncoder() {
                public void dispose() {

                }

                public void encode(IoSession session, Object message, EncoderOutput output) {
                    String str = (String) message;
                    str = "<div style=\"color:blue;\">" + str;
                    str += "</div>\n";
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

            }

            public void onSessionOpened(IoSession session) {

            }

            public void onMessageSent(IoSession session, Object message) {
                System.err.println("Sent: " + message);
            }

            public void onMessageReceived(IoSession session, Object message) {
                System.err.println("Recieved: " + message.toString());
            }

            public void onSessionClosed(IoSession session) {
                System.err.println("Closed");
            }

            public void onExceptionOccurred(IoSession session, Throwable ex) {
                ex.printStackTrace();
            }
        });
        for (int i = 0; i < 1000; i++) {
            con.write("HELLO WORLD\n");
        }
    }
}
