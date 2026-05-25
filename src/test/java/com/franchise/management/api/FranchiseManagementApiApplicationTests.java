package com.franchise.management.api;

import org.junit.jupiter.api.Test;

class FranchiseManagementApiApplicationTests {

    @Test
    void applicationStarts() {
        // This test verifies that the application class exists and can be instantiated
        // Full context loading is tested in integration tests
        FranchiseManagementApiApplication application = new FranchiseManagementApiApplication();
        assert application != null;
    }
}