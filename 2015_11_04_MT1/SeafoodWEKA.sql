WITH sea_sales AS
(
	SELECT DISTINCT c.customer_id, 'yes' AS seafood_buyer
	FROM customer c, product p, product_class pc, sales_fact s
	WHERE c.customer_id = s.customer_id AND s.product_id = p.product_id AND p.product_class_id = pc.product_class_id
	AND pc.product_department = 'Seafood'
)
SELECT s.customer_id, c.city, c.state_province, c.country, c.yearly_income, c.gender, c.total_children, c.occupation, c.houseowner, c.num_cars_owned, 
	SUM(store_sales) AS total_sales,
	CASE WHEN seafood_buyer IS NULL THEN 'no' ELSE 'yes' END AS seafood_buyer
FROM sales_fact s LEFT OUTER JOIN sea_sales ss ON s.customer_id = ss.customer_id JOIN customer c ON s.customer_id = c.customer_id
GROUP BY s.customer_id, seafood_buyer, c.city, c.state_province, c.country, c.yearly_income, c.gender, c.total_children, c.occupation, c.houseowner, c.num_cars_owned
ORDER BY s.customer_id