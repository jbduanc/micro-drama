package com.series.user.service;

import com.series.common.auth.AuthTokenIssueService;
import com.series.common.auth.AuthTokenPair;
import com.series.user.dto.LoginTokenDTO;
import com.series.user.dto.UserProfileDTO;
import com.series.user.entity.AppUser;
import com.series.user.utils.UserJwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    @Autowired
    private AuthTokenIssueService authTokenIssueService;

    @Autowired
    private UserJwtUtil userJwtUtil;

    public LoginTokenDTO issueToken(AppUser user) {
        String userId = user.getId().toString();
        AuthTokenPair pair = authTokenIssueService.issue(userId, com.series.common.auth.AuthAudience.USER);
        return new LoginTokenDTO(pair.getAccessToken(), pair.getRefreshToken(), toProfile(user));
    }

    public static UserProfileDTO toProfile(AppUser user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId().toString());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatar());
        dto.setBalance(user.getBalance());
        dto.setTelegramId(user.getTelegramId());
        dto.setWalletAddress(user.getWalletAddress());
        dto.setAuthProvider(user.getAuthProvider());
        return dto;
    }
}
