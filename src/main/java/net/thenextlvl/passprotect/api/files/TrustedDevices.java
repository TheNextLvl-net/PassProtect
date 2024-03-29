package net.thenextlvl.passprotect.api.files;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.thenextlvl.core.api.file.formats.JsonFile;
import net.thenextlvl.cryptography.AES;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
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

    TrustedDevices(String username) {
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
    public String getPassword(net.thenextlvl.adb.Device device) throws IllegalStateException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        JsonObject root = getJsonElement().getAsJsonObject();
        if (!root.has(device.getMacAddress())) throw new IllegalStateException("device not trusted");
        JsonObject object = root.get(device.getMacAddress()).getAsJsonObject();
        if (!object.has("password")) throw new IllegalStateException("password is not defined");
        return getAES(device.getFingerprint()).decode(object.get("password").getAsString());
    }

    public void trust(net.thenextlvl.adb.Device device, String password) throws IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        JsonObject root = getJsonElement().getAsJsonObject();
        JsonObject object = new JsonObject();
        object.addProperty("name", device.getModel());
        object.addProperty("serial-number", device.getSerialNumber());
        object.addProperty("password", getAES(device.getFingerprint()).encode(password));
        root.add(device.getMacAddress(), object);
        devices.add(new Device(device));
        save();
    }

    public void untrust(Device device) {
        devices.remove(device);
        getJsonElement().getAsJsonObject().remove(device.getMacAddress());
        save();
    }

    public boolean isTrusted(net.thenextlvl.adb.Device device) {
        for (Device all : devices) if (all.getMacAddress().equals(device.getMacAddress())) return true;
        return false;
    }

    public boolean hasTrustedDevices() {
        return !devices.isEmpty();
    }

    @Nonnull
    private AES getAES(String fingerprint) {
        return new AES(Hashing.sha256().hashBytes(fingerprint.getBytes(StandardCharsets.UTF_8)).asBytes());
    }

    @Getter
    public static class Device {

        @Nonnull
        private final String name, macAddress, serialNumber;

        public Device(String name, String macAddress, String serialNumber) {
            this.name = name;
            this.macAddress = macAddress;
            this.serialNumber = serialNumber;
        }

        public Device(net.thenextlvl.adb.Device device) {
            this(device.getModel(), device.getMacAddress(), device.getSerialNumber());
        }
    }
}
