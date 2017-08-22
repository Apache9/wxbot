
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
public class CreditCardProcessor implements MessageProcessor {

    private static final String PREFIX = "信用卡 ";

    private static final String LOOKUP = "查询 ";

    private final String dbUrl;

    public CreditCardProcessor(String dbFile) throws SQLException {
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
                    PreparedStatement pst = conn.prepareStatement("SELECT * FROM CreditCard");
                    ResultSet rst = pst.executeQuery()) {
                while (rst.next()) {
                    String bank = rst.getString("BANK");
                    String number = rst.getString("NUMBER");
                    String owner = rst.getString("OWNER");
                    int billDate = rst.getInt("BILL_DATE");
                    int repaymentDate = rst.getInt("REPAYMENT_DATE");
                    if (bank.contains(toMatch) || owner.contains(toMatch) || number.endsWith(toMatch)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("户名：").append(owner).append("\r\n");
                        sb.append("开户行：").append(bank).append("\r\n");
                        sb.append("卡号：").append(CardUtils.formatCardNumber(number)).append("\r\n");
                        sb.append("账单日：每月").append(billDate).append("号\r\n");
                        sb.append("还款日：每月").append(repaymentDate).append("号\r\n");
                        cards.add(sb.toString());
                    }
                }
            }
            if (!cards.isEmpty()) {
                return Optional.of(new Message("", "TEXT",
                        cards.stream().collect(Collectors.joining("\r\n", "@" + msg.getMember() + " ", "")), ""));
            }
        }
        return Optional.of(new Message("", "TEXT", "@" + msg.getMember() + " 没有查询到匹配的信用卡信息", ""));
    }
}
