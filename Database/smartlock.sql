-- phpMyAdmin SQL Dump
-- version 4.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Dec 13, 2016 at 11:13 AM
-- Server version: 10.1.13-MariaDB
-- PHP Version: 5.6.23

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smartlock`
--

-- --------------------------------------------------------

--
-- Table structure for table `lock`
--

CREATE TABLE `lock` (
  `lock_id` int(11) NOT NULL,
  `mac` varchar(20) NOT NULL,
  `name` varchar(11) NOT NULL,
  `password` varchar(11) NOT NULL,
  `status` enum('active','inactive','dead') NOT NULL DEFAULT 'inactive'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `lock`
--

INSERT INTO `lock` (`lock_id`, `mac`, `name`, `password`, `status`) VALUES
(1, '00:04:ff:ff:ff:d0', 'lock_1', '1234', 'active'),
(2, 'ec:1a:59:61:07:b2', 'lock_2', '3444', 'active'),
(3, '90:59:af:3d:6d:bc', 'lock_3', '5555', 'active'),
(4, '3c:97:0e:48:22:12', 'lock_4', '3423', 'active'),
(5, '00:18:31:87:8f:b0', 'lock_5', '4567', 'active');

-- --------------------------------------------------------

--
-- Table structure for table `owners`
--

CREATE TABLE `owners` (
  `user_id` int(11) NOT NULL,
  `lock_id` int(11) NOT NULL,
  `user_type` enum('root','owner') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `owners`
--

INSERT INTO `owners` (`user_id`, `lock_id`, `user_type`) VALUES
(12, 1, 'root'),
(12, 2, 'root'),
(12, 3, 'owner'),
(12, 5, 'owner');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `user_name` varchar(11) NOT NULL,
  `email` varchar(20) NOT NULL,
  `password` varchar(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `user_name`, `email`, `password`) VALUES
(5, 'minhquan', 'mq@email', '123456'),
(9, 'a', 'aa', 'aaa'),
(10, 'b', 'bb', 'bbb'),
(12, 'sang8', 'sang@email', 'sang'),
(15, 'c', 'cc', 'ccc'),
(19, 'aa', 'aaa', 'aaa'),
(20, 'sang9', 'sang9@email', 'sang');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `lock`
--
ALTER TABLE `lock`
  ADD PRIMARY KEY (`lock_id`),
  ADD UNIQUE KEY `mac` (`mac`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `lock`
--
ALTER TABLE `lock`
  MODIFY `lock_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
