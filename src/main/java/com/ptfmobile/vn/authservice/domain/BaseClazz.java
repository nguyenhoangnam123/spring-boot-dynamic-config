/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptfmobile.vn.authservice.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author MinhDV
 */

public class BaseClazz {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void baseLog(String key, String action, Object object) {
        try {
            logger.info("=> {}_{}: {}", key, action, object);
        } catch (Throwable throwable) {
            logger.error("ex = {}", throwable);
        }
    }

    protected void baseLog(String key, Object action) {
        try {
            logger.info("=> {}_{}", key, action);
        } catch (Throwable throwable) {
            logger.error("ex = {}", throwable);
        }
    }

}
