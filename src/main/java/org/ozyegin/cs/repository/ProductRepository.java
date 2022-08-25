package org.ozyegin.cs.repository;

import java.util.*;
import javax.sql.DataSource;

import org.ozyegin.cs.entity.Company;
import org.ozyegin.cs.entity.Product;
import org.ozyegin.cs.entity.Sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository extends JdbcDaoSupport {
    final int batchSize = 10;

    final String createPS = "INSERT INTO product (name, description, brandName) VALUES(?,?,?)";
    final String updatePS = "UPDATE product SET name=?, description=?, brandName=? WHERE id=?";
    final String getPS = "SELECT * FROM product WHERE id IN (:ids)";
    final String deleteAllPS = "DELETE FROM product";
    final String deletePS = "DELETE FROM product WHERE id =?";


    private final RowMapper<Integer> intRowMapper = (resultSet, i) -> resultSet.getInt(1);


    @Autowired
    public void setDatasource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    private final RowMapper<Product> productRowMapper = (resultSet, i) -> {
        Product product = new Product();
        product.id(resultSet.getInt("id"));
        product.name(resultSet.getString("name"));
        product.description(resultSet.getString("description"));
        product.brandName(resultSet.getString("brandName"));
        return product;
    };

    public Product find(int id)  {
        return Objects.requireNonNull(getJdbcTemplate()).queryForObject("SELECT * FROM product WHERE id=?",
                new Object[] {id}, productRowMapper);
    }

    public List<Product> findMultiple(List<Integer> ids) {

        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        } else {
            Map<String, List<Integer>> newlist = new HashMap<>() {
                {
                    this.put("ids", new ArrayList<>(ids));
                }
            };
            var template = new NamedParameterJdbcTemplate(Objects.requireNonNull(getJdbcTemplate()));
            return template.query(getPS, newlist, productRowMapper);
        }
    }

    public List<Product> findByBrandName(String brandName) {
        return Objects.requireNonNull(getJdbcTemplate()).query("SELECT * FROM product WHERE brandName = ?",
                new Object[] {brandName}, productRowMapper);
    }

    public List<Integer> create(List<Product> products) {
        List<Integer> idlist = Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT id FROM product", intRowMapper);

        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(createPS, products,
                batchSize,
                (ps, product) -> {
                    ps.setString(1, product.getName());
                    ps.setString(2, product.getDescription());
                    ps.setString(3, product.getBrandName());
                });

        List<Integer> newidslist = Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT id FROM product", intRowMapper);

        newidslist.removeAll(idlist);
        return newidslist;
    }

    public void update(List<Product> products) {
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(updatePS,products,batchSize,
                (ps, product) -> {
                    ps.setString(1,product.getName());
                    ps.setString(2, product.getDescription());
                    ps.setString(3, product.getBrandName());
                    ps.setInt(4, product.getId());
                });
    }

    public void delete(List<Integer> ids) {
        int id;

        for (int i=0 ;i<ids.size() ;i++){
            id= ids.get(i);
            if (Objects.requireNonNull(getJdbcTemplate()).update(deletePS,
                    id) != 1);
        }
    }

    public void deleteAll() {
        Objects.requireNonNull(getJdbcTemplate()).update(deleteAllPS);
    }
}
