insert into users (user_id, user_name, user_type) values
  (1000, 'kuku', 'C');
  
insert into phone (serial_number, phone_id, description, bean_id, model_id) values
  ('999123456789', 1000, 'unittest-sample phone1', 'testPhone', 'testPhoneModel');

insert into line (line_id, phone_id, position, user_id) values
  (1000, 1000, 0, 1000);
