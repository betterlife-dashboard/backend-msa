package com.betterlife.auth.controller;

import com.betterlife.auth.dto.*;
import com.betterlife.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(operationId = "me", summary = "토큰 검증")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 검증 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 유저",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    })
    @Parameter(
            in = ParameterIn.HEADER,
            name = "X-User-Id",
            required = true,
            schema = @Schema(type = "integer", defaultValue = "-1", example = "-1")
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(authService.me(userId));
    }

    @Operation(operationId = "register", summary = "회원가입")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 형식",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
            @ApiResponse(responseCode = "409", description = "중복된 이메일",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    @Operation(operationId = "login", summary = "로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 형식",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        String refresh = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(2592000)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(authService.renew(refresh));
    }

    @Operation(operationId = "renew", summary = "토큰 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "갱신 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 형식",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    })
    @Parameter(
            in = ParameterIn.HEADER,
            name = "refresh_token",
            required = true,
            schema = @Schema(type = "string", defaultValue = "token_example", example = "token_example")
    )
    @PostMapping("/renew")
    public ResponseEntity<LoginResponse> renew(@CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse response) {
        String refresh = authService.checkRefresh(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(2592000)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(authService.renew(refresh));
    }

    @Operation(operationId = "logout", summary = "로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    })
    @Parameter(
            in = ParameterIn.HEADER,
            name = "refresh_token",
            required = true,
            schema = @Schema(type = "string", defaultValue = "token_example", example = "token_example")
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token") String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(operationId = "withdraw", summary = "탈퇴")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 완료"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    })
    @Parameter(
            in = ParameterIn.HEADER,
            name = "refresh_token",
            required = true,
            schema = @Schema(type = "string", defaultValue = "token_example", example = "token_example")
    )
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@CookieValue(name = "refresh_token") String refreshToken) {
        authService.withdraw(refreshToken);
        return ResponseEntity.noContent().build();
    }
}
