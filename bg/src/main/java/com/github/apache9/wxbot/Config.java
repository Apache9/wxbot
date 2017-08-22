package com.github.apache9.wxbot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/**
 * @author Apache9
 */
public class Config {

    public final String msgDbPath;

    public final String financeDbPath;

    public Config(File file) throws IOException {
        Properties props = new Properties();
        props.load(Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8));
        msgDbPath = props.getProperty("msg_db_path");
        financeDbPath = props.getProperty("financeDbPath");
    }
}
