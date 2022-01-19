#!/bin/sh

sed -i "s#{REDIS_PASS}#${REDIS_PASS}#g" BOOT-INF/classes/application.yml
sed -i "s#{REDIS_URL}#${REDIS_URL}#g" BOOT-INF/classes/application.yml
sed -i "s#{RABBIT_HOST}#${RABBIT_HOST}#g" BOOT-INF/classes/application.yml
sed -i "s#{RABBIT_PORT}#${RABBIT_PORT}#g" BOOT-INF/classes/application.yml
sed -i "s#{RABBIT_USER}#${RABBIT_USER}#g" BOOT-INF/classes/application.yml
sed -i "s/{RABBIT_PASS}/${RABBIT_PASS}/g" BOOT-INF/classes/application.yml
sed -i "s#{MONGO_DB}#${MONGO_DB}#g" BOOT-INF/classes/application.yml
sed -i "s#{MONGO_USER}#${MONGO_USER}#g" BOOT-INF/classes/application.yml
sed -i "s#{MONGO_PASS}#${MONGO_PASS}#g" BOOT-INF/classes/application.yml
sed -i "s#{MONGO_URL}#${MONGO_URL}#g" BOOT-INF/classes/application.yml
sed -i "s#{PUBLIC_WEB_CAPTCHA_KEY}#${PUBLIC_WEB_CAPTCHA_KEY}#g" BOOT-INF/classes/application.yml
sed -i "s#{SECRET_WEB_CAPTCHA_KEY}#${SECRET_WEB_CAPTCHA_KEY}#g" BOOT-INF/classes/application.yml
sed -i "s#{SECRET_APP_CAPTCHA_KEY}#${SECRET_APP_CAPTCHA_KEY}#g" BOOT-INF/classes/application.yml
sed -i "s#{CAPTCHA_VERIFY_CALLBACK}#${CAPTCHA_VERIFY_CALLBACK}#g" BOOT-INF/classes/application.yml

java org.springframework.boot.loader.JarLauncher
# java -Dspring.config.location=file:BOOT-INF/classes/application.yml org.springframework.boot.loader.JarLauncher --spring.profiles.active=${PROFILE}
# SPRING_PROFILES_ACTIVE