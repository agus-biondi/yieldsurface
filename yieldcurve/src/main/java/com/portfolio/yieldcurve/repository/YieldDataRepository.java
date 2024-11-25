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
import java.util.Optional;

@Repository
public class YieldDataRepository {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.yield-data-table-name}")
    private String tableName;

    public Optional<LocalDate> getMostRecentUpdateDate() {

        String query = new StringBuilder("SELECT MAX(date) FROM ").append(tableName).toString();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                java.sql.Date sqlDate = resultSet.getDate(1);
                if (sqlDate != null ) {
                    return Optional.of(sqlDate.toLocalDate());
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception getting most recent update: " + e);
        }
        return Optional.empty();
    }

    public void saveYieldCurveData(List<Object[]> yieldDataUpdates) {
        String sql = "INSERT INTO " + tableName + " (date, maturity, yield) " +
                "VALUES (?, CAST(? AS maturity_enum), ?)";

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
