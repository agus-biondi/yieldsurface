package com.portfolio.yieldcurve.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class YieldDataRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(YieldDataRepository.class);

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.yield-data-table-name}")
    private String tableName;


    /**
     * Retrieves yield curve data grouped by the specified period.
     *
     * @param startDate Start date in YYYY-MM-DD format.
     * @param endDate   End date in YYYY-MM-DD format.
     * @param groupBy   Grouping period: day, week, month, or year.
     * @return A map of grouped dates to yield values.
     */
    public Map<String, List<Float>> getYieldCurveData(String startDate, String endDate, String groupBy) {

        String query = buildGetYieldCurveQuery(groupBy);

        Map<String, List<Float>> yieldDataByDate = new LinkedHashMap<>();

        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDate(1, Date.valueOf(startDate));
            preparedStatement.setDate(2, Date.valueOf(endDate));

            LOGGER.info("Executing query: {}", preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String dateGroup = resultSet.getString("date_group");
                    BigDecimal[] yieldsArray = (BigDecimal[]) resultSet.getArray("avg_yield").getArray();

                    List<Float> yields = convertBigDecimalArrayToFloatList(yieldsArray);
                    yieldDataByDate.put(dateGroup, yields);
                }
            }
        } catch (SQLException e) {
            String message = "Error fetching yield curve data for date range: " + startDate + " to " + endDate;
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }

        return yieldDataByDate;

    }


    /**
     * Retrieves the most recent update date in the yield data table.
     *
     * @return An Optional containing the most recent date or empty if no data exists.
     */
    public Optional<LocalDate> getMostRecentUpdateDate() {

        String query = String.format("SELECT MAX(date) FROM %s", tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                Date sqlDate = resultSet.getDate(1);
                return Optional.ofNullable(sqlDate).map(Date::toLocalDate);
            }
        } catch (SQLException e) {
            LOGGER.error("Error fetching the most recent update date", e);
            throw new RuntimeException("Error fetching the most recent update date", e);
        }
        return Optional.empty();
    }

    /**
     * Saves yield curve data to the database.
     *
     * @param yieldDataUpdates A list of rows where each row contains [LocalDate, String, BigDecimal].
     */
    public void saveYieldCurveData(List<Object[]> yieldDataUpdates) {
        String sql = String.format("INSERT INTO %s (date, maturity, yield) " +
                "VALUES (?, CAST(? AS maturity_enum), ?)", tableName);

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
            LOGGER.error("Error saving yield curve data", e);
            throw new RuntimeException("Error saving yield curve data", e);
        }
    }


    /**
     * Converts a SQL Array to a List of Float values.
     *
     * @param array The SQL Array to convert.
     * @return A List of Float values.
     * @throws SQLException If an error occurs while accessing the array.
     */
    private List<Float> convertBigDecimalArrayToFloatList(BigDecimal[] array) throws SQLException {
        return Arrays.stream(array)
                .map(bd -> bd == null ? null : bd.floatValue())
                .toList();
    }


    private String buildGetYieldCurveQuery(String groupBy) {
        return String.format("""
                WITH dates AS (
                    SELECT DISTINCT date::DATE
                    FROM %s
                    WHERE date BETWEEN ? AND ?
                ),
                maturities AS (
                    SELECT unnest(enum_range(NULL::maturity_enum)) AS maturity
                ),
                all_combinations AS (
                    SELECT
                        d.date,
                        m.maturity
                    FROM dates d
                    CROSS JOIN maturities m
                )
                SELECT
                	date_group,
                	ARRAY_AGG(COALESCE(yield, NULL) ORDER BY maturity) avg_yield
                FROM (

                	SELECT
                		DATE_TRUNC('%s', ac.date)::DATE date_group,
                		ac.maturity,
                		ROUND(AVG(y.yield), 2) yield
                	FROM
                		all_combinations ac
                	LEFT JOIN\s
                		%s y
                	ON\s
                		ac.date = y.date AND
                		ac.maturity = y.maturity

                	GROUP BY
                		DATE_TRUNC('%s', ac.date),
                		ac.maturity
                	ORDER BY\s
                		1,2
                ) avg_yield_data

                GROUP BY
                	date_group
                ORDER BY \s
                	date_group ASC

                    """, tableName, groupBy.toUpperCase(), tableName, groupBy.toUpperCase());
    }


}
