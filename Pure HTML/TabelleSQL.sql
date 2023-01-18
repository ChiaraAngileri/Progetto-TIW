
CREATE TABLE 'User' (
'id' int AUTO_INCREMENT primary key,
'name' varchar(45) not null,
'surname' varchar(45) not null,
'username' varchar(45) unique not null, 
'e-mail' varchar(60) unique not null,
'password' varchar(45) not null
)



CREATE TABLE 'BankAccount' (
'id' int(11)  AUTO_INCREMENT primary key,
'balance'  decimal(10, 2) not null default '0.00',
'user_id' int not null,
'name' varchar(45) not null,
costraint 'userAccount' foreign key ('user_id') 
			references 'User'('id') on update cascade 
									on delete cascade
)



CREATE TABLE 'MoneyTransfer' (
'id' int(11) AUTO_INCREMENT primary key,
'date' timestamp not null default CURRENT_TIMESTAMP,
'bankAccountOrigin' int not null,
'bankAccountDestination' int not null,
'amount' decimal(10, 2) not null,
'reason' varchar(60) not null,
'origin_initial_amount' decimal(10, 2) not null,
'destination_initial_amount' decimal(10, 2) not null,
primary key(timestamp, bankAccountOrigin, bankAccountDestination),
costraint 'sourceAccount' foreign key ('bankAccountOrigin')
			references 'BankAccount'('id') on update cascade
										   on delete no action,
costraint 'destinationAccount' foreign key ('bankAccountDestination')
			references 'BankAccount'('id') on update cascade
										   on delete no action
)



 