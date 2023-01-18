CREATE DATABASE  IF NOT EXISTS `db_moneytransfer` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `db_moneytransfer`;
-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: db_moneytransfer
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bank_account`
--

DROP TABLE IF EXISTS `bank_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00',
  `user_id` int NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `userAccount_idx` (`user_id`),
  CONSTRAINT `userAccount` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bank_account`
--

LOCK TABLES `bank_account` WRITE;
/*!40000 ALTER TABLE `bank_account` DISABLE KEYS */;
INSERT INTO `bank_account` VALUES (2,18.00,1,'viaggi'),(3,1.00,1,'affitto'),(4,10.00,1,'svago'),(5,50.00,2,'spesa'),(6,30.56,3,'festa'),(7,1002.00,4,'macchina'),(8,43.00,5,'telefono'),(10,1111.33,8,'montagna'),(11,0.99,7,'scooter'),(12,111.00,13,'Lavoro'),(13,0.00,14,'Default account'),(14,0.00,1,'lavoro');
/*!40000 ALTER TABLE `bank_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `money_transfer`
--

DROP TABLE IF EXISTS `money_transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `money_transfer` (
  `id` int NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `bank_account_origin` int NOT NULL,
  `bank_account_destination` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `reason` varchar(60) NOT NULL,
  `origin_initial_amount` decimal(10,2) NOT NULL,
  `destination_initial_amount` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sourceAccount_idx` (`bank_account_origin`) /*!80000 INVISIBLE */,
  KEY `destinationAccount_idx` (`bank_account_destination`) /*!80000 INVISIBLE */,
  CONSTRAINT `destinationAccount` FOREIGN KEY (`bank_account_destination`) REFERENCES `bank_account` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `sourceAccount` FOREIGN KEY (`bank_account_origin`) REFERENCES `bank_account` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `money_transfer`
--

LOCK TABLES `money_transfer` WRITE;
/*!40000 ALTER TABLE `money_transfer` DISABLE KEYS */;
INSERT INTO `money_transfer` VALUES (22,'2022-05-01',6,5,1.00,'per la spesa',12.00,24.00),(23,'2022-05-14',3,12,5.00,'Affitto gennaio',17.00,95.00),(24,'2022-05-14',3,12,5.00,'Affitto febbraio',12.00,100.00),(25,'2022-05-14',3,12,5.00,'Affitto marzo',7.00,105.00),(26,'2022-05-14',3,12,1.00,'Acconto',2.00,110.00);
/*!40000 ALTER TABLE `money_transfer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `surname` varchar(45) NOT NULL,
  `username` varchar(45) NOT NULL,
  `email` varchar(60) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'chiara','angileri','chiara','chiara.angileri@mail.polimi.it','Chiara2000!'),(2,'mario','rossi','mario','mario.rossi@libdro.it','mariopassword'),(3,'sofia','ferrari','sofi','sofia@mail.it','sofiapassword'),(4,'giulia','bianchi','giu00','giuliabianchi@hotmail.it','giuliapassword'),(5,'alessandro','esposito','alex10','alex10@gmail.com','alessandropassword'),(7,'leonardo','ricci','leo','leonardo.ricci@mail.polimi.it','leonardopassword'),(8,'lorenzo','romano','lore','lorenzo@dominio.it','lorenzopassword'),(11,'Marco','Angileri','marco','marco@libero.it','marco'),(12,'Lucia','Milazzo','lucia','lucia@hotmail.it','lucia'),(13,'Francesca','Pipitone','francesca','francesca.p@gmail.com','francesca'),(14,'Giuseppe','Angileri','peppe1','peppe@libero.it','peppe1');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'db_moneytransfer'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-05-14 16:52:35
