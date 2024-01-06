package net.thenextlvl.passprotect.application;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.thenextlvl.passprotect.PassProtect;

import java.io.File;

@Getter
@ToString
@AllArgsConstructor
public class ApplicationData {
    @SerializedName("desktop") private File desktop;
    @SerializedName("activities") private File activities;

    public void setActivities(File activities) {
        this.activities = activities;
        PassProtect.getDataFile().save();
    }

    public void setDesktop(File desktop) {
        this.desktop = desktop;
        PassProtect.getDataFile().save();
    }
}