package net.nonswag.passprotect.api.files;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.nonswag.core.api.file.formats.JsonFile;
import net.nonswag.tnl.cryptography.AES;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TrustedDevices extends JsonFile {

    @Nonnull
    private final List<Device> devices = new ArrayList<>();
    @Nonnull
    private final String username;

    TrustedDevices(@Nonnull String username) {
        super(username, "devices.json");
        this.username = username;
        JsonObject root = getJsonElement().getAsJsonObject();
        root.entrySet().forEach(entry -> {
            if (!entry.getValue().isJsonObject()) return;
            JsonObject object = entry.getValue().getAsJsonObject();
            if (!object.has("name") || !object.has("serial-number")) return;
            String name = object.get("name").getAsString();
            String serialNumber = object.get("serial-number").getAsString();
            devices.add(new Device(name, entry.getKey(), serialNumber));
        });
    }

    @Nonnull
    public List<Device> getDevices() {
        return ImmutableList.copyOf(devices);
    }

    @Nonnull
    public String getPassword(@Nonnull net.nonswag.adb.Device device) throws IllegalStateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        JsonObject root = getJsonElement().getAsJsonObject();
        if (!root.has(device.getMacAddress())) throw new IllegalStateException("device not trusted");
        JsonObject object = root.get(device.getMacAddress()).getAsJsonObject();
        if (!object.has("password")) throw new IllegalStateException("password is not defined");
        return getAES(device.getFingerprint()).decrypt(object.get("password").getAsString());
    }

    public void trust(@Nonnull net.nonswag.adb.Device device, @Nonnull String password) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        JsonObject root = getJsonElement().getAsJsonObject();
        JsonObject object = new JsonObject();
        object.addProperty("name", device.getModel());
        object.addProperty("serial-number", device.getSerialNumber());
        object.addProperty("password", getAES(device.getFingerprint()).encrypt(password));
        root.add(device.getMacAddress(), object);
        devices.add(new Device(device));
        save();
    }

    public void untrust(@Nonnull Device device) {
        devices.remove(device);
        getJsonElement().getAsJsonObject().remove(device.getMacAddress());
        save();
    }

    public boolean isTrusted(@Nonnull net.nonswag.adb.Device device) {
        for (Device all : devices) if (all.getMacAddress().equals(device.getMacAddress())) return true;
        return false;
    }

    public boolean hasTrustedDevices() {
        return !devices.isEmpty();
    }

    @Nonnull
    private AES getAES(@Nonnull String fingerprint) {
        return new AES(Hashing.sha256().hashBytes(fingerprint.getBytes(StandardCharsets.UTF_8)).asBytes());
    }

    @Getter
    public static class Device {

        @Nonnull
        private final String name, macAddress, serialNumber;

        public Device(@Nonnull String name, @Nonnull String macAddress, @Nonnull String serialNumber) {
            this.name = name;
            this.macAddress = macAddress;
            this.serialNumber = serialNumber;
        }

        public Device(@Nonnull net.nonswag.adb.Device device) {
            this(device.getModel(), device.getMacAddress(), device.getSerialNumber());
        }
    }
}
