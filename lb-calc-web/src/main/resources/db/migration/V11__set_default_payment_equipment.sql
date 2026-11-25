-- Назначаем базовые физические модели для обоих вариантов оплаты.
-- PIN не имеет отдельной модели оборудования: для него используется дисплей.
UPDATE equipment
SET is_default = TRUE
WHERE (functional_type = 'PAYMENT' AND purpose = 'CARDS'
       AND category = 'PAYMENT_TERMINAL' AND name = 'Банковский терминал')
   OR (functional_type = 'PAYMENT' AND purpose = 'CASH'
       AND category = 'CASH_ACCEPTOR' AND name = 'Купюроприёмник');
