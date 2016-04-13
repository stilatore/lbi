WITH dev AS
(
	SELECT time_id, SUM(store_sales) - AVG(SUM(store_sales)) OVER() AS dev_sales 
	FROM sales_fact
	GROUP BY time_id
),
running AS
(
	SELECT time_id, dev_sales, SUM(dev_sales) OVER(ORDER BY time_id ROWS UNBOUNDED PRECEDING) AS running_dev
	FROM dev
)
SELECT TOP 1 r1.time_id, r2.time_id, r2.running_dev - r1.running_dev + r1.dev_sales AS summation
FROM running r1, running r2
ORDER BY r2.running_dev - r1.running_dev + r1.dev_sales DESC