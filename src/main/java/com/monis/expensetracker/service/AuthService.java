package com.monis.expensetracker.service;

import com.monis.expensetracker.dto.AuthResponseDto;
import com.monis.expensetracker.dto.LoginRequestDto;
import com.monis.expensetracker.dto.RegisterRequestDto;
import com.monis.expensetracker.entity.User;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto registerDto);

    AuthResponseDto login(LoginRequestDto loginDto);

    User getCurrentAuthenticatedUser();
}
