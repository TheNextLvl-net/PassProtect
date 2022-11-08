package net.nonswag.adb;

import javax.annotation.Nonnull;

public record DeviceReference(@Nonnull String model, @Nonnull String serialNumber) {
}
