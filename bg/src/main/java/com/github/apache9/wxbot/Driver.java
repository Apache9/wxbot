package com.github.apache9.wxbot;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Apache9
 */
public class Driver implements Closeable {

    private static final Logger LOG = LogManager.getFormatterLogger(Driver.class);

    private final MessageStore store;

    private final List<MessageProcessor> processors;

    public Driver(Config conf) throws SQLException {
        this.store = new MessageStore(conf.msgDbPath);
        this.processors = Arrays.asList(new IpProcessor(), new CreditCardProcessor(conf.financeDbPath),
                new DebitCardProcessor(conf.financeDbPath));
    }

    public void exec() {
        outer: for (;;) {
            Optional<Message> msg = Optional.empty();
            try {
                msg = store.poll();
            } catch (Exception e) {
                LOG.warn("failed to poll message", e);
            }
            if (!msg.isPresent()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOG.warn("sleep interrupted, quit", e);
                    Thread.interrupted();
                    return;
                }
                continue;
            }
            Message m = msg.get();
            for (MessageProcessor processor : processors) {
                Optional<Message> toSend;
                try {
                    toSend = processor.process(m);
                } catch (Exception e) {
                    LOG.warn(() -> "failed to process " + m, e);
                    continue;
                }
                if (toSend.isPresent()) {
                    try {
                        store.send(toSend.get());
                    } catch (Exception e) {
                        LOG.warn(() -> "failed to send " + toSend.get(), e);
                    }
                    continue outer;
                }
            }
            LOG.warn("no processor for %s", m);
            try {
                store.send(new Message("", "TEXT", "@" + m.getMember() + " 不好意思，我没听懂", ""));
            } catch (Exception e) {
                LOG.warn("failed to send no reply", e);
            }
        }
    }

    @Override
    public void close() {
        store.close();
    }

    public static void main(String[] args) throws SQLException, IOException {
        try (Driver driver = new Driver(new Config(new File(args[0])))) {
            driver.exec();
        }
    }
}
