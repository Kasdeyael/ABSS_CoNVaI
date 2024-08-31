-- MySQL dump 10.13  Distrib 8.0.33, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: simulation
-- ------------------------------------------------------
-- Server version	8.0.33

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
-- Table structure for table `data_configuration_by_model`
--

DROP TABLE IF EXISTS `data_configuration_by_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `data_configuration_by_model` (
  `configurationUID` int unsigned NOT NULL,
  `execution` smallint unsigned NOT NULL,
  `probRead` decimal(9,8) unsigned DEFAULT '0.00000000',
  `probInfect` decimal(9,8) unsigned NOT NULL,
  `probDebunk` decimal(9,8) unsigned NOT NULL,
  `probInfl` decimal(9,8) unsigned DEFAULT '0.00000000',
  `probReply` decimal(9,8) unsigned DEFAULT '0.00000000',
  `probChange` decimal(9,8) unsigned NOT NULL,
  `probOpinion` decimal(9,8) unsigned DEFAULT '0.00000000',
  `noveltyFactor` decimal(9,8) unsigned DEFAULT '0.00000000',
  `randomSeed` mediumint unsigned NOT NULL,
  `confidence` decimal(9,8) unsigned DEFAULT '0.00000000',
  `engagement` decimal(9,8) unsigned DEFAULT '0.00000000',
  `useProbReply` tinyint unsigned DEFAULT '0',
  `k` decimal(9,8) unsigned DEFAULT '1.00000000',
  PRIMARY KEY (`configurationUID`,`execution`),
  CONSTRAINT `IniDataMod` FOREIGN KEY (`configurationUID`) REFERENCES `initial_data_configuration` (`configurationUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table for the probabilities used on each configuration, relative to each model.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `errors_ablation`
--

DROP TABLE IF EXISTS `errors_ablation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `errors_ablation` (
  `configuration_best` int unsigned NOT NULL,
  `configuration_ablation` int unsigned NOT NULL,
  `isSpread` tinyint unsigned NOT NULL,
  `RMSE` decimal(15,6) unsigned NOT NULL,
  `RMSESpr` decimal(15,6) unsigned NOT NULL,
  `RMSEDeb` decimal(15,6) unsigned NOT NULL,
  `MSD` decimal(15,6) NOT NULL,
  `MSDSpr` decimal(15,6) NOT NULL,
  `MSDDeb` decimal(15,6) NOT NULL,
  PRIMARY KEY (`configuration_best`,`configuration_ablation`,`isSpread`),
  KEY `IniDataAbl` (`configuration_ablation`),
  CONSTRAINT `IniDataAbl` FOREIGN KEY (`configuration_ablation`) REFERENCES `initial_data_configuration` (`configurationUID`),
  CONSTRAINT `IniDataAblFr` FOREIGN KEY (`configuration_best`) REFERENCES `initial_data_configuration` (`configurationUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `initial_data_configuration`
--

DROP TABLE IF EXISTS `initial_data_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `initial_data_configuration` (
  `configurationUID` int unsigned NOT NULL,
  `spreadUID` smallint unsigned NOT NULL,
  `modelType` tinyint unsigned NOT NULL DEFAULT '1',
  `netType` tinyint unsigned NOT NULL,
  `linksPerNode` smallint unsigned DEFAULT NULL,
  `networkSeed` mediumint unsigned NOT NULL,
  `initialNodesNetwork` smallint unsigned DEFAULT NULL,
  `probConnect` decimal(9,8) unsigned DEFAULT NULL,
  PRIMARY KEY (`configurationUID`),
  KEY `fk_ini_spread_idx` (`spreadUID`) /*!80000 INVISIBLE */,
  KEY `fk_ini_conf_idx` (`configurationUID`),
  CONSTRAINT `SpreadID` FOREIGN KEY (`spreadUID`) REFERENCES `news_being_sent` (`spreadUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table for the initial data per configuration, stating all the probabilities and number of users for any model.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `msg_per_run`
--

DROP TABLE IF EXISTS `msg_per_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `msg_per_run` (
  `configurationUID` int unsigned NOT NULL,
  `execution` smallint unsigned NOT NULL,
  `newsUID` smallint unsigned NOT NULL,
  `tick` smallint unsigned NOT NULL,
  `vaccinated` smallint unsigned NOT NULL,
  `infected` smallint unsigned NOT NULL,
  PRIMARY KEY (`configurationUID`,`execution`,`newsUID`,`tick`),
  KEY `Execution_idx` (`execution`),
  CONSTRAINT `dataConfMsg` FOREIGN KEY (`configurationUID`, `execution`) REFERENCES `data_configuration_by_model` (`configurationUID`, `execution`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table for storing the output msgs of the agents, for each run.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `msg_rmse_results`
--

DROP TABLE IF EXISTS `msg_rmse_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `msg_rmse_results` (
  `configurationUID` int unsigned NOT NULL,
  `execution` smallint unsigned NOT NULL,
  `newsUID` smallint unsigned NOT NULL,
  `RMSE` decimal(15,6) unsigned NOT NULL,
  `RMSESpr` decimal(15,6) unsigned NOT NULL,
  `RMSEDeb` decimal(15,6) unsigned NOT NULL,
  `MAE` decimal(15,6) unsigned NOT NULL,
  `MAESpr` decimal(15,6) unsigned NOT NULL,
  `MAEDeb` decimal(15,6) unsigned NOT NULL,
  `weightSpr` smallint unsigned NOT NULL,
  `weightDeb` smallint unsigned NOT NULL,
  PRIMARY KEY (`configurationUID`,`execution`,`newsUID`),
  CONSTRAINT `DataConfigMsgEr` FOREIGN KEY (`configurationUID`, `execution`) REFERENCES `data_configuration_by_model` (`configurationUID`, `execution`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table for storing the results of RMSE for each run and for a specific dataset.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `news_being_sent`
--

DROP TABLE IF EXISTS `news_being_sent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news_being_sent` (
  `spreadUID` smallint unsigned NOT NULL,
  `newsUID` smallint unsigned NOT NULL,
  `tick` smallint DEFAULT NULL,
  PRIMARY KEY (`newsUID`,`spreadUID`),
  KEY `spreadIdx` (`spreadUID`),
  CONSTRAINT `NewsInfoSentID` FOREIGN KEY (`newsUID`) REFERENCES `news_information` (`newsUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='For each configuration, we set the news that were sent (if applicable) and when.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `news_information`
--

DROP TABLE IF EXISTS `news_information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news_information` (
  `newsUID` smallint unsigned NOT NULL,
  `name` varchar(30) DEFAULT NULL,
  `author` varchar(30) DEFAULT NULL,
  `probInfl` decimal(9,8) unsigned DEFAULT NULL,
  `novelty` decimal(9,8) unsigned DEFAULT NULL,
  PRIMARY KEY (`newsUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Table for the information about each news (or message), pertaining its name, influence and novelty.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `real_dataset_msg`
--

DROP TABLE IF EXISTS `real_dataset_msg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `real_dataset_msg` (
  `newsUID` smallint unsigned NOT NULL,
  `timestamp` smallint unsigned NOT NULL,
  `supporting` smallint unsigned DEFAULT '0',
  `denying` smallint unsigned DEFAULT '0',
  PRIMARY KEY (`newsUID`,`timestamp`),
  CONSTRAINT `newsInfoID` FOREIGN KEY (`newsUID`) REFERENCES `news_information` (`newsUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table for storing the dataset messages.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `real_dataset_spread`
--

DROP TABLE IF EXISTS `real_dataset_spread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `real_dataset_spread` (
  `newsUID` smallint unsigned NOT NULL,
  `timestamp` smallint unsigned NOT NULL,
  `supporting` smallint unsigned DEFAULT '0',
  `denying` smallint unsigned DEFAULT '0',
  `neutral` smallint unsigned DEFAULT '0',
  PRIMARY KEY (`newsUID`,`timestamp`),
  CONSTRAINT `newsInfoSprID` FOREIGN KEY (`newsUID`) REFERENCES `news_information` (`newsUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table for storing the dataset spread.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state_per_run`
--

DROP TABLE IF EXISTS `state_per_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `state_per_run` (
  `configurationUID` int unsigned NOT NULL,
  `execution` smallint unsigned NOT NULL,
  `tick` smallint unsigned NOT NULL,
  `vaccinated` smallint unsigned NOT NULL,
  `infected` smallint unsigned NOT NULL,
  `neutral` smallint unsigned NOT NULL,
  PRIMARY KEY (`configurationUID`,`execution`,`tick`),
  KEY `Execution_idx` (`execution`),
  CONSTRAINT `dataConfStat` FOREIGN KEY (`configurationUID`, `execution`) REFERENCES `data_configuration_by_model` (`configurationUID`, `execution`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Table for storing the output state of the agents, for each run.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state_rmse_results`
--

DROP TABLE IF EXISTS `state_rmse_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `state_rmse_results` (
  `configurationUID` int unsigned NOT NULL,
  `execution` smallint unsigned NOT NULL,
  `RMSE` decimal(15,6) unsigned NOT NULL,
  `RMSESpr` decimal(15,6) unsigned NOT NULL,
  `RMSEDeb` decimal(15,6) unsigned NOT NULL,
  `MAE` decimal(15,6) unsigned NOT NULL,
  `MAESpr` decimal(15,6) unsigned NOT NULL,
  `MAEDeb` decimal(15,6) unsigned NOT NULL,
  `weightSpr` smallint unsigned NOT NULL,
  `weightDeb` smallint unsigned NOT NULL,
  PRIMARY KEY (`configurationUID`,`execution`),
  CONSTRAINT `DataConfigStEr` FOREIGN KEY (`configurationUID`, `execution`) REFERENCES `data_configuration_by_model` (`configurationUID`, `execution`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_diff_per_news`
--

DROP TABLE IF EXISTS `user_diff_per_news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_diff_per_news` (
  `newsUID` smallint unsigned NOT NULL,
  `tick` smallint unsigned NOT NULL,
  `userUID` bigint unsigned NOT NULL,
  `vaccinated` tinyint unsigned NOT NULL,
  `infected` tinyint unsigned NOT NULL,
  PRIMARY KEY (`newsUID`,`tick`,`userUID`),
  CONSTRAINT `newsInfoDiffID` FOREIGN KEY (`newsUID`) REFERENCES `news_information` (`newsUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User spread per tick. To track which user is sending what message';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_diff_per_sims`
--

DROP TABLE IF EXISTS `user_diff_per_sims`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_diff_per_sims` (
  `configurationUID` int unsigned NOT NULL,
  `execution` smallint unsigned NOT NULL,
  `newsUID` smallint unsigned NOT NULL,
  `tick` smallint unsigned NOT NULL,
  `userUID` bigint unsigned NOT NULL,
  `vaccinated` tinyint unsigned NOT NULL,
  `infected` tinyint unsigned NOT NULL,
  PRIMARY KEY (`configurationUID`,`execution`,`newsUID`,`tick`,`userUID`),
  CONSTRAINT `DataConfDiff` FOREIGN KEY (`configurationUID`, `execution`) REFERENCES `data_configuration_by_model` (`configurationUID`, `execution`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User spread per tick. To track which user is sending what message in the simulation.';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-08-18 22:42:07
