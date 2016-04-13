SELECT customer_id, COUNT(DISTINCT s.time_id) as fpw
FROM sales_fact_1998 s JOIN time_by_day t ON s.time_id = t.time_id
WHERE the_day IN ('Saturday','Sunday')
GROUP BY customer_id
ORDER BY customer_id