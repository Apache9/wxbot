package com.github.apache9.wxbot;

import java.util.Optional;

/**
 * @author Apache9
 */
public interface MessageProcessor {

    Optional<Message> process(Message msg) throws Exception;
}
