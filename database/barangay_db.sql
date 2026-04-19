-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 19, 2026 at 04:08 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `barangay_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `requests`
--

CREATE TABLE `requests` (
  `id` int(11) NOT NULL,
  `requester_name` varchar(150) NOT NULL,
  `contact_number` varchar(20) NOT NULL,
  `email` varchar(150) NOT NULL,
  `request_type` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `status` enum('PENDING','RESOLVED') NOT NULL DEFAULT 'PENDING',
  `submitted_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `requests`
--

INSERT INTO `requests` (`id`, `requester_name`, `contact_number`, `email`, `request_type`, `description`, `status`, `submitted_at`) VALUES
(2, 'Maria Santos', '+63 912 345 6789', 'maria@gmail.com', 'Certificate of Indigency', 'Needed for scholarship application.', 'RESOLVED', '2026-04-18 17:49:26'),
(3, 'Pedro Garcia', '+63 917 111 2222', 'pedro@gmail.com', 'Certificate of Residency', 'Required by the court.', 'RESOLVED', '2026-04-18 17:49:26'),
(9, 'Lanz Justin Diego', '09876543221', 'Lanzdimagiba@gmail.com', 'Barangay Business Permit', 'no work', 'RESOLVED', '2026-04-18 23:14:47'),
(10, 'Lanz Justin Diego', '09876543221', 'Lanzdimagiba@gmail.com', 'Barangay Clearance Certificate', 'asda', 'RESOLVED', '2026-04-18 23:16:53'),
(11, 'Lanz Justin Diego', '09876543221', 'Lanzdimagiba@gmail.com', 'Barangay Clearance Application', 'for national id', 'RESOLVED', '2026-04-18 23:24:05'),
(12, 'Lanz Justin Diego', '09876543221', 'Lanzdimagiba@gmail.com', 'Barangay Clearance Application', 'For immersion work', 'PENDING', '2026-04-18 23:27:23'),
(13, 'Lanz Justin Diego', '09876543221', 'Lanzdimagiba@gmail.com', 'Barangay Business Permit', 'no wurk tu du', 'RESOLVED', '2026-04-18 23:30:41');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `email` varchar(150) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `is_admin` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `full_name`, `email`, `phone`, `password`, `is_admin`, `created_at`) VALUES
(1, 'Admin User', 'admin@gmail.com', '+63 900 000 0001', 'admin123', 1, '2026-04-18 17:49:26'),
(2, 'Maria Santos', 'mariasantos@gmail.com', '+63 912 345 6789', 'maria123', 1, '2026-04-18 17:49:26'),
(8, 'Lanz Justin Diego', 'Lanzdimagiba@gmail.com', '09876543221', 'lanz123', 0, '2026-04-18 21:51:22');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `requests`
--
ALTER TABLE `requests`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `requests`
--
ALTER TABLE `requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
