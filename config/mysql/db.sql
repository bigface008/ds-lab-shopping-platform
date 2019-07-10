CREATE TABLE IF NOT EXISTS commodity (
  id INT UNSIGNED AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  price DECIMAL(65,30),
  currency VARCHAR(100) NOT NULL,
  inventory INT UNSIGNED,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS result (
  id VARCHAR(100) NOT NULL,
  user_id VARCHAR(100) NOT NULL,
  initiator VARCHAR(100) NOT NULL,
  success BOOLEAN,
  paid DECIMAL(65,30),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS amount (
  currency VARCHAR(100) NOT NULL,
  amount DECIMAL(65,30),
  PRIMARY KEY (currency)
);

INSERT INTO commodity (name, price, currency, inventory) VALUES ('A', 66.0, 'USD', 123);
INSERT INTO commodity (name, price, currency, inventory) VALUES ('B', 88.0, 'CNY', 789);
INSERT INTO commodity (name, price, currency, inventory) VALUES ('C', 99.0, 'JPY', 666);
INSERT INTO commodity (name, price, currency, inventory) VALUES ('D', 45.5, 'EUR', 333);

INSERT INTO result (id, user_id, initiator, success, paid) VALUES ('1235234', '12345', 'RMB', 1, 792.15);

INSERT INTO amount (currency, amount) VALUES ('USD', 0);
INSERT INTO amount (currency, amount) VALUES ('CNY', 0);
INSERT INTO amount (currency, amount) VALUES ('JPY', 0);
INSERT INTO amount (currency, amount) VALUES ('EUR', 0);