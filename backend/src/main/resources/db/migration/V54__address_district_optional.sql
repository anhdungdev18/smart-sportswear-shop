-- Vietnam's 2025 administrative reform dropped the district (quan/huyen) tier -
-- addresses are now Tinh/Thanh pho -> Phuong/Xa directly. The column stays for
-- legacy data and free-form staff entry, it just stops being required.
alter table addresses alter column district drop not null;
alter table shipments alter column district drop not null;
