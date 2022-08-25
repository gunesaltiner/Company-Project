zip_city (zip,city)
PRIMARY KEY (zip_city) = <zip>

company (name,zip,country,streetInfo,phoneNumber)
PRIMARY KEY (company) = <name>
CANDIDATE KEY (company) = <phoneNumber>

emails (name,email)
PRIMARY KEY (emails) = <name,email>
FOREIGN KEY Emails (name) REFERENCES company(name)

Product(id,name,description,brandName)
PRIMARY KEY (product) = <id>

Produce (id,company,product_id,capacity)
PRIMARY KEY (produce) = <id>
FOREIGN KEY produce (company) REFERENCES company (name)
FOREIGN KEY produce (product_id) REFERENCES product (id)

product_order (id,company,product_id,amount,order_date)
PRIMARY KEY (product_order) = <id>
FOREIGN KEY (product_order) (company) REFERENCES company (name)
FOREIGN KEY (product_order) (id) REFERENCES Product (id)

transaction_table(id,company,product_id,amount, order_date)
PRIMARY KEY (transaction_table) = <id>
