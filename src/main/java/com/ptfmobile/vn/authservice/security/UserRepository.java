package com.ptfmobile.vn.authservice.security;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ptfmobile.vn.authservice.domain.exception.UserNotExistException;
import com.ptfmobile.vn.authservice.domain.exception.UserUnavailableException;
import com.ptfmobile.vn.common.DBConstant.TableName;
import com.ptfmobile.vn.common.Status;
import com.ptfmobile.vn.db.dto.User;
import com.ptfmobile.vn.mongodb.BaseMongoDao;

import java.util.Collections;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final BaseMongoDao baseMongoDao;

    @Autowired
    public UserRepository(MongoDatabase mongoDatabase) {
        this.baseMongoDao = new BaseMongoDao(mongoDatabase);
    }

    public CustomUserDetails findByUsername(String username)
            throws UserNotExistException, UserUnavailableException {
        User user;
        user = baseMongoDao.findOneV2(TableName.USERS, Filters.regex("userId", username, "i"), User.class);
        if (Objects.isNull(user)) {
            user = baseMongoDao.findOneV2(TableName.USERS, Filters.regex("email", username, "i"), User.class);
            if (Objects.isNull(user)) {
                user = baseMongoDao.findOneV2(TableName.USERS, Filters.regex("saleId", username, "i"), User.class);
            }
        }

        if (Objects.isNull(user)) {
            throw new UserNotExistException("Tài khoản " + username + " không tồn tại");
        }

        if (user.getIsDelete() || Status.INACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new UserUnavailableException("Tài khoản " + username + " không hoạt động hoặc đã bị xóa");
        }
        return new CustomUserDetails(user.getUserId(), user.getPassword(), Collections.singletonList("ROLE_USER"));
    }
}
