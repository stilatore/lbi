WITH prev_month AS
(
	SELECT s.customer_id, t.month_of_year, t.the_month, (t.month_of_year -1) as prev
	FROM sales_fact_1998 s, time_by_day t
	WHERE s.time_id = t.time_id
),
vista AS
(
	SELECT v1.customer_id, v2.customer_id as cust_toCheck, v1.month_of_year, v1.the_month
	FROM prev_month v1 LEFT OUTER JOIN prev_month v2 ON v1.customer_id = v2.customer_id and v1.month_of_year = v2.prev
)
SELECT customer_id, the_month
FROM vista
WHERE cust_toCheck IS NULL
GROUP BY customer_id, the_month
ORDER BY customer_id, the_month
