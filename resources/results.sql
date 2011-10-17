-- phpMyAdmin SQL Dump
-- version 3.4.3.2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Oct 05, 2011 at 12:39 PM
-- Server version: 5.1.53
-- PHP Version: 5.3.5

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `neurobjects_new`
--

-- --------------------------------------------------------

--
-- Table structure for table `results`
--

CREATE TABLE IF NOT EXISTS `results` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animal` varchar(25) NOT NULL,
  `area` varchar(25) NOT NULL,
  `object` varchar(25) NOT NULL,
  `round` int(11) NOT NULL,
  `model` varchar(40) NOT NULL,
  `bin_size` double NOT NULL,
  `window_size` int(11) NOT NULL,
  `neuron_drop` int(11) DEFAULT NULL,
  `surrogate` enum('uniform','poisson','col_swap','neuron_swap','matrix_swap','col_swap_d','poisson_d','uniform_d','spike_jitter','mean_d','contact_swap') DEFAULT NULL,
  `num_surrogate` int(11) DEFAULT NULL,
  `pct_surrogate` double DEFAULT NULL,
  `dist_surrogate` double DEFAULT NULL,
  `num_instances` int(11) NOT NULL,
  `correct_instances` int(11) NOT NULL,
  `auroc` double NOT NULL,
  `kappa` double NOT NULL,
  `yes_fmeasure` double NOT NULL,
  `yes_fp` int(11) NOT NULL,
  `yes_fn` int(11) NOT NULL,
  `no_fmeasure` double NOT NULL,
  `no_fp` int(11) NOT NULL,
  `no_fn` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `CONTEXT` (`animal`,`area`,`object`,`round`,`model`,`bin_size`,`window_size`,`neuron_drop`,`surrogate`,`num_surrogate`,`pct_surrogate`,`dist_surrogate`),
  KEY `MAIN` (`animal`,`area`,`object`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Reference table, never changes.' AUTO_INCREMENT=1 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
