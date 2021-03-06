package com.puzheng.lejian.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-11-28.
 */
public class SPUType implements Parcelable {

    private int id;
    private String name;
    private int weight;
    private boolean enabled;
    private int spuCnt;
    private final Pic pic;

    protected SPUType(Parcel in) {
        id = in.readInt();
        name = in.readString();
        weight = in.readInt();
        enabled = in.readByte() != 0;
        spuCnt = in.readInt();
        pic = in.readParcelable(Pic.class.getClassLoader());
    }

    public static final Creator<SPUType> CREATOR = new Creator<SPUType>() {
        @Override
        public SPUType createFromParcel(Parcel in) {
            return new SPUType(in);
        }

        @Override
        public SPUType[] newArray(int size) {
            return new SPUType[size];
        }
    };

    public Pic getPic() {
        return pic;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSpuCnt() {
        return spuCnt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(weight);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeInt(spuCnt);
        dest.writeParcelable(pic, 0);
    }

    public SPUType(int id, String name, int weight, boolean enabled,
                   int spuCnt, Pic pic) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.enabled = enabled;
        this.spuCnt = spuCnt;
        this.pic = pic;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
