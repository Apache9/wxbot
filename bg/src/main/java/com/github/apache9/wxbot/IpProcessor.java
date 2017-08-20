
package com.github.apache9.wxbot;

import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;

/**
 * @author Apache9
 */
public class IpProcessor implements MessageProcessor {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36";

    @Override
    public Optional<Message> process(Message msg) throws Exception {
        if (!msg.getContent().equalsIgnoreCase("IP")) {
            return Optional.empty();
        }
        String content = Jsoup
                .parse(Request.Get("http://www.ip.cn").userAgent(USER_AGENT).execute().returnContent().asString())
                .getElementById("result").getElementsByTag("p").stream().map(e -> e.text())
                .collect(Collectors.joining("\r\n", "@" + msg.getMember(), ""));
        return Optional.of(new Message("", "TEXT", content, ""));
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new IpProcessor().process(new Message("", "", "ip", "")));
    }
}
