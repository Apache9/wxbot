package com.github.apache9.wxbot;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Apache9
 */
public class MessageStore implements Closeable {

    private static final Logger LOG = LogManager.getLogger(MessageStore.class);

    private final Connection conn;

    private String dbUrl;

    public MessageStore(String dbFile) throws SQLException {
        dbUrl = "jdbc:sqlite:" + dbFile;
        conn = DriverManager.getConnection(dbUrl);
    }

    public Optional<Message> poll() throws SQLException {
        long id;
        try (PreparedStatement pst = conn.prepareStatement("SELECT MIN(ID) FROM Receive");
                ResultSet rst = pst.executeQuery()) {
            if (!rst.next()) {
                return Optional.empty();
            }
            id = rst.getLong(1);
        }
        Message message;
        try (PreparedStatement pst = conn.prepareStatement("SELECT * FROM Receive WHERE ID = ?")) {
            pst.setLong(1, id);
            try (ResultSet rst = pst.executeQuery()) {
                if (!rst.next()) {
                    return Optional.empty();
                }
                String messageId = rst.getString("MSG_ID");
                String type = rst.getString("TYPE");
                String content = rst.getString("CONTENT");
                int idx = content.indexOf(' ');
                String member = rst.getString("MEMBER");
                message = new Message(messageId, type, content.substring(idx + 1), member);
                LOG.info("Message received: %s", message);
            }
        }
        try (PreparedStatement pst = conn.prepareStatement("DELETE FROM Receive WHERE ID = ?")) {
            pst.setLong(1, id);
            pst.executeUpdate();
        }
        return Optional.of(message);
    }

    public void send(Message msg) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement pst = conn.prepareStatement("INSERT INTO Send (TYPE. CONTENT) VALUES (?, ?)")) {
            pst.setString(1, msg.getType());
            pst.setString(2, msg.getContent());
            pst.executeUpdate();
            LOG.info("Message sent: %s", msg);
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            LOG.warn("close sqlite connection failed", e);
        }
    }
}
