WITH travellers AS
(
	SELECT DISTINCT c.customer_id, 'yes' AS traveller
	FROM sales_fact s, customer c, store st
	WHERE s.customer_id = c.customer_id AND s.store_id = st.store_id AND c.city != st.store_city
)
SELECT DISTINCT c.customer_id, city, birthdate, marital_status, yearly_income, gender,
		total_children, num_children_at_home, education, date_accnt_opened, member_card, occupation, houseowner, num_cars_owned,
		CASE WHEN traveller IS NULL THEN 'no' ELSE 'yes' END AS traveller
FROM customer c LEFT OUTER JOIN  travellers t ON c.customer_id = t.customer_id
ORDER BY c.customer_id

