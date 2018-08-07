package com.example.amitk.mobilecon;

import android.app.Application;

import java.util.ArrayList;

/**
 * Created by amitk on 07-Feb-18.
 */

public class GlobalVar extends Application{
    ArrayList<String> name=new ArrayList<>();
    ArrayList<String> numbers=new ArrayList<>();

    public ArrayList<String> getName() {
        return name;
    }

    public void setName(ArrayList<String> name) {
        this.name = name;
    }

    public ArrayList<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<String> numbers) {
        this.numbers = numbers;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        System.out.println("///////////////////////-----------------------////////////// Terminate");
    }
}
