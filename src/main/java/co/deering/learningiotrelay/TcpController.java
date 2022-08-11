package co.deering.learningiotrelay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@RestController
public class TcpController {
    public static ServerSocket SERVER_SOCKET;
    public static PrintWriter PRINT_WRITER;

    @ServiceActivator
    @GetMapping("/open/socket")
    public void create() {
        try {
            SERVER_SOCKET = new ServerSocket(8000);
            log.info("server start");

            Socket socket = SERVER_SOCKET.accept();

            OutputStream outputStream = socket.getOutputStream();
            PRINT_WRITER = new PrintWriter(outputStream, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/send")
    public void send(@RequestParam String message) {
        byte[] encoded = message.getBytes(StandardCharsets.UTF_16BE);
        PRINT_WRITER.println(encoded);
    }

    @GetMapping("/send-with-offset")
    public void sendWithOffset(@RequestParam String message) {
        byte[] encoded = message.getBytes(StandardCharsets.UTF_16BE);
        byte[] withOffset = Arrays.copyOfRange(encoded, 2, encoded.length);
        PRINT_WRITER.println(withOffset);
    }

    @GetMapping("/release")
    public void relase() throws IOException {
        PRINT_WRITER.close();
        SERVER_SOCKET.close();
    }
}
