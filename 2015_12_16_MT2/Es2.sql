--for every product category and customer country,
--the store with the highest proportion of sales over the total sales
--of that category and country.
--Result must be ordered by proportion descending
--, SUM(store_sales) OVER(PARTITION BY product_category, country) AS total_sales
WITH v1 AS
(
	SELECT product_category, country, store_id,
		SUM(store_sales)/SUM(SUM(store_sales)) OVER(PARTITION BY product_category, country) as ratio
	FROM sales_fact s, product p, product_class pc, customer c
	WHERE s.product_id = p.product_id AND p.product_class_id = pc.product_class_id AND s.customer_id = c.customer_id
	GROUP BY product_category, country, store_id
),
v2 AS
(
	SELECT *, ROW_NUMBER() OVER (PARTITION BY product_category, country ORDER BY ratio DESC) AS my_rank
	FROM v1
)
SELECT product_category, country, store_id, ratio
FROM v2
WHERE my_rank = 1
ORDER BY ratio DESC