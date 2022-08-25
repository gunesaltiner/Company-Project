package org.ozyegin.cs.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Repository
public class TransactionRepository extends JdbcDaoSupport {

    private final RowMapper<Integer> intRowMapper = ((resultSet, i) -> resultSet.getInt(1));

    @Autowired
    public void setDatasource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    public Integer order(String company, int product, int amount, Date createdDate) {
        List<Integer> idlist = Objects.requireNonNull(getJdbcTemplate()).query("SELECT id FROM product_order",intRowMapper);

        Objects.requireNonNull(getJdbcTemplate()).update("INSERT INTO product_order (company, product_id, amount, order_date) VALUES(?,?,?,?)",company,product,amount,createdDate);

        List<Integer> newIdList = Objects.requireNonNull(getJdbcTemplate()).query("SELECT id FROM product_order",intRowMapper);

        getJdbcTemplate().update("INSERT INTO transaction_table (company, product_id, amount, order_date) VALUES(?,?,?,?)",company,product,amount,createdDate);

        newIdList.removeAll(idlist);

        return newIdList.get(0);
    }

    public void delete(int transactionId) throws Exception {
        if (Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM product_order WHERE id=?",transactionId) != 1) {
            throw new Exception(" SAMPLE DELETE IS FAILED !");
        }
    }

    public void deleteAll() {
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM product_order");
    }
}