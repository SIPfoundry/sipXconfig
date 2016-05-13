update phone set is_supported = false, serial_number = serial_number || '-unsupported' where bean_id='snom' and is_supported = true;
