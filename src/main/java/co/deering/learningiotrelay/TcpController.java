package co.deering.learningiotrelay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
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
        outputStream.write(0xFFFF);
        outputStream.write(message.getBytes());

        PRINT_WRITER.println(outputStream);
        log.info("message \"{}\" sent", outputStream);
        log.info("encoded message \"{}\" sent", outputStream);
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
