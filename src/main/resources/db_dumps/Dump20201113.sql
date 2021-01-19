-- MySQL dump 10.13  Distrib 8.0.22, for osx10.13 (x86_64)
--
-- Host: localhost    Database: rif_notifier_dump
-- ------------------------------------------------------
-- Server version	8.0.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chainaddress_event`
--

DROP TABLE IF EXISTS `chainaddress_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chainaddress_event` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nodehash` varchar(250) DEFAULT NULL,
  `event_name` varchar(45) DEFAULT NULL,
  `chain` varchar(100) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `block` bigint NOT NULL,
  `hashcode` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=772 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chainaddress_event`
--

LOCK TABLES `chainaddress_event` WRITE;
/*!40000 ALTER TABLE `chainaddress_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `chainaddress_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datafetcher`
--

DROP TABLE IF EXISTS `datafetcher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `datafetcher` (
  `id` int NOT NULL AUTO_INCREMENT,
  `last_block` bigint NOT NULL,
  `block_type` enum('RSK_BLOCK','RSK_CHAINADDR_BLOCK') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datafetcher`
--

LOCK TABLES `datafetcher` WRITE;
/*!40000 ALTER TABLE `datafetcher` DISABLE KEYS */;
INSERT INTO `datafetcher` VALUES (1,0,'RSK_BLOCK'),(2,0,'RSK_CHAINADDR_BLOCK');
/*!40000 ALTER TABLE `datafetcher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notif_users`
--

DROP TABLE IF EXISTS `notif_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notif_users` (
  `address` varchar(45) NOT NULL,
  `api_key` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`address`),
  KEY `notif_users_idx_1` (`api_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notif_users`
--

LOCK TABLES `notif_users` WRITE;
/*!40000 ALTER TABLE `notif_users` DISABLE KEYS */;
/*!40000 ALTER TABLE `notif_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `to_address` varchar(45) NOT NULL,
  `timestamp` varchar(45) DEFAULT NULL,
  `sent` tinyint DEFAULT '0',
  `data` varchar(1000) NOT NULL,
  `id_topic` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `notification_idx_1` (`to_address`),
  KEY `notification_idx_2` (`id_topic`)
) ENGINE=InnoDB AUTO_INCREMENT=612 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_log`
--

DROP TABLE IF EXISTS `notification_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `notification_id` int NOT NULL,
  `notification_preference_id` int NOT NULL,
  `sent` tinyint(1) NOT NULL DEFAULT '0',
  `result_text` tinytext,
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `retry_count` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `notification_log_idx_1` (`notification_preference_id`),
  KEY `notification_log_idx_2` (`notification_id`),
  KEY `notification_log_idx_3` (`retry_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_log`
--

LOCK TABLES `notification_log` WRITE;
/*!40000 ALTER TABLE `notification_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_preference`
--

DROP TABLE IF EXISTS `notification_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_preference` (
  `id` int NOT NULL AUTO_INCREMENT,
  `notification_service` enum('SMS','EMAIL','API') DEFAULT NULL,
  `id_subscription` int NOT NULL,
  `id_topic` int DEFAULT NULL,
  `destination` varchar(128) DEFAULT NULL,
  `destination_params` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `notification_preference_idx_1` (`id_subscription`,`id_topic`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_preference`
--

LOCK TABLES `notification_preference` WRITE;
/*!40000 ALTER TABLE `notification_preference` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_preference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_service`
--

DROP TABLE IF EXISTS `notification_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_service` (
  `id` int NOT NULL,
  `type` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_service`
--

LOCK TABLES `notification_service` WRITE;
/*!40000 ALTER TABLE `notification_service` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `raw_data`
--

DROP TABLE IF EXISTS `raw_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `raw_data` (
  `id` varchar(45) NOT NULL,
  `type` varchar(45) NOT NULL,
  `data` varchar(1000) NOT NULL,
  `processed` tinyint(1) NOT NULL DEFAULT '0',
  `block` bigint NOT NULL,
  `id_topic` int DEFAULT NULL,
  `row_hash_code` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `raw_data`
--

LOCK TABLES `raw_data` WRITE;
/*!40000 ALTER TABLE `raw_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `raw_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription` (
  `id` int NOT NULL AUTO_INCREMENT,
  `active_since` date DEFAULT NULL,
  `notification_balance` int NOT NULL,
  `user_address` varchar(45) NOT NULL,
  `status` enum('PENDING','ACTIVE','EXPIRED','COMPLETED') NOT NULL,
  `subscription_plan_id` int NOT NULL,
  `price` bigint NOT NULL,
  `currency` varchar(20) NOT NULL,
  `previous_subscription_id` int DEFAULT NULL,
  `hash` varchar(100) NOT NULL,
  `expiration_date` date NOT NULL,
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `subscription_idx_1` (`user_address`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription_payment`
--

DROP TABLE IF EXISTS `subscription_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_payment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `subscription_id` int NOT NULL,
  `payment_reference` varchar(128) DEFAULT NULL,
  `amount` bigint NOT NULL,
  `payment_date` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `subscription_payment_idx_1` (`subscription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription_payment`
--

LOCK TABLES `subscription_payment` WRITE;
/*!40000 ALTER TABLE `subscription_payment` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription_payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription_plan`
--

DROP TABLE IF EXISTS `subscription_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_plan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `validity` int NOT NULL,
  `notification_quantity` int NOT NULL,
  `notification_preferences` set('SMS','EMAIL','API') NOT NULL,
  `status` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription_plan`
--

LOCK TABLES `subscription_plan` WRITE;
/*!40000 ALTER TABLE `subscription_plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription_price`
--

DROP TABLE IF EXISTS `subscription_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_price` (
  `id` int NOT NULL AUTO_INCREMENT,
  `price` bigint NOT NULL,
  `currency` varchar(20) NOT NULL,
  `subscription_plan_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription_price`
--

LOCK TABLES `subscription_price` WRITE;
/*!40000 ALTER TABLE `subscription_price` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription_price` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `topic`
--

DROP TABLE IF EXISTS `topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` enum('NEW_BLOCK','NEW_TRANSACTIONS','PENDING_TRANSACTIONS','CONTRACT_EVENT') NOT NULL,
  `hash` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic`
--

LOCK TABLES `topic` WRITE;
/*!40000 ALTER TABLE `topic` DISABLE KEYS */;
/*!40000 ALTER TABLE `topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic_params`
--

DROP TABLE IF EXISTS `topic_params`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_params` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_topic` int NOT NULL,
  `param_type` enum('CONTRACT_ADDRESS','EVENT_NAME','EVENT_PARAM') NOT NULL,
  `value` varchar(45) NOT NULL,
  `param_order` int DEFAULT '0',
  `value_type` varchar(45) DEFAULT 'string',
  `is_indexed` tinyint DEFAULT '0',
  `filter` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_params`
--

LOCK TABLES `topic_params` WRITE;
/*!40000 ALTER TABLE `topic_params` DISABLE KEYS */;
/*!40000 ALTER TABLE `topic_params` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_topic`
--

DROP TABLE IF EXISTS `user_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_topic` (
  `id_topic` int NOT NULL,
  `id_subscription` varchar(45) NOT NULL,
  KEY `user_topic_idx_1` (`id_subscription`,`id_topic`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_topic`
--

LOCK TABLES `user_topic` WRITE;
/*!40000 ALTER TABLE `user_topic` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_topic` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-01-12 16:32:24
