-- Insert users
INSERT INTO users (name, email, password)
VALUES ('John Doe', 'john.doe@example.com', 'password123');

INSERT INTO users (name, email, password)
VALUES ('Jane Smith', 'jane.smith@example.com', 'password456');

-- Insert categories
INSERT INTO categories (name, description)
VALUES ('Food', 'Expenses related to groceries and eating out');

INSERT INTO categories (name, description)
VALUES ('Transport', 'Expenses related to public transport, taxis, and car maintenance');

INSERT INTO categories (name, description)
VALUES ('Housing', 'Rent, utility bills, and home improvements');

INSERT INTO categories (name, description)
VALUES ('Entertainment', 'Movies, concerts, and other leisure activities');

-- Insert expenses
INSERT INTO expenses (description, amount, date, user_id)
VALUES ('Grocery shopping', 75.50, '2025-05-01', 1);

INSERT INTO expenses (description, amount, date, user_id)
VALUES ('Movie tickets', 25.00, '2025-05-02', 1);

INSERT INTO expenses (description, amount, date, user_id)
VALUES ('Electricity bill', 120.75, '2025-05-03', 2);

INSERT INTO expenses (description, amount, date, user_id)
VALUES ('Fuel', 60.00, '2025-05-04', 2);

-- Link expenses to categories
INSERT INTO expense_categories (expense_id, category_id)
VALUES (1, 1);

INSERT INTO expense_categories (expense_id, category_id)
VALUES (2, 4);

INSERT INTO expense_categories (expense_id, category_id)
VALUES (3, 3);

INSERT INTO expense_categories (expense_id, category_id)
VALUES (4, 2);