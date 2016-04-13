WITH v1 AS
(
	SELECT s.customer_id, t.the_year, t.the_month, SUM(store_sales) AS total_sales
	FROM sales_fact s JOIN time_by_day t ON s.time_id = t.time_id
	GROUP BY s.customer_id, t.the_year, t.the_month
), 
v2 AS
(
	SELECT the_year, the_month, COUNT(*) AS non_sundays
	FROM time_by_day
	WHERE the_day != 'Sunday'
	GROUP BY the_year, the_month
)
SELECT (lname + ' ' + fname) AS fullname, v1.the_year, v1.the_month, v1.total_sales, v2.non_sundays, (v1.total_sales/v2.non_sundays) AS MPD
FROM v1 , v2, customer c
WHERE v1.the_year = v2.the_year AND v1.the_month = v2.the_month AND c.customer_id = v1.customer_id
ORDER BY MPD DESC

