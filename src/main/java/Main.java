import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by guhailin on 13-12-30.
 */
public class Main {
    private Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        OptionParser optionParser = new OptionParser();
        optionParser.acceptsAll(Arrays.asList("help", "?", "h"), "show this help.");
        optionParser.acceptsAll(Arrays.asList("s", "server")).withRequiredArg().describedAs("email smtp server");
        optionParser.acceptsAll(Arrays.asList("p", "port"))
                .withRequiredArg().describedAs("email smtp server port")
                .ofType(Integer.class)
                .defaultsTo(25);
        optionParser.acceptsAll(Arrays.asList("u", "username")).withRequiredArg().describedAs("email smtp server uername");
        optionParser.acceptsAll(Arrays.asList("password")).withRequiredArg().describedAs("email smtp server password");
        optionParser.acceptsAll(Arrays.asList("title", "subject")).withRequiredArg().describedAs("email title");
        optionParser.acceptsAll(Arrays.asList("c", "content")).withRequiredArg().describedAs("email content");
        optionParser.acceptsAll(Arrays.asList("f", "from")).withRequiredArg().describedAs("email from");
        optionParser.acceptsAll(Arrays.asList("t", "to")).withRequiredArg().describedAs("email to");

        OptionSet optionSet = optionParser.parse(args);
        if (optionSet.hasArgument("help") || optionSet.hasArgument("?") || optionSet.hasArgument("h")) {
            try {
                optionParser.printHelpOn(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        } else {
            String address = (String) optionSet.valueOf("s");
            int port = (Integer) optionSet.valueOf("p");
            String username = (String) optionSet.valueOf("u");
            String password = (String) optionSet.valueOf("password");
            String subject = (String) optionSet.valueOf("subject");
            String content = (String) optionSet.valueOf("c");
            String from = (String) optionSet.valueOf("from");
            String to = (String) optionSet.valueOf("to");

            MySmtp mySmtp = new MySmtp(address, port, username, password, from, to, subject, content);
            try {
                mySmtp.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
