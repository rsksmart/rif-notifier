CREATE DATABASE IF NOT EXISTS rif_notifier;
CREATE USER IF NOT EXISTS 'notifier_user'@'%' identified by 'Nine2Five@';
GRANT ALL PRIVILEGES ON rif_notifier.* TO 'notifier_user'@'%';
FLUSH PRIVILEGES;
