package org.npbeta.OldwangMC;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MotD {

    private InetSocketAddress host;
    private final Gson gson = new Gson();

    public void setAddress(InetSocketAddress host) {
        this.host = host;
    }

    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    public StatusResponse fetchData() throws IOException {

        Socket socket = new Socket();
        OutputStream outputStream;
        DataOutputStream dataOutputStream;
        InputStream inputStream;
        InputStreamReader inputStreamReader;

        int timeout = 7000;
        socket.setSoTimeout(timeout);

        socket.connect(host, timeout);

        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);

        inputStream = socket.getInputStream();
        inputStreamReader = new InputStreamReader(inputStream);

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(b);
        handshake.writeByte(0x00); //packet id for handshake
        writeVarInt(handshake, 4); //protocol version
        writeVarInt(handshake, this.host.getHostString().length()); //host length
        handshake.writeBytes(this.host.getHostString()); //host string
        handshake.writeShort(host.getPort()); //port
        writeVarInt(handshake, 1); //state (1 for handshake)

        writeVarInt(dataOutputStream, b.size()); //prepend size
        dataOutputStream.write(b.toByteArray()); //write handshake packet

        long startTime = System.currentTimeMillis();
        dataOutputStream.writeByte(0x01); //size is only 1
        dataOutputStream.writeByte(0x00); //packet id for ping
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int size = readVarInt(dataInputStream); //size of packet
        int id = readVarInt(dataInputStream); //packet id

        if (id == -1) {
            throw new IOException("Premature end of stream.");
        }

        if (id != 0x00) { //we want a status response
            throw new IOException("Invalid packetID");
        }
        int length = readVarInt(dataInputStream); //length of json string

        if (length == -1) {
            throw new IOException("Premature end of stream.");
        }

        if (length == 0) {
            throw new IOException("Invalid string length.");
        }

        long endTime = System.currentTimeMillis(); //read response
        byte[] in = new byte[length];
        dataInputStream.readFully(in);  //read json string
        String json = new String(in);


        long now = System.currentTimeMillis();
        dataOutputStream.writeByte(0x09); //size of packet
        dataOutputStream.writeByte(0x01); //0x01 for ping
        dataOutputStream.writeLong(now); //time!?

        readVarInt(dataInputStream);
        id = readVarInt(dataInputStream);
        if (id == -1) {
            throw new IOException("Premature end of stream.");
        }

        if (id != 0x01) {
            throw new IOException("Invalid packetID");
        }

        StatusResponse response = gson.fromJson(json, StatusResponse.class);
        response.setTime((int) (endTime - startTime));

        dataOutputStream.close();
        outputStream.close();
        inputStreamReader.close();
        inputStream.close();
        socket.close();

        return response;
    }

    public static class StatusResponse {
        private Players players;
        private Version version;
        private int time;

        public Players getPlayers() {
            return players;
        }

        public Version getVersion() {
            return version;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }

    public static class Players {
        private int online;

        public int getOnline() {
            return online;
        }
    }

    public static class Version {
        private String name;

        public String getName() {
            return name;
        }
    }
}