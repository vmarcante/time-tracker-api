package com.vmarcante.time_tracker.core.interfaces.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.ConfirmUserRegistrationInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.CreateUserInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CreateUserOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.CheckUsernameAvailabilityUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.in.ConfirmUserRegistrationUseCase;
import com.vmarcante.time_tracker.core.application.person.in.CheckEmailAvailabilityUseCase;
import com.vmarcante.time_tracker.core.application.user.orchestrator.UserRegistrationOrchestrator;
import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.ProgressiveLevel;
import com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.annotation.RateLimit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/public/register")
@Tag(name = "User Registration", description = "User registration endpoints")
public class UserRegisterController extends BaseResponseController {

    private final UserRegistrationOrchestrator userRegistrationOrchestrator;
    private final CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase;
    private final CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;
    private final ConfirmUserRegistrationUseCase confirmUserRegistrationUseCase;

    public UserRegisterController(
            UserRegistrationOrchestrator userRegistrationOrchestrator,
            CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase,
            CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase,
            ConfirmUserRegistrationUseCase confirmUserRegistrationUseCase) {
        this.userRegistrationOrchestrator = userRegistrationOrchestrator;
        this.checkUsernameAvailabilityUseCase = checkUsernameAvailabilityUseCase;
        this.checkEmailAvailabilityUseCase = checkEmailAvailabilityUseCase;
        this.confirmUserRegistrationUseCase = confirmUserRegistrationUseCase;
    }

    @PostMapping
    @RateLimit(
        maxRequests = 5, 
        windowSeconds = 3600, 
        key = "register:signup",
        progressive = true,
        progressiveLevels = {
            @ProgressiveLevel(maxRequests = 5, windowSeconds = 120, banSeconds = 60),
            @ProgressiveLevel(maxRequests = 10, windowSeconds = 600, banSeconds = 1800),
            @ProgressiveLevel(maxRequests = 15, windowSeconds = 1800, banSeconds = 3600)
        }
    )
    @Operation(summary = "Register new user", description = "Creates a new user account with authentication credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username already exists"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<ApiResponseDTO<CreateUserOutputDTO>> signup(
            @RequestBody CreateUserInputDTO input,
            @RequestHeader(value = "Accept-Language", required = false, defaultValue = "pt") String acceptLanguage)
            throws ApplicationException {

        String locale = acceptLanguage != null && acceptLanguage.startsWith("en") ? "en" : "pt";
        CreateUserInputDTO inputWithLocale = new CreateUserInputDTO(
                input.username(),
                input.name(),
                input.age(),
                input.email(),
                input.phone(),
                input.password(),
                locale);

        CreateUserOutputDTO output = userRegistrationOrchestrator.execute(inputWithLocale);
        return created(output);
    }

    @GetMapping("/username-available")
    @RateLimit(maxRequests = 20, windowSeconds = 60, key = "register:username-available")
    @Operation(summary = "Check username availability", description = "Checks if a username is available for registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username availability checked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid username format"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<ApiResponseDTO<Boolean>> checkUsername(
            @RequestParam(value = "username", required = true) String username)
            throws ApplicationException {
        boolean isAvailable = checkUsernameAvailabilityUseCase.execute(username);
        return ok(isAvailable);
    }

    @GetMapping("/email-available")
    @RateLimit(maxRequests = 20, windowSeconds = 60, key = "register:email-available")
    @Operation(summary = "Check email availability", description = "Checks if an email is available for registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email availability checked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email format"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<ApiResponseDTO<Boolean>> checkEmail(
            @RequestParam(value = "email", required = true) String email)
            throws ApplicationException {
        boolean isAvailable = checkEmailAvailabilityUseCase.execute(email);
        return ok(isAvailable);
    }

    @PatchMapping("/confirm")
    @RateLimit(maxRequests = 10, windowSeconds = 60, key = "register:confirm")
    @Operation(summary = "Confirm user registration", description = "Confirms user registration using the access token sent via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User registration confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing access token"),
            @ApiResponse(responseCode = "404", description = "User not found or token expired"),
            @ApiResponse(responseCode = "409", description = "User already confirmed"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public ResponseEntity<ApiResponseDTO<Void>> confirmRegistration(
            @RequestBody ConfirmUserRegistrationInputDTO input)
            throws ApplicationException {
        confirmUserRegistrationUseCase.execute(input);
        return noContent();
    }
}
