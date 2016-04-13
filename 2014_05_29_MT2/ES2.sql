--For every country, the percentage of store sales of top 10 customers in
--that country over the total sales to customers of that country

WITH vista AS
(
	SELECT c.country, SUM(store_sales) AS total_sales, 
		DENSE_RANK() OVER(PARTITION BY c.country ORDER BY SUM(store_sales) DESC) AS DenseRank
	FROM sales_fact s JOIN customer c ON c.customer_id = s.customer_id
	GROUP BY c.country, c.customer_id
),
total AS
(
	SELECT c.country, SUM(store_sales) AS total_sales
	FROM sales_fact s JOIN customer c ON c.customer_id = s.customer_id
	GROUP BY c.country
),
top10 AS
(
	SELECT country, SUM(total_sales) AS top10_sales
	FROM vista
	WHERE DenseRank <= 10
	GROUP BY country
)
SELECT t.country, top10_sales*100/total_sales AS ratio
FROM total t JOIN top10 tp ON t.country = tp.country