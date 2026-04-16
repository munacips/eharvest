package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.entities.RefreshToken;
import com.munashechipanga.eharvest.entities.User;

public interface RefreshTokenService {
    RefreshToken create(User user);
    RefreshToken verify(String token);
    void revoke(String token);
}
