WITH view_m_i AS
(
	SELECT c.country, c.city, COUNT(DISTINCT c.customer_id) AS m_i
	FROM sales_fact s JOIN customer c ON s.customer_id = c.customer_id
	WHERE c.gender = 'F'
	GROUP BY c.country, c.city
),
view_t_i AS
(
	SELECT c.country, c.city, COUNT(DISTINCT c.customer_id) AS t_i
	FROM sales_fact s JOIN customer c ON s.customer_id = c.customer_id
	GROUP BY c.country, c.city
),
view_ratio AS
(
	SELECT m.country, m.city, m_i, t_i, (m_i*1.0/t_i*1.0) AS ratio
	FROM view_m_i m JOIN view_t_i t ON m.city = t.city
),
view_ncity AS
(
	SELECT country, COUNT(DISTINCT city) AS n
	FROM customer
	GROUP BY country
)
SELECT r.country, SUM(ratio)/n
FROM view_ratio r JOIN view_ncity nc ON r.country = nc.country
GROUP BY r.country, n