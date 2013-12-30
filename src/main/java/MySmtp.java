import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by guhailin on 13-12-29.
 */
public class MySmtp {
    private Logger logger = LoggerFactory.getLogger(MySmtp.class);

    private String server;
    private int port;
    private String from;
    private String to;
    private String username;
    private String password;
    private final byte[] CRLF = "\r\n".getBytes();

    private String content;
    private String subject;

    private InputStream inputStream;
    private OutputStream outputStream;

    public MySmtp(String server, int port, String username, String password, String from, String to, String subject, String content) {
        this.server = server;
        this.port = port;
        BASE64Encoder encoder = new BASE64Encoder();
        this.username = encoder.encode(username.getBytes());
        this.password = encoder.encode(password.getBytes());
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public void send() throws Exception {
        Socket socket = new Socket(server, port);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        ArrayList<String> res = read();
        if (res.size() == 1 && res.get(0).startsWith("220")) {
            if (ehlo()) {
                if (auth(username, password)) {
                    if (mail(from)) {
                        if (rcpt(to)) {
                            if (data(content, subject, from, to))
                                quit();
                            else {
                                throw new Exception("can not send data");
                            }
                        } else {
                            throw new Exception("can not set to");
                        }
                    } else {
                        throw new Exception("can not set from");
                    }
                } else {
                    throw new Exception("auth failed");
                }
            } else {
                throw new Exception(String.format("can not connect the server:%s:%d", server, port));
            }
        } else {
            throw new Exception(String.format("can not connect the server:%s:%d", server, port));
        }
        inputStream.close();
        outputStream.close();
    }

    private boolean ehlo() throws Exception {
        String ehlo = "ehlo guhailin";
        write(ehlo.getBytes());
        ArrayList<String> strings = read();
        return strings.get(strings.size() - 1).startsWith("250");
    }

    private boolean auth(String username, String password) throws Exception {
        String auth = "auth login";
        write(auth.getBytes());
        ArrayList<String> authResponse = read();
        if (!authResponse.get(0).startsWith("334")) {
            return false;
        }
        write(username.getBytes());
        ArrayList<String> usernameResponse = read();
        if (!usernameResponse.get(0).startsWith("334")) {
            return false;
        }
        write(password.getBytes());
        ArrayList<String> passwordResponse = read();
        if (!passwordResponse.get(0).startsWith("235")) {
            return false;
        }
        return true;
    }

    private boolean mail(String sender) throws Exception {
        String mail = String.format("mail From:<%s>", sender);
        write(mail);
        ArrayList<String> response = read();
        if (!response.get(0).startsWith("250")) {
            return false;
        }
        return true;

    }

    private boolean rcpt(String receiver) throws Exception {
        String rcpt = String.format("rcpt To:<%s>", receiver);
        write(rcpt);
        ArrayList<String> response = read();
        if (!response.get(0).startsWith("250")) {
            return false;
        }
        return true;
    }

    private boolean data(String content, String subject, String from, String to) throws Exception {
        write("data");
        ArrayList<String> response = read();
        if (!response.get(0).startsWith("354")) {
            return false;
        }
        String data = String.format("Subject:%s\r\nFrom:\"\"<%s>\r\nTo:\"\"<%s>\r\n\r\n%s", subject, from, to, content);
        write(data);
        write(".");
        response = read();
        if (!response.get(0).startsWith("250")) {
            return false;
        }
        return true;
    }

    private void quit() throws Exception {
        write("quit");
        read();
    }

    private ArrayList<String> read() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> strings = new ArrayList<String>();
        String line = null;
        try {
            while (true) {
                line = reader.readLine();
                System.out.println("<<<<<<");
                System.out.println(line);
                strings.add(line);
                if (isInteger(line.split(" ")[0])) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strings;
    }

    private void write(String data) throws IOException {
        System.out.println(">>>>>>");
        System.out.println(data);
        write(data.getBytes());
    }

    private void write(byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.write(CRLF);
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
