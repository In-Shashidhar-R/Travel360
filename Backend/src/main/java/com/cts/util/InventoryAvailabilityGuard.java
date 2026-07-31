package com.cts.util;

import com.cts.entity.Inventory;
import com.cts.enumeration.Status;
import com.cts.exception.InventoryUnavailableException;


public final class InventoryAvailabilityGuard {

    private InventoryAvailabilityGuard() {}

    public static void assertBookable(Inventory inventory) {
        if (inventory.getStatus() != Status.ACTIVE) {
            throw new InventoryUnavailableException(
                    "Inventory #" + inventory.getInventoryId()
                    + " is not available for booking (current status: " + inventory.getStatus() + ").");
        }
    }
}
