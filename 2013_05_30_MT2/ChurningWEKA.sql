WITH prev_month AS
(
	SELECT s.customer_id, t.month_of_year, t.the_month, (t.month_of_year -1) as prev
	FROM sales_fact_1998 s JOIN time_by_day t ON s.time_id = t.time_id
),
vista AS
(
	SELECT v1.customer_id, v2.customer_id as cust_toCheck, v1.month_of_year, v1.the_month
	FROM prev_month v1 LEFT OUTER JOIN prev_month v2 ON v1.customer_id = v2.customer_id and v1.month_of_year = v2.prev
)
SELECT v.customer_id, the_month, city, state_province, country, birthdate, marital_status, yearly_income, gender, total_children,
		num_children_at_home, education, date_accnt_opened, member_card, occupation, houseowner, num_cars_owned,
	CASE WHEN cust_toCheck IS NULL THEN 'yes' ELSE 'no' END AS churning
FROM vista v JOIN customer c ON v.customer_id = c.customer_id
GROUP BY v.customer_id, cust_toCheck, the_month, city, state_province, country, birthdate, marital_status, yearly_income, gender, total_children,
		num_children_at_home, education, date_accnt_opened, member_card, occupation, houseowner, num_cars_owned
ORDER BY v.customer_id, the_month