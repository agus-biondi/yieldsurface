package com.portfolio.yieldcurve.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class YieldDataRepository {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.yield-data-table-name}")
    private String tableName;

    public LocalDate getMostRecentUpdateDate() {

        String query = new StringBuilder("SELECT MAX(date) FROM ").append(tableName).toString();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDate(1).toLocalDate();
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e);
        }
        return null;
    }

    public void saveYieldCurveData(List<Object[]> yieldDataUpdates) {
        String sql = "INSERT INTO yield_curve (date, maturity, yield) " +
                "VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Object[] row : yieldDataUpdates) {
                statement.setDate(1, java.sql.Date.valueOf((LocalDate) row [0]));
                statement.setString(2, (String) row[1]);
                statement.setObject(3, row[2]);

                statement.addBatch();
            }

            statement.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving yield curve data", e);
        }
    }

}
