-- Insert Sample Billers
INSERT INTO biller (code, name, email) VALUES
                                           ('BILL_12345', 'Fawry Merchant A', 'merchantA@fawry.com'),
                                           ('BILL_67890', 'Fawry Merchant B', 'merchantB@fawry.com');

-- Insert Sample Gateways (matching the task document)
INSERT INTO gate_way (code, name, min_transaction, max_transaction, commission_fixed, commission_amount, daily_limit, processing_time, is_active) VALUES
                                                                                                                                                      ('gateway_1', 'Gateway 1', 10, 5000, 2, 0.015, 50000, 0, true),
                                                                                                                                                      ('gateway_2', 'Gateway 2', 100, 999999999, 5, 0.008, 200000, 86400, true),
                                                                                                                                                      ('gateway_3', 'Gateway 3', 50, 10000, 0, 0.025, 100000, 7200, true);

-- Insert Gateway Availability
-- Gateway 1: 24/7
INSERT INTO gateway_availability (gateway_id, day_week, is_24_7) VALUES
                                                                     (1, 'MONDAY', true),
                                                                     (1, 'TUESDAY', true),
                                                                     (1, 'WEDNESDAY', true),
                                                                     (1, 'THURSDAY', true),
                                                                     (1, 'FRIDAY', true),
                                                                     (1, 'SATURDAY', true),
                                                                     (1, 'SUNDAY', true);

-- Gateway 2: Sun-Thu 9AM-5PM
INSERT INTO gateway_availability (gateway_id, day_week, start_time, end_time, is_24_7) VALUES
                                                                                           (2, 'SUNDAY', '09:00:00', '17:00:00', false),
                                                                                           (2, 'MONDAY', '09:00:00', '17:00:00', false),
                                                                                           (2, 'TUESDAY', '09:00:00', '17:00:00', false),
                                                                                           (2, 'WEDNESDAY', '09:00:00', '17:00:00', false),
                                                                                           (2, 'THURSDAY', '09:00:00', '17:00:00', false);

-- Gateway 3: 24/7
INSERT INTO gateway_availability (gateway_id, day_week, is_24_7) VALUES
                                                                     (3, 'MONDAY', true),
                                                                     (3, 'TUESDAY', true),
                                                                     (3, 'WEDNESDAY', true),
                                                                     (3, 'THURSDAY', true),
                                                                     (3, 'FRIDAY', true),
                                                                     (3, 'SATURDAY', true),
                                                                     (3, 'SUNDAY', true);