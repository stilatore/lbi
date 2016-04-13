WITH travel AS
(
	SELECT s.customer_id, lname, fname, SUM(store_sales) AS travel_sales
	FROM sales_fact_1998 s, store st, customer c
	WHERE s.store_id = st.store_id AND s.customer_id = c.customer_id
	AND st.store_city != c.city
	GROUP BY s.customer_id, lname, fname
),
total AS
(
	SELECT customer_id, SUM(store_sales) AS total_sales
	FROM sales_fact_1998
	GROUP BY customer_id
)
SELECT lname, fname, total_sales, (travel_sales/total_sales) AS ratio
FROM travel tr JOIN total tot ON tr.customer_id = tot.customer_id