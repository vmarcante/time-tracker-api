package com.vmarcante.time_tracker.core.interfaces.timeentry.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.TagInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TagOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.CreateTagUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.DeactivateTagUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListMyTagsUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.UpdateTagUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/tags")
@Tag(name = "Tags", description = "User tag management endpoints")
public class TagController extends BaseResponseController {

    private final CreateTagUseCase createTagUseCase;
    private final UpdateTagUseCase updateTagUseCase;
    private final DeactivateTagUseCase deactivateTagUseCase;
    private final ListMyTagsUseCase listMyTagsUseCase;

    public TagController(
            CreateTagUseCase createTagUseCase,
            UpdateTagUseCase updateTagUseCase,
            DeactivateTagUseCase deactivateTagUseCase,
            ListMyTagsUseCase listMyTagsUseCase) {
        this.createTagUseCase = createTagUseCase;
        this.updateTagUseCase = updateTagUseCase;
        this.deactivateTagUseCase = deactivateTagUseCase;
        this.listMyTagsUseCase = listMyTagsUseCase;
    }

    @AuthSecure
    @PostMapping
    @Operation(summary = "Create tag", description = "Creates a reusable tag for the authenticated user")
    public ResponseEntity<ApiResponseDTO<TagOutputDTO>> create(
            @RequestBody TagInputDTO input) throws ApplicationException {
        return created(createTagUseCase.execute(input));
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List my tags", description = "Lists the authenticated user's active tags")
    public ResponseEntity<ApiResponseDTO<List<TagOutputDTO>>> list() throws ApplicationException {
        return ok(listMyTagsUseCase.execute());
    }

    @AuthSecure
    @PutMapping("/{tagId}")
    @Operation(summary = "Update tag", description = "Updates a tag owned by the authenticated user")
    public ResponseEntity<ApiResponseDTO<TagOutputDTO>> update(
            @PathVariable UUID tagId,
            @RequestBody TagInputDTO input) throws ApplicationException {
        return ok(updateTagUseCase.execute(tagId, input));
    }

    @AuthSecure
    @DeleteMapping("/{tagId}")
    @Operation(summary = "Deactivate tag", description = "Deactivates a tag and removes it from all entries")
    public ResponseEntity<ApiResponseDTO<Void>> deactivate(
            @PathVariable UUID tagId) throws ApplicationException {
        deactivateTagUseCase.execute(tagId);
        return noContent();
    }
}
