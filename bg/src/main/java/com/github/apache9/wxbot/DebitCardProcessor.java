package com.github.apache9.wxbot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Apache9
 */
public class DebitCardProcessor implements MessageProcessor {

    private static final String PREFIX = "储蓄卡 ";

    private static final String LOOKUP = "查询 ";

    private final String dbUrl;

    public DebitCardProcessor(String dbFile) throws SQLException {
        dbUrl = "jdbc:sqlite:" + dbFile;
    }

    @Override
    public Optional<Message> process(Message msg) throws Exception {
        if (!msg.getContent().startsWith(PREFIX)) {
            return Optional.empty();
        }
        String cmd = msg.getContent().substring(PREFIX.length());
        if (cmd.startsWith(LOOKUP)) {
            List<String> cards = new ArrayList<>();
            String toMatch = cmd.substring(LOOKUP.length());
            try (Connection conn = DriverManager.getConnection(dbUrl);
                    PreparedStatement pst = conn.prepareStatement("SELECT * FROM DebitCard");
                    ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    String bank = rst.getString("BANK");
                    String number = rst.getString("NUMBER");
                    String owner = rst.getString("OWNER");
                    if (bank.contains(toMatch) || owner.contains(toMatch) || number.endsWith(toMatch)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("户名：").append(owner).append("\n");
                        sb.append("开户行：").append(bank).append("\n");
                        sb.append("卡号：").append(CardUtils.formatCardNumber(number)).append("\n");
                        cards.add(sb.toString());
                    }
                }
            }
            if (!cards.isEmpty()) {
                return Optional.of(new Message("", "TEXT",
                        cards.stream().collect(Collectors.joining("\n", "@" + msg.getMember() + " ", "")), ""));
            }
        }
        return Optional.of(new Message("", "TEXT", "@" + msg.getMember() + " 没有查询到匹配的储蓄卡信息", ""));
    }

}
