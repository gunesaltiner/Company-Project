package org.ozyegin.cs.repository;

import java.sql.Date;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.ozyegin.cs.entity.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionHistoryRepository extends JdbcDaoSupport {

  private final RowMapper<Pair> pairMapper = (resultSet, i) -> new Pair(
      resultSet.getString(1),
      resultSet.getInt(2));

  private final RowMapper<String> stringMapper = (resultSet, i) -> resultSet.getString(1);

  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public List<Pair> query1() {

      List<Pair> ids = Objects.requireNonNull(getJdbcTemplate()).query(" SELECT T.company, T.product_id FROM transaction_table T " +
              "GROUP BY T.company, T.product_id  " +
              "HAVING SUM(T.amount)>(SELECT SUM(T1.amount)  " +
                                    "FROM transaction_table T1" +
                                    " WHERE T.company=T1.company AND T.product_id<>T1.product_id)",pairMapper);
    return ids;
  }

  public List<String> query2(Date start, Date end) {

      List<String> company_date = Objects.requireNonNull(getJdbcTemplate().query("SELECT C.name  FROM transaction_table T ,company C  " +
              "GROUP BY  C.name " +
              "EXCEPT " +
              "SELECT  T1.company " +
              "FROM transaction_table T1, company C1   " +
              "WHERE  C1.name=T1.company AND T1.order_date >= ? and T1.order_date <= ?  ", new Object[]{start, end}, stringMapper));

      return company_date;
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM transaction_table");
  }
}
