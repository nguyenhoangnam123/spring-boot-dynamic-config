#!/bin/bash

sed -i "s/{REDIS_PASS}/${REDIS_PASS}/g" application.yml
sed -i "s/{REDIS_ADDR}/${REDIS_ADDR}/g" application.yml
sed -i "s/{RABBIT_HOST}/${RABBIT_HOST}/g" application.yml
sed -i "s/{RABBIT_PORT}/${RABBIT_PORT}/g" application.yml
sed -i "s/{RABBIT_USER}/${RABBIT_USER}/g" application.yml
sed -i "s/{RABBIT_PASSWORD}/${RABBIT_PASSWORD}/g" application.yml
sed -i "s/{MONGO_DB}/${MONGO_DB}/g" application.yml
sed -i "s/{MONGO_USER}/${MONGO_USER}/g" application.yml
sed -i "s/{MONGO_PASSWORD}/${MONGO_PASSWORD}/g" application.yml
sed -i "s/{MONGO_URL}/${MONGO_URL}/g" application.yml
sed -i "s/{PUBLIC_WEB_CAPTCHA_KEY}/${PUBLIC_WEB_CAPTCHA_KEY}/g" application.yml
sed -i "s/{SECRET_WEB_CAPTCHA_KEY}/${SECRET_WEB_CAPTCHA_KEY}/g" application.yml
sed -i "s/{SECRET_APP_CAPTCHA_KEY}/${SECRET_APP_CAPTCHA_KEY}/g" application.yml
sed -i "s/{CAPTCHA_VERIFY_CALLBACK}/${CAPTCHA_VERIFY_CALLBACK}/g" application.yml

sh -c java org.springframework.boot.loader.JarLauncher --spring.profiles.active=${PROFILE}