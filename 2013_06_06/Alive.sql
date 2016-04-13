WITH v1 AS
(
	SELECT customer_id, MAX(month_of_year) AS max_month
	FROM sales_fact s JOIN time_by_day t ON s.time_id = t.time_id
	GROUP BY customer_id
)
SELECT customer_id, the_month,
	CASE WHEN month_of_year <= max_month THEN 'alive' ELSE 'not alive' END AS alive
FROM v1, time_by_day
GROUP BY customer_id, month_of_year, the_month, max_month
ORDER BY customer_id, month_of_year

--Oppure

WITH v1 AS
(
	SELECT customer_id, MAX(month_of_year) AS max_month
	FROM sales_fact s JOIN time_by_day t ON s.time_id = t.time_id
	GROUP BY customer_id
)
SELECT customer_id, the_month, 'alive'
FROM v1 RIGHT JOIN time_by_day ON month_of_year <= max_month
GROUP BY customer_id, month_of_year, the_month
ORDER BY customer_id, month_of_year