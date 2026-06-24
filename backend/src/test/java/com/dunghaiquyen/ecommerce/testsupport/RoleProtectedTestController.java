package com.dunghaiquyen.ecommerce.testsupport;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only: exists solely so AuthorizationIntegrationTest can prove the
 * "valid auth, insufficient role -> 403 standardized ApiResponse" behavior
 * end-to-end. Lives under src/test/java so it is never part of the shipped
 * application; picked up automatically because it is a sub-package of the
 * component-scanned base package com.dunghaiquyen.ecommerce.
 */
@RestController
public class RoleProtectedTestController {

    @GetMapping("/api/v1/_test/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
        return "ok";
    }
}
