create table if not exists sample(
    id serial PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    data text,
    value int default 0
    );
create table if not exists zip_city(
    zip int PRIMARY KEY,
    city VARCHAR(64)
    );

create table if not exists company(
    name VARCHAR(64) PRIMARY KEY ,
    zip int ,
    country VARCHAR(64) ,
    streetInfo VARCHAR(64) ,
    phoneNumber VARCHAR(64) not null  unique,
    foreign key (zip) references zip_city(zip)
    );

create table if not exists emails(
    name varchar(64),
    email varchar(64),
    primary key (name, email),
    foreign key (name) references company(name)
    );


create table if not exists product(
    name VARCHAR(64) not null ,
    id serial PRIMARY KEY,
    description VARCHAR(64) ,
    brandName VARCHAR(64)

    );

create table if not exists produce(
    id serial PRIMARY KEY,
    company VARCHAR(64) not null,
    product_id  int not null ,
    capacity int,
    foreign key (company) references company(name),
    foreign key (product_id) references product(id)
    );


create table if not exists product_order(
        id serial PRIMARY KEY,
        company VARCHAR(64) not null,
        product_id int not null,
        amount int,
        order_date timestamp with time zone,
        foreign key (company) references company(name),
        foreign key (product_id) references product(id)
    );


create table if not exists transaction_table(
       id serial PRIMARY KEY,
       company VARCHAR(64) not null,
       product_id int not null,
       amount int,
       order_date timestamp with time zone,
       foreign key (company) references company(name),
       foreign key (product_id) references product(id)
);


create FUNCTION sample_trigger() RETURNS TRIGGER AS
    '
    BEGIN
        IF (SELECT value FROM sample where id = NEW.id ) > 1000
        THEN
            RAISE SQLSTATE ''23503'';
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

create TRIGGER sample_value AFTER insert ON sample
    FOR EACH ROW EXECUTE PROCEDURE sample_trigger();

CREATE FUNCTION ordered_trigger() RETURNS TRIGGER AS
    '
    BEGIN
        IF (Select capacity
            FROM produce p2,company c1, product p1
            WHERE p2.product_id=p1.id AND p2.company=c1.name AND
                    p2.capacity < (SELECT SUM(amount)
                                   FROM product_order o1
                                   WHERE o1.company=c1.name AND o1.product_id=p1.id
                ))
        THEN
            RAISE SQLSTATE ''23503'';
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER order_amount AFTER insert ON product_order
    FOR EACH ROW EXECUTE PROCEDURE ordered_trigger();