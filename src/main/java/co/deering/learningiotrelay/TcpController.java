package co.deering.learningiotrelay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@RestController
public class TcpController {
    public static ServerSocket SERVER_SOCKET;
    public static Socket SOCKET;

    public static BufferedReader BUFFERED_READER;
    public static PrintWriter PRINT_WRITER;

    @ServiceActivator
    @GetMapping("/open/socket")
    public void create() {
        try {
            SERVER_SOCKET = new ServerSocket(8000);
            log.info("server start");
            SOCKET = SERVER_SOCKET.accept();
            OutputStream outputStream = SOCKET.getOutputStream();
            PRINT_WRITER = new PrintWriter(outputStream, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(PRINT_WRITER);

            InputStream inputStream = SOCKET.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BUFFERED_READER = new BufferedReader(inputStreamReader);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/send")
    public void send(@RequestParam String message) {
        byte[] encoded = message.getBytes(StandardCharsets.UTF_16BE);
        PRINT_WRITER.println(encoded);
        log.info("message \"{}\" sent", message);
        log.info("encoded message \"{}\" sent", encoded);
    }

    @GetMapping("/send-with-offset")
    public void sendWithOffset(@RequestParam String message) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(convertHEXString2ByteArray("FFFF"));
        outputStream.write("*HBCR,NB,860640052170273,V0,2#\n".getBytes());

        PRINT_WRITER.print(outputStream.toByteArray());
        log.info("message \"{}\" sent", outputStream);
    }

    @GetMapping("/send-with-offset2")
    public void sendWithOffset2(@RequestParam String message) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(convertHEXString2ByteArray("FFFF"));
        outputStream.write("*HBCS,NB,860640052170273,R0,0,60,1234,1660747046#\n".getBytes());

        PRINT_WRITER.print(outputStream.toByteArray());
        log.info("message \"{}\" sent", outputStream);
    }


    private byte[] convertHEXString2ByteArray(String value) {
        if (value == null || value.length() == 0) {
            return null;
        } else {//from   w  ww .  j  a  va 2  s.  co  m
            char[] array = value.toCharArray();
            int ext = array.length % 2; // can be 0 or 1 only!
            byte[] out = new byte[array.length / 2 + ext];
            for (int i = 0; i < array.length - ext; i += 2) {
                String part = new String(array, i, 2);
                try {
                    out[i / 2] = (byte) Integer.parseInt(part, 16);
                } catch (NumberFormatException e) {
                    // ignore conversion error
                    out[i / 2] = 0;
                }
            }

            if (ext != 0) {
                String part = String.valueOf(array[array.length - 1]);
                try {
                    out[out.length - 1] = (byte) Integer.parseInt(part, 16);
                } catch (NumberFormatException e) {
                    // ignore conversion error
                    out[out.length - 1] = 0;
                }
            }
            return out;
        }
    }

    @GetMapping("/listen")
    public void listen() throws IOException {
        while (true) {
            System.out.println(BUFFERED_READER.readLine());
        }
    }

    @GetMapping("/release")
    public void relase() throws IOException {
        PRINT_WRITER.close();
        BUFFERED_READER.close();
        SERVER_SOCKET.close();
    }
}
