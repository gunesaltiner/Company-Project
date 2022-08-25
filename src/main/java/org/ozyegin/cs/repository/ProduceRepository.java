package org.ozyegin.cs.repository;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class ProduceRepository extends JdbcDaoSupport {

    private final RowMapper<Integer> intRowMapper = ((resultSet, i) -> resultSet.getInt(1));

    @Autowired
    public void setDatasource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    public Integer produce(String company, int product, int capacity) {

        List<Integer> idList = Objects.requireNonNull(getJdbcTemplate()).query("SELECT id FROM produce",intRowMapper);

        Objects.requireNonNull(getJdbcTemplate()).update("INSERT INTO produce (company, product_id, capacity) VALUES(?,?,?)",company,product,capacity);

        List<Integer> newIdList = Objects.requireNonNull(getJdbcTemplate()).query("SELECT id FROM produce",intRowMapper);

        newIdList.removeAll(idList);

        return newIdList.get(0);
    }

    public void delete(int produceId) throws Exception {
        if (Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM produce WHERE id=?",
                produceId) != 1) {
            throw new Exception("Sample Delete is failed!");
        }  }

    public void deleteAll() {
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM produce");
    }
}