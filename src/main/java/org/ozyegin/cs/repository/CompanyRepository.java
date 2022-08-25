package org.ozyegin.cs.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.ozyegin.cs.entity.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyRepository extends JdbcDaoSupport {


    @Autowired
    public void setDatasource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }


    private final RowMapper<Company> companyRowMapper = (resultSet, i) -> new Company()
            .name(resultSet.getString("name"))
            .zip(resultSet.getInt("zip"))
            .country(resultSet.getString("country"))
            .streetInfo(resultSet.getString("streetInfo"))
            .phoneNumber(resultSet.getString("phoneNumber"));

    private final RowMapper<Company> zipRowMapper = ((resultSet, i) -> new Company()
            .zip(resultSet.getInt("zip"))
            .city(resultSet.getString("city"))
    );


    private final RowMapper<String> stringRowMapper = (resultSet, i) -> resultSet.getString(1);


    public Company find(String name) {

        Company company = Objects.requireNonNull(getJdbcTemplate()).queryForObject("SELECT * FROM company WHERE name=?",
                new Object[] {name}, companyRowMapper);



        if (company != null) {
            company.setCity(Objects.requireNonNull(getJdbcTemplate()).queryForObject("SELECT city FROM zip_city WHERE zip=?",
                    new Object[] {company.getZip()}, stringRowMapper));
        }

        company.setE_mails(Objects.requireNonNull(getJdbcTemplate()).query("SELECT email FROM emails WHERE name=?",
                new Object[] {company.getName()}, stringRowMapper));

        return company;
    }

    public List<Company> findByCountry(String country) {
        ArrayList<Company> companies = new ArrayList<>();
        List<String> companyNames = Objects.requireNonNull(getJdbcTemplate()).
                query("SELECT name FROM company WHERE country=?", new Object[] {country}, stringRowMapper);
        for (String name: companyNames) {
            companies.add(find(name));
        }
        return companies;
    }

    public String create(Company company) throws Exception {
        try {
            String city = Objects.requireNonNull(getJdbcTemplate()).
                    queryForObject("SELECT city FROM zip_city WHERE zip=?", new Object[] {company.getZip()},stringRowMapper);
            if (!city.equals(company.getCity())) {
                throw new Exception("Sample not found!");
            }
        } catch (EmptyResultDataAccessException e){
            Objects.requireNonNull(getJdbcTemplate())
                    .update("INSERT INTO zip_city (zip, city) VALUES (?, ?)", company.getZip(), company.getCity());
        }

        Objects.requireNonNull(getJdbcTemplate())
                .update("INSERT INTO company (name, country, zip, streetInfo, phoneNumber) VALUES (?,?,?,?,?)",
                        company.getName(), company.getCountry(), company.getZip(), company.getStreetInfo(),
                        company.getPhoneNumber());

        for (String email: company.getE_mails()){
            Objects.requireNonNull(getJdbcTemplate())
                    .update("INSERT INTO emails (name, email) VALUES (?, ?)", company.getName(), email);
        }
        return company.getName();
    }

    public String delete(String name) {
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM emails WHERE name=?", name);
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM company WHERE name=?", name);
        return name;
    }

    public void deleteAll() {
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM emails");
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM company");
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM zip_city");
    }
}