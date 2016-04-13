WITH v1 AS
(
	SELECT customer_id, the_day, SUM(store_sales) AS total_sales
	FROM sales_fact_1998 s JOIN time_by_day t ON s.time_id = t.time_id
	GROUP BY customer_id, the_day
),
v2 AS
(
	SELECT customer_id, MAX(total_sales) AS vday_sales
	FROM v1
	GROUP BY customer_id
)
SELECT v2.customer_id, the_day, vday_sales
FROM v1 JOIN v2 ON v1.customer_id = v2.customer_id AND total_sales = vday_sales
ORDER BY v2.customer_id, the_day