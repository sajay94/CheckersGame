package com.example.checkers;


import android.os.Parcel;
import android.os.Parcelable;

public class Cell implements Parcelable {


    protected Cell(Parcel in) {
        this.id = in.readInt();
        this.tag = in.readString();
    }

    public static final Creator<Cell> CREATOR = new Creator<Cell>() {
        @Override
        public Cell createFromParcel(Parcel in) {
            return new Cell(in);
        }

        @Override
        public Cell[] newArray(int size) {
            return new Cell[size];
        }
    };

    private int id;
    private String tag;

    public Cell(int a, String b) {
        id = a;
        tag = b;
    }

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.tag);
    }
}
