drop database if exists bridge;
create database bridge;
use bridge;


SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `dnslog`;
CREATE TABLE `dnslog` (
  `id` varchar(36) DEFAULT NULL,
  `host` text DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `ip` text DEFAULT NULL,
  `time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `logid` int DEFAULT NULL
) CHARSET=utf8;


DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `userid` varchar(36) DEFAULT NULL,
  `username` varchar(32) DEFAULT NULL,
  `password` text DEFAULT NULL,
  `logid` int DEFAULT NULL,
  `apikey` text DEFAULT NULL
) CHARSET=utf8;

DROP TABLE IF EXISTS `weblog`;
CREATE TABLE `weblog` (
  `id` varchar(36) DEFAULT NULL,
  `host` text DEFAULT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `method` varchar(32) DEFAULT NULL,
  `version` varchar(32) DEFAULT NULL,
  `path` text DEFAULT NULL,
  `header` text DEFAULT NULL,
  `params` text DEFAULT NULL,
  `data` text DEFAULT NULL,
  `time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `logid` int DEFAULT NULL
) CHARSET=utf8;


DROP TABLE IF EXISTS `dns_record_a`;
CREATE TABLE `dns_record_a` (
  `id` varchar(36) DEFAULT NULL,
  `subdomain` text DEFAULT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `logid` int DEFAULT NULL
) CHARSET=utf8;

DROP TABLE IF EXISTS `dns_record_rebind`;
CREATE TABLE `dns_record_rebind` (
  `id` varchar(36) DEFAULT NULL,
  `subdomain` text DEFAULT NULL,
  `ip1` varchar(32) DEFAULT NULL,
  `ip2` varchar(32) DEFAULT NULL,
  `time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `logid` int DEFAULT NULL
) CHARSET=utf8;

DROP TABLE IF EXISTS `response`;
CREATE TABLE `response` (
  `id` varchar(36) DEFAULT NULL,
  `subdomain` text DEFAULT NULL,
  `responseType` text DEFAULT NULL,
  `statusCode` int DEFAULT NULL,
  `responsebody` text DEFAULT NULL,
  `headers` text DEFAULT NULL,
  `redirectURL` text DEFAULT NULL,
  `time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `logid` int DEFAULT NULL
) CHARSET=utf8;