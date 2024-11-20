# Project Setup Guide

## Prerequisites

- JDK 1.8
- Spring Boot 2.7.6
- MySQL 8.3.0
- Redis 7.2.4

## Setup Steps

1. **MySQL Setup**
    - Install MySQL (or use AWS RDS)
    - Run scripts from `sql` directory

2. **Redis Setup**
    - Install Redis (or use Digital Ocean Database Caching)
    - Note: AWS ElastiCache not recommended (private VPC limitations)

3. **Environment Configuration**  
   Configure in `application.yml`:
    - MySQL connection
    - Redis connection
    - OpenAI token
