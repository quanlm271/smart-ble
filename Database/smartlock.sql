-- phpMyAdmin SQL Dump
-- version 4.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jan 16, 2017 at 06:29 AM
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
-- Table structure for table `hictory`
--

CREATE TABLE `hictory` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `lock_id` int(11) NOT NULL,
  `command` enum('lock','unlock') NOT NULL,
  `timestamp` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `location` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `hictory`
--

INSERT INTO `hictory` (`id`, `user_id`, `lock_id`, `command`, `timestamp`, `location`) VALUES
(1, 12, 5, 'unlock', '2017/01/16 09:55:41', 'Quan 9, TPHCM'),
(2, 12, 5, 'lock', '2017/01/16 09:55:44', 'Quan 9, TPHCM'),
(7, 12, 5, 'unlock', '01/16/2017 12:17:18', 'District 9, Ho Chi Minh');

-- --------------------------------------------------------

--
-- Table structure for table `lock`
--

CREATE TABLE `lock` (
  `lock_id` int(11) NOT NULL,
  `mac` varchar(20) NOT NULL,
  `name` varchar(11) NOT NULL,
  `pin` varchar(11) NOT NULL,
  `status` enum('active','inactive','dead') NOT NULL DEFAULT 'inactive'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `lock`
--

INSERT INTO `lock` (`lock_id`, `mac`, `name`, `pin`, `status`) VALUES
(1, '00:04:FF:FF:FF:D0', 'lock_1', '01020304', 'active'),
(2, 'EC:1A:59:61:07:B2', 'lock_2', '03040404', 'active'),
(3, '90:59:af:3d:6d:bc', 'lock_3', '05050505', 'active'),
(4, '3C:97:0E:48:22:12', 'lock_4', '03040203', 'active'),
(5, '00:18:31:87:8F:B0', 'lock_5', '01020404', 'active'),
(6, '80:E2:4C:5E:61:58', 'lock_06', '05040302', 'active'),
(18, '87:C2:54:12:34:5A', 'lock_09', '01020304', 'active'),
(21, '88:C2:55:12:34:5A', 'HM10_2', '01020304', 'active');

-- --------------------------------------------------------

--
-- Table structure for table `owners`
--

CREATE TABLE `owners` (
  `owner_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `lock_id` int(11) NOT NULL,
  `user_type` enum('root','owner') NOT NULL,
  `pin` varchar(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `owners`
--

INSERT INTO `owners` (`owner_id`, `user_id`, `lock_id`, `user_type`, `pin`) VALUES
(1, 12, 1, 'root', ''),
(2, 12, 2, 'root', ''),
(3, 12, 3, 'owner', ''),
(4, 12, 5, 'root', '01020506'),
(5, 5, 18, 'root', ''),
(6, 5, 2, 'owner', ''),
(7, 20, 1, 'owner', ''),
(8, 5, 1, 'owner', ''),
(9, 5, 21, 'root', ''),
(10, 12, 21, 'root', ''),
(11, 5, 5, 'owner', '');

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
(20, 'sang9', 'sang9@email', 'sang'),
(21, 'sang10', 'sang@gmail', 'sang'),
(22, 'sang11', 'sang11@email', 'sang'),
(23, 'sang12', 'sang12@email', 'sang'),
(24, 'sang15', 'sang15@email', 'sang');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `hictory`
--
ALTER TABLE `hictory`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `lock`
--
ALTER TABLE `lock`
  ADD PRIMARY KEY (`lock_id`),
  ADD UNIQUE KEY `mac` (`mac`);

--
-- Indexes for table `owners`
--
ALTER TABLE `owners`
  ADD PRIMARY KEY (`owner_id`);

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
-- AUTO_INCREMENT for table `hictory`
--
ALTER TABLE `hictory`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;
--
-- AUTO_INCREMENT for table `lock`
--
ALTER TABLE `lock`
  MODIFY `lock_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;
--
-- AUTO_INCREMENT for table `owners`
--
ALTER TABLE `owners`
  MODIFY `owner_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
